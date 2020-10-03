package com.learce.client.main;

import com.learce.client.util.ConnectionState;

import java.io.BufferedOutputStream;
import java.net.Socket;

public class Connection {

    private Socket socket;
    private ConnectionState connectionState;

    public Connection(Socket socket) {
        this.socket = socket;
        connectionState = ConnectionState.NEW;
    }

    public Channel createChannel() {
        return null;
    }

    public void start() {

    }
}
