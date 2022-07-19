package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.ArkOne.ArkOneSender;
import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.UserService;
import javagrinko.spring.tcp.Connection;
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
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class UserPlugin {

    @Autowired
    Server server;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private ArkOneSender arkOneSender;

    public String RegisterUser(Element element) throws ParserConfigurationException, TransformerException {
        String username = element.getAttribute("l");
        String password = element.getAttribute("p");
        String securityQuestion = element.getAttribute("sq");
        String securityAnswer = element.getAttribute("sa");

        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .securityQuestion(securityQuestion)
                .securityAnswer(passwordEncoder.encode(securityAnswer))
                .build();

        // 0 - Successfully registered
        // 1 - Name already exists
        // 2 - Issues connecting to server
        Integer responseCode = 0;

        String uniqueId = "";

        // First check if username doesn't already exist in the DB
        if (userService.existsByUsername(newUser.getUsername())) {
            // Username already exists
            responseCode = 1;
        } else {
            // Username doesn't exist - save it
            User newUserInDB = userService.save(newUser);

            uniqueId = String.valueOf(newUserInDB.getUUID());
        }

        // Build response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // Create the root element
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("u_reg");

        // Set attributes
        rootElement.setAttribute("r", String.valueOf(responseCode));
        rootElement.setAttribute("u", uniqueId);

        doc.appendChild(rootElement);

        return ArkOneParser.RemoveXMLTag(doc);
    }

    public String GetBuddyList(Connection connection) throws ParserConfigurationException, TransformerException {
        User user = server.getConnectedUsers().get(connection.getClientIdentifier());

        // Start of response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("u_gbl");
        rootElement.setAttribute("r", "0");
        doc.appendChild(rootElement);

        // User should never be null here but check just in case
        // Also return if no buddies
        if (user == null || user.getRawBuddyList() == null) {
            return ArkOneParser.RemoveXMLTag(doc);
        }

        ArrayList<String> buddyList = new ArrayList<>(Arrays.asList(user.getRawBuddyList().split(",")));

        // Skip NULL or blank buddy lists
        if (buddyList.size() > 0) {
            for (String buddy : buddyList) {
                if (buddy.equals("")) { continue; }

                Integer buddyUUID = Integer.valueOf(buddy);

                // Get the buddy's information
                User buddyUser = userService.findByUUID(buddyUUID).orElse(null);

                // Make sure they aren't null
                if (buddyUser == null) {
                    continue;
                }
                //TODO: VERIFY THIS BUILDS THE XML AS EXPECTED
                // Get information off their data to build a xml tag
                Element buddyElement = doc.createElement("buddy");
                buddyElement.setAttribute("id", String.valueOf(buddyUser.getUUID()));
                buddyElement.setAttribute("n", buddyUser.getUsername());
                buddyElement.setAttribute("s", String.valueOf(buddyUser.getChatStatus()));
                buddyElement.setAttribute("o", String.valueOf(buddyUser.getIsOnline()));
                buddyElement.setAttribute("ph", String.valueOf(buddyUser.getPhoneStatus()));

                rootElement.appendChild(buddyElement);
            }
        }

        // The client doesn't set online status.  So the server sets it when it gets all the user's buddies
        user.setIsOnline(1);
        userService.updateUserOnServer(connection, user);

        // Let all your buddies know you are online
        if (buddyList.size() > 0) {
            arkOneSender.SendStatusUpdate("u_cos", "o",
                    String.valueOf(user.getIsOnline()), user.getUUID());
        }

        return ArkOneParser.RemoveXMLTag(doc);
    }

    public String ChangeChatStatus(Element element, Connection connection) throws ParserConfigurationException, TransformerException {
        User user = server.getConnectedUsers().get(connection.getClientIdentifier());

        // Update the chat status
        user.setChatStatus(Integer.valueOf(element.getAttribute("s")));
        userService.updateUserOnServer(connection, user);

        // Build the response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("u_ccs");
        rootElement.setAttribute("s", String.valueOf(user.getChatStatus()));
        rootElement.setAttribute("id", String.valueOf(user.getUUID()));
        doc.appendChild(rootElement);

        // Announce to friends
        arkOneSender.SendStatusUpdate("u_ccs", "s", String.valueOf(user.getChatStatus()), user.getUUID());

        return ArkOneParser.RemoveXMLTag(doc);
    }

    public String ChangePhoneStatus(Element element, Connection connection) throws ParserConfigurationException, TransformerException {
        User user = server.getConnectedUsers().get(connection.getClientIdentifier());

        // Update the phone status
        user.setPhoneStatus(Integer.valueOf(element.getAttribute("ph")));
        userService.updateUserOnServer(connection, user);

        // Build the response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("u_cph");
        rootElement.setAttribute("ph", String.valueOf(user.getPhoneStatus()));
        rootElement.setAttribute("id", String.valueOf(user.getUUID()));
        doc.appendChild(rootElement);

        // Announce to friends
        arkOneSender.SendStatusUpdate("u_cph", "ph", String.valueOf(user.getPhoneStatus()), user.getUUID());

        return ArkOneParser.RemoveXMLTag(doc);
    }

    public String Ping() {
        return "<p t=\"30\" />";
    }
}
