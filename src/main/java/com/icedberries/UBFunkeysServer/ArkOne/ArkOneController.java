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

        // Log the received request
        String xmlData = new String(data);
        System.out.println("[ArkOne] New Request: " + xmlData);

        try {
            connection.send(xmlData.toUpperCase().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectEvent(Connection connection) {
        // No need to log anything
    }

    @Override
    public void disconnectEvent(Connection connection) {
        // No need to log anything
    }
}