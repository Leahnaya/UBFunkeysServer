package com.icedberries.UBFunkeysServer.ArkOne.Plugins.Multiplayer;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.ArkOne.ArkOneSender;
import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.UserService;
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
public class RainbowShootoutPlugin {

    @Autowired
    Server server;

    @Autowired
    UserService userService;

    @Autowired
    private ArkOneSender arkOneSender;

    public String ShotParameters(Element element) throws ParserConfigurationException,
            TransformerException {
        // Start building the response with the plugin tag
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("h5_0");
        resp.appendChild(rootElement);

        // Create the message element
        Element spElement = resp.createElement("sp");
        spElement.setAttribute("p", element.getAttribute("p"));
        spElement.setAttribute("z", element.getAttribute("z"));
        spElement.setAttribute("y", element.getAttribute("y"));
        spElement.setAttribute("x", element.getAttribute("x"));
        spElement.setAttribute("bid", element.getAttribute("bid"));
        rootElement.appendChild(spElement);

        // Send the message to the other user
        User buddy = userService.findByUUID(Integer.valueOf(element.getAttribute("bid"))).orElse(null);
        if (buddy != null) {
            arkOneSender.SendToUser(buddy.getConnectionId(), ArkOneParser.RemoveXMLTag(resp));
        }

        return "<notneeded/>";
    }

    public String CharacterMove(Element element) throws ParserConfigurationException,
            TransformerException {
        // Start building the response with the plugin tag
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("h5_0");
        resp.appendChild(rootElement);

        // Create the message element
        Element cmElement = resp.createElement("cm");
        cmElement.setAttribute("x", element.getAttribute("x"));
        cmElement.setAttribute("d", element.getAttribute("d"));
        cmElement.setAttribute("bid", element.getAttribute("bid"));
        rootElement.appendChild(cmElement);

        // Send the message to the other user
        User buddy = userService.findByUUID(Integer.valueOf(element.getAttribute("bid"))).orElse(null);
        if (buddy != null) {
            arkOneSender.SendToUser(buddy.getConnectionId(), ArkOneParser.RemoveXMLTag(resp));
        }

        return "<notneeded/>";
    }
}
