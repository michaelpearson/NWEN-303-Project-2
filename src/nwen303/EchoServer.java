package nwen303;/*
 * An echo server listening on port passed as an argument. This server reads
 * from the client and echoes back the result. When the client enters the
 * string "bye", the server closes the connection.
 */

import nwen303.task1.Connection;

import java.net.*;
import java.io.*;

public class EchoServer {
    public static void main(String[] args) throws IOException {

        ServerSocket sock = null;

        try {
            // establish the socket
            sock = new ServerSocket(1025);
            System.out.println("Waiting for connections on " + sock.getLocalPort());

            /*
             * listen for new connection requests.
             * when a request arrives, pass the socket to
             * a separate thread and resume listening for
             * more requests.
             * creating a separate thread for each new request
             * is known as the "thread-per-message" approach.
             */
            while (true) {
                // now listen for connections
                Socket client = sock.accept();

                // service the connection in a separate thread
                Connection c = new Connection(client);
                c.start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (sock != null)
                sock.close();
        }
    }
}
