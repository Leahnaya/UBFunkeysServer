package com.icedberries.UBFunkeysServer.ArkOne;

import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.UserService;
import javagrinko.spring.tcp.Connection;
import javagrinko.spring.tcp.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Service
public class ArkOneSender {

    @Autowired
    Server server;

    @Autowired
    private UserService userService;

    public void SendStatusUpdate(String statusHeader, String shortHeader, String status, Integer userUUID)
            throws ParserConfigurationException, TransformerException {
        // No need to announce with no buddies
        if (userService.getBuddyList(userUUID) == null) {
            return;
        }
        ArrayList<String> buddyList = new ArrayList<>(Arrays.asList(userService.getBuddyList(userUUID).split(",")));

        for (String buddy : buddyList) {
            if (!buddy.equals("")) {
                int isOnline = 0;
                UUID connectionId = null;

                User buddyUser = userService.findByUUID(Integer.valueOf(buddy)).orElse(null);

                if (buddyUser != null) {
                    isOnline = buddyUser.getIsOnline();
                    if (isOnline == 1) {
                        connectionId = buddyUser.getConnectionId();

                        // Build announcement
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.newDocument();
                        Element rootElement = doc.createElement(statusHeader);
                        rootElement.setAttribute(shortHeader, status);
                        rootElement.setAttribute("id", String.valueOf(userUUID));
                        doc.appendChild(rootElement);

                        // Send announcement to user
                        SendToUser(connectionId, ArkOneParser.RemoveXMLTag(doc));
                    }
                }
            }
        }
    }

    public void SendToUser(UUID clientId, String message) {
        for (Connection conn : server.getConnections()) {
            if (conn.getClientIdentifier().equals(clientId)) {
                try {
                    // Append a 0x00 to the end of the response
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write(message.getBytes());
                    outputStream.write((byte)0x00);

                    conn.send(outputStream.toByteArray());

                    return;
                } catch (IOException e) {
                    System.out.println("[ArkOne][ERROR] Failed to send message to user [" + clientId + "]: " + message);
                    e.printStackTrace();
                }
            }
        }
    }
}
