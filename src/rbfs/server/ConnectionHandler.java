package rbfs.server;

import java.net.Socket;
import java.util.List;
import com.google.gson.*;

/**
 * A thread that handles an incoming connection and responds to the embedded request.
 * @author James Hoak
 * @version 1.0
 */
final class ConnectionHandler implements Runnable {
    // TODO null check, idiot
    // TODO put a protocol package with JSON parsing, validation of each message
    private Socket connection;

    private ConnectionHandler(Socket connection) {
        this.connection = connection;
    }
    
    static ConnectionHandler make(Socket connection) {
        return new ConnectionHandler(connection);
    }

    public void run() {
        try {
            // TODO
        }
        // TODO catch other exceptions
        catch (Exception x) {
            // TODO
        }
    }

    private void handle(String requestMessage) {
        JsonObject convertedMessage = (JsonObject)(new Gson().toJsonTree(requestMessage));
        String req = convertedMessage.get("request").getAsString();
        if (req.equals("login") || req.equals("register"))
            handleLoginRequest(convertedMessage);
        else
            handleSessionRequest(convertedMessage);
        // TODO close connection if not done?
    }

    private void handleLoginRequest(JsonObject msg) {
        String name = msg.get("name").getAsString(),
                pwd = msg.get("pwd").getAsString();
        if (msg.get("request").getAsString().equals("login")) {
            String validUserQuery = String.format(
                    "select uid from User where name = %s && pwd = %s;",
                    name,
                    pwd
            );
            try {
                List<Object[]> userResults = DBUtils.runQuery(validUserQuery, false);
                if (userResults.size() != 0) {
                    int uid = (Integer)(userResults.get(0)[0]);
                    String existingSessionQuery = String.format(
                            "select * from Session where uid = %d;",
                            uid
                    );
                    List<Object[]> sessionResults = DBUtils.runQuery(existingSessionQuery, false);
                    if (sessionResults.size() == 0) {
                        login(uid);
                    }
                    else {
                        // TODO send message that login failed - user already logged in
                    }
                }
                else {
                    // TODO send message that login failed - bad name/pwd combination
                }
            }
            catch (DBUtils.DBException x) {
                // TODO send message that we failed to login, try again later
            }
        }
        else {
            // TODO handle registration (don't login yet)
            // TODO get email: String email = msg.get("email").getAsString();
        }
    }

    private void login(int uid) throws DBUtils.DBException {
        // insert the session record into the db
        int[] results = DBUtils.runUpdate(String.format(
                "insert into Session (uid, skey) values (%d, %x);",
                uid,
                DBUtils.generateSessionKey()
        ));
        if (results[0] == 1) {
            // TODO send success message with sesh key
        }
        else {
            // TODO send failure message - user already logged in
        }
    }

    private void handleSessionRequest(JsonObject msg) {

    }
}
