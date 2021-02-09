package com.learce.client.main;

import com.learce.client.util.ConnectionChannel;
import com.learce.client.util.ConnectionStatus;

import java.net.Socket;

public class Connection {

    private final Socket socket;
    private ConnectionStatus connectionStatus;
    private final ConnectionChannel connectionChannel;

    public Connection(Socket socket) {
        this.socket = socket;
        this.connectionChannel = new ConnectionChannel(this);
        connectionStatus = ConnectionStatus.NEW;
    }

    public Channel createChannel() {
        return new Channel(this);
    }

    public void start() {
        connectionChannel.startConnection();
        connectionStatus = ConnectionStatus.OPEN_OK;
    }

    public void close() {
        connectionChannel.closeConnection();
        connectionStatus = ConnectionStatus.CLOSE_OK;
    }
}
