// TODO multithread so i can tell dispatcher to stop or something
package rbfs.server;

import java.io.*;
import java.net.*;
import java.util.function.Function;

/**
 Listens for new connections and assigns a thread to handle each one.
 @author James Hoak
 @version 1.0
 */
final class Dispatcher {

    /* The socket this Dispatcher uses to listen for incoming connections. */
    private ServerSocket socket;

    /* A method for getting a thread that will handle an incoming connection. */
    private Function<Socket, Thread> getConnectionHandler;

    /**
     * Creates a new Dispatcher. It will listen for connections on the given socket and requisition
     * a new thread for each new connection. The Dispatcher will not run until run() is called.
     * @param socket The ServerSocket on which the Dispatcher will listen for connections
     * @param getConnectionHandler A method to get a new Thread that will handle an incoming
     * connection
     */
    private Dispatcher(ServerSocket socket, Function<Socket, Thread> getConnectionHandler) {
        this.socket = socket;
        this.getConnectionHandler = getConnectionHandler;
    }

    /**
     * Creates a new Dispatcher. It will not run until run() is called.
     * @param getConnectionHandler A method that returns a Thread that will handle a given
     * connection.
     * @return A new Dispatcher that may be started with its run() method.
     * @throws FailedInitException If the Dispatcher fails to initialize. Common reasons include
     * passing a null value for the getConnectionHandler method, as well as passing a bad port.
     */
    static Dispatcher makeDispatcher(Function<Socket, Thread> getConnectionHandler)
    throws FailedInitException {
        if (getConnectionHandler == null)
            throw new FailedInitException("Failed to make Dispatcher (null getConnectionHandler)");
        Integer port = Config.getPort();
        try {
            ServerSocket skt = new ServerSocket(port);
            return new Dispatcher(skt, getConnectionHandler);
        }
        catch (IOException x) {
            throw new FailedInitException("Failed to make Dispatcher (could not init on port).");
        }
        catch (Exception x) {
            throw new FailedInitException("Failed to make Dispatcher. Error: " + x.getMessage());
        }
    }

    /**
     * Starts the Dispatcher. It will start accepting connections on its socket and passing them to
     * threads via getConnectionHandler.
     */
    void run() {
        // TODO log dispatcher start and other stuff here
        while (true) {
            try {
                Socket connection = socket.accept();
                Thread handler = getConnectionHandler.apply(connection);
                handler.start();
            }
            catch (IOException x) {
                // TODO log IO error here. but keep going though
            }
            catch (Exception x) {
                // TODO log crash here
                break;
            }
        }
        // TODO log exit?
    }

    /**
     * An exception that describes a failure to initialize a new Dispatcher.
     * @author James Hoak
     * @version 1.0
     */
    static class FailedInitException extends Exception {
        /**
         * Constructs a new exception with the given message.
         * @param message The exception message.
         */
        private FailedInitException(String message) { super(message); }
    }
}