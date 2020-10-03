package com.learce.client.main;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ConnectionFactory {

    public static Connection createConnection(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            //todo: handle exception
        }
        Connection connection = new Connection(socket);
        connection.start();
        return connection;
    }
}
