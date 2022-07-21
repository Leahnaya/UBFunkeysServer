package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneController;
import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.UserService;
import javagrinko.spring.tcp.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BasePlugin {

    @Autowired
    Server server;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String PORT = "20502";

    public String LoginGuestUser() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // Create the root element
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("a_lgu");

        // Set attributes
        rootElement.setAttribute("r", "0");
        rootElement.setAttribute("u", "0");
        rootElement.setAttribute("n", "GUESTUSER");
        rootElement.setAttribute("p", "");
        rootElement.setAttribute("s", "1");

        doc.appendChild(rootElement);

        return ArkOneParser.RemoveXMLTag(doc);
    }

    public String GetServiceDetails(String s) throws ParserConfigurationException, TransformerException {
        // Set some variables for the response
        String serverID = "1";

        String xIPAddress = ArkOneController.IP_ADDRESS;
        String xPort = "80";

        String bIPAddress = ArkOneController.IP_ADDRESS;
        String bPort = "80";

        // Update the port for each server
        // ** This would be used if each routing plugin had their own server/port combo **
        switch (s){
            //User
            case "1":
                xPort = PORT;
                bPort = PORT;
                break;

            //Galaxy
            case "7":
                xPort = PORT;
                bPort = PORT;
                break;

            //Trunk
            case "10":
                xPort = PORT;
                bPort = PORT;
                break;
        }

        // Build the response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // Create the root element
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("a_gsd");

        // Set attributes
        rootElement.setAttribute("s", serverID);
        rootElement.setAttribute("xi", xIPAddress);
        rootElement.setAttribute("xp", xPort);
        rootElement.setAttribute("bi", bIPAddress);
        rootElement.setAttribute("bp", bPort);

        doc.appendChild(rootElement);

        return ArkOneParser.RemoveXMLTag(doc);
    }

    public String GetPluginDetails(String p) throws ParserConfigurationException, TransformerException {
        // Set some variables for the response
        String serverID = "1";

        String xIPAddress = ArkOneController.IP_ADDRESS;
        String xPort = "80";

        String bIPAddress = ArkOneController.IP_ADDRESS;
        String bPort = "80";

        // Update the port for each plugin
        // ** This would be used if each routing plugin had their own server/port combo **
        switch (p){
            //User
            case "1":
                xPort = PORT;
                bPort = PORT;
                break;

            //Galaxy
            case "7":
                xPort = PORT;
                bPort = PORT;
                break;

            //Trunk
            case "10":
                xPort = PORT;
                bPort = PORT;
                break;
        }

        // Build the response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // Create the root element
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("a_gpd");

        // Set attributes
        rootElement.setAttribute("s", serverID);
        rootElement.setAttribute("xi", xIPAddress);
        rootElement.setAttribute("xp", xPort);
        rootElement.setAttribute("bi", bIPAddress);
        rootElement.setAttribute("bp", bPort);
        rootElement.setAttribute("p", p);

        doc.appendChild(rootElement);

        return ArkOneParser.RemoveXMLTag(doc);
    }

    public String LoginRegisteredUser(Element element, UUID connectionId) throws ParserConfigurationException, TransformerException {
        // Response r codes:
        // 0 - Accepted Login
        // 1 - Already exist in Terrapinia
        // 2 - Already exist in Terrapinia
        // 3 - Problem with your account, please call phone number
        // 4 - Password Incorrect
        // 5 - Funkey Name Not Found
        Integer responseCode = 0;

        String username = element.getAttribute("n");
        String password = element.getAttribute("p");

        // Get a funkey with that username
        User user = userService.findByUsername(username).orElse(null);
        String uuid = "";

        // Check if null (no funkey with that name)
        if (user == null) {
            responseCode = 5;
        } else {
            // Funkey name exists - Verify Password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                // Passwords don't match
                responseCode = 4;
            } else {
                // Login success

                // Check if user already logged in
                if (user.getIsOnline() == 1) {
                    responseCode = 2;
                } else {
                    // User not in game
                    uuid = String.valueOf(user.getUUID());

                    // Store the UUID for our connection to the server to the DB
                    user.setConnectionId(connectionId);

                    // Set their last ping time to now
                    user.setLastPing(LocalDateTime.now());

                    // Set they are online now
                    user.setChatStatus(0);
                    user.setIsOnline(1);

                    // Save to local map and update DB
                    userService.updateUserOnServer(connectionId, user);
                }
            }
        }

        // Build response
        // Build the response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // Create the root element
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("a_lru");

        // Set attributes
        rootElement.setAttribute("s", "1");
        rootElement.setAttribute("r", String.valueOf(responseCode));
        if (responseCode == 0) {
            // Only set the user id field if successfully found the user
            rootElement.setAttribute("u", uuid);
        }

        doc.appendChild(rootElement);

        return ArkOneParser.RemoveXMLTag(doc);
    }
}
