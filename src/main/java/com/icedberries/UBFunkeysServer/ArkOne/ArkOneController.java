package com.icedberries.UBFunkeysServer.ArkOne;

import javagrinko.spring.tcp.Connection;
import javagrinko.spring.tcp.TcpController;
import javagrinko.spring.tcp.TcpHandler;

import java.io.IOException;

@TcpController
public class ArkOneController implements TcpHandler {

    @Override
    public void receiveData(Connection connection, byte[] data) {
        //TODO: IMPLEMENT PLUGIN CHECKING AND FORWARDING HERE
        //Currently just echos back the received data until implemented
        String s = new String(data);
        try {
            connection.send(s.toUpperCase().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectEvent(Connection connection) {
        // Just log connections
        System.out.println("[ArkOne][EVENT] Client connection from: " + connection.getAddress().getCanonicalHostName());
    }

    @Override
    public void disconnectEvent(Connection connection) {
        // Just log disconnections
        System.out.println("[ArkOne][EVENT] Client disconnected: " + connection.getAddress().getCanonicalHostName());
    }
}