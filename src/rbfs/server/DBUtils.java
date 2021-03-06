package rbfs.server;

import java.math.BigInteger;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import rbfs.util.GeneralUtils;

/**
 * Handles running queries against the database and retrieving the results.
 * TODO clean up session keys every so often?
 * TODO null check
 * TODO can't do certain stuff w/o a lock (updates, at least)
 * TODO comment af
 * @author James Hoak
 * @version 1.0
 */
public class DBUtils {
    /**
     * Runs a given SQL query.
     * @param sql The SQL query to execute. Not parametrized.
     * @return The results of the query, in a ResultSet.
     * @throws DBConnectionFailedException If the connection attempt failed.
     * @throws DBQueryFailedException If the query resulted in an exception.
     * @throws IllegalArgumentException If sql is null.
     */
    public static List<Object[]> runQuery(String sql, boolean full)
            throws DBConnectionFailedException, DBQueryFailedException {
        if (sql == null)
            throw new IllegalArgumentException("Null arg passed.");
        Connection c = getConnection();
        try {
            ResultSet rs = c.createStatement().executeQuery(sql);
            return download(rs, full);
        }
        catch (SQLException x) {
            throw new DBQueryFailedException(x.getMessage(), sql);
        }
        finally {
            tryClosing(c);
        }
    }

    /**
     * Executes an array of SQL update statements in a batch. Fails if any of them fail.
     * @param sqls An array of SQL update statements (not parametrized) to execute.
     * @return An array of ints each representing the number of records changed by the
     * corresponding update query.
     * @throws DBConnectionFailedException If the database connection was not made successfully.
     * @throws DBQueryFailedException If one or more of the queries resulted in an exception.
     * @throws IllegalArgumentException If a null value or empty array is passed, or if any of the
     * given statements are null.
     */
    public static int[] runUpdate(String... sqls) throws DBConnectionFailedException,
            DBQueryFailedException {
        if (sqls == null)
            throw new IllegalArgumentException("Null arg passed.");
        else if (GeneralUtils.anyNullInArray(sqls))
            throw new IllegalArgumentException("Null statement passed.");
        else if (sqls.length == 0)
            throw new IllegalArgumentException("Got an empty array of SQL update statements.");

        Connection c = getConnection();
        try {
            Statement s = c.createStatement();
            for (String str: sqls)
                s.addBatch(str);
            return s.executeBatch();
        }
        catch (SQLException x) {
            throw new DBQueryFailedException(x.getMessage(), sqls);
        }
        finally {
            tryClosing(c);
        }
    }

    public static BigInteger generateSessionKey() throws DBConnectionFailedException,
            DBQueryFailedException {
        BigInteger key;
        do {
            key = new BigInteger(2048, new Random(System.currentTimeMillis()));
        } while (sessionInDb(key));
        return key;
    }

    /**
     * Attempt to open a connection to the database.
     * @return A Connection representing the newly-opened database connection
     * @throws DBConnectionFailedException If the connection attempt fails.
     */
    private static Connection getConnection() throws DBConnectionFailedException {
        try {
            return DriverManager.getConnection("jdbc:sqlite:rb.db");
        }
        catch (SQLException x) {
            throw new DBConnectionFailedException(x.getMessage());
        }
    }

    /**
     * Try to close the given database connection. Don't throw any errors, even if the close fails.
     * @param c The database connection to close
     * @throws IllegalArgumentException If null is passed as the value of c.
     */
    private static void tryClosing(Connection c) {
        if (c == null) {
            throw new IllegalArgumentException("Null arg passed.");
        }
        try {
            c.close();
        }
        catch (Exception x) {
            // TODO log this
        }
    }

    private static boolean sessionInDb(BigInteger sessionKey) throws DBConnectionFailedException,
            DBQueryFailedException {
        String query = "select * from Session where skey = " + sessionKey.toString() + ";";
        List<Object[]> results = runQuery(query, false);
        return !results.isEmpty();
    }

    private static List<Object[]> download(ResultSet rs, boolean full)
            throws DBConnectionFailedException {
        try {
            int cols = rs.getMetaData().getColumnCount();
            LinkedList<Object[]> resultList = new LinkedList<>();
            int numDownloadedTuples = 0;
            while ((full || (numDownloadedTuples < rs.getFetchSize())) && rs.next()) {
                Object[] tuple = new Object[cols];
                for (int i = 0; i < cols; i++)
                    tuple[i] = rs.getObject(i + 1); // columns start at 1
                resultList.add(tuple);
                numDownloadedTuples++;
            }
            return resultList;
        }
        catch (SQLException x) {
            throw new DBConnectionFailedException("Couldn't download results: " + x.getMessage());
        }
        finally {
            try {
                rs.close();
            }
            catch (SQLException x) {
                // TODO log this
            }
        }
    }

    public static class DBException extends Exception {
        public DBException(String message) {
            super(message);
        }
    }

    public static class DBConnectionFailedException extends DBException {
        public DBConnectionFailedException(String message) {
            super(message);
        }
    }

    public static class DBQueryFailedException extends DBException {
        private String[] sqls;

        public DBQueryFailedException(String message, String sql) {
            this(message, new String[]{sql});
        }

        public DBQueryFailedException(String message, String[] sqls) {
            super(message);
            this.sqls = sqls;
        }

        public String[] getQueries() {
            return sqls;
        }
    }
}
