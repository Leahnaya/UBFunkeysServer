package com.icedberries.UBFunkeysServer.ArkOne.Plugins.Multiplayer;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.ArkOne.ArkOneSender;
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

@Service
public class MultiplayerPlugin {

    @Autowired
    Server server;

    @Autowired
    UserService userService;

    @Autowired
    private ArkOneSender arkOneSender;

    public String MessageOpponent(Element element, Connection connection, String plugin)
            throws ParserConfigurationException, TransformerException {
        // Start building the response with the plugin tag
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("h" + plugin + "_0");
        resp.appendChild(rootElement);

        // Get a reference to the calling user
        User thisUser = server.getConnectedUsers().get(connection.getClientIdentifier());

        // Create the message element
        Element msElement = resp.createElement("ms");
        msElement.setAttribute("n", thisUser.getUsername());
        msElement.setAttribute("m", element.getAttribute("m"));
        msElement.setAttribute("bid", element.getAttribute("bid"));
        rootElement.appendChild(msElement);

        // Send the message to the other user
        User buddy = userService.findByUUID(Integer.valueOf(element.getAttribute("bid"))).orElse(null);
        if (buddy != null) {
            arkOneSender.SendToUser(buddy.getConnectionId(), ArkOneParser.RemoveXMLTag(resp));
        }

        return ArkOneParser.RemoveXMLTag(resp);
    }

    //public String LeaveGame(String plugin) {

    //}
}
