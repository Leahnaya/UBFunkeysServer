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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

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
                .chatStatus(0)
                .connectionId("00000000-0000-0000-0000-000000000000")
                .isOnline(0)
                .phoneStatus(0)
                .rawBuddyList("")
                .transactionCount(0)
                .transactionHistory("")
                .jammersUsed(0)
                .jammersTotal(0)
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

    public String AddBuddy(Element element, Connection connection) throws ParserConfigurationException, TransformerException {
        // Try to get a buddy with the username passed
        User buddy = userService.findByUsername(element.getAttribute("n")).orElse(null);
        User thisUser = server.getConnectedUsers().get(connection.getClientIdentifier());

        boolean fail = false;
        boolean isAlreadyBuddy = false;

        if (buddy != null && buddy.getRawBuddyList() != null) {
            // Check if existing buddies
            ArrayList<String> buddyList = new ArrayList<>(Arrays.asList(buddy.getRawBuddyList().split(",")));
            for (String buddyItem : buddyList) {
                if (buddyItem.equals(String.valueOf(buddy.getUUID()))) {
                    isAlreadyBuddy = true;
                    break;
                }
            }
        }

        // Start of response for failure
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("u_abd");

        if (buddy == null) {
            rootElement.setAttribute("r", "2");
            fail = true;
        } else if (buddy.getIsOnline() == 0) {
            rootElement.setAttribute("r", "5");
            fail = true;
        } else if (isAlreadyBuddy) {
            rootElement.setAttribute("r", "3");
            fail = true;
        } else if (buddy.getUsername().equals("") || buddy.getUsername().equals("GUESTUSER")) {
            rootElement.setAttribute("r", "2");
            fail = true;
        }

        rootElement.setAttribute("n", element.getAttribute("n"));
        resp.appendChild(rootElement);

        // Check if failed
        if (fail) {
            return ArkOneParser.RemoveXMLTag(resp);
        } else {
            Document send = dBuilder.newDocument();
            Element sendRoot = send.createElement("u_abr");
            sendRoot.setAttribute("b", String.valueOf(thisUser.getUUID()));
            sendRoot.setAttribute("n", thisUser.getUsername());
            send.appendChild(sendRoot);

            // Send the request to the other user
            arkOneSender.SendToUser(buddy.getConnectionId(), ArkOneParser.RemoveXMLTag(send));

            // Return a value
            return "<notneeded/>";
        }
    }

    public String AddBuddyResponse(Element element, Connection connection) throws ParserConfigurationException,
            TransformerException {
        boolean accepted = false;

        User buddy = userService.findByUsername(element.getAttribute("n")).orElse(null);
        User thisUser = server.getConnectedUsers().get(connection.getClientIdentifier());

        // Start of response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("u_abd");

        // If accepted -> Build response and update DB
        if (element.getAttribute("r").equals("1")) {
            accepted = true;

            rootElement.setAttribute("r", "0");

            // Append to each buddy list the other users id
            String modifiedBuddyListOne;
            if (buddy.getRawBuddyList() == null || buddy.getRawBuddyList().equals("")) {
                modifiedBuddyListOne = String.valueOf(thisUser.getUUID());
            } else {
                modifiedBuddyListOne = buddy.getRawBuddyList() + "," + thisUser.getUUID();
            }
            buddy.setRawBuddyList(modifiedBuddyListOne);
            userService.updateUserOnServer(buddy.getConnectionId(), buddy);

            String modifiedBuddyListTwo;
            if (thisUser.getRawBuddyList() == null || thisUser.getRawBuddyList().equals("")) {
                modifiedBuddyListTwo = String.valueOf(buddy.getUUID());
            } else {
                modifiedBuddyListTwo = thisUser.getRawBuddyList() + "," + buddy.getUUID();
            }
            thisUser.setRawBuddyList(modifiedBuddyListTwo);
            userService.updateUserOnServer(thisUser.getConnectionId(), thisUser);

            rootElement.setAttribute("b", String.valueOf(buddy.getUUID()));
            rootElement.setAttribute("ph", String.valueOf(buddy.getPhoneStatus()));
            rootElement.setAttribute("s", String.valueOf(buddy.getChatStatus()));
            rootElement.setAttribute("o", String.valueOf(buddy.getIsOnline()));
        }

        // If accepted -> Build message to send to other original user
        Document send = dBuilder.newDocument();
        Element sendRootElement = send.createElement("u_abd");
        if (element.getAttribute("r").equals("1")) {
            sendRootElement.setAttribute("r", "0");
            sendRootElement.setAttribute("ph", String.valueOf(thisUser.getPhoneStatus()));
            sendRootElement.setAttribute("s", String.valueOf(thisUser.getChatStatus()));
            sendRootElement.setAttribute("o", String.valueOf(thisUser.getIsOnline()));
        } else {
            sendRootElement.setAttribute("r", "4");
        }

        sendRootElement.setAttribute("b", String.valueOf(thisUser.getUUID()));
        sendRootElement.setAttribute("n", thisUser.getUsername());
        send.appendChild(sendRootElement);

        // Send to that user
        arkOneSender.SendToUser(buddy.getConnectionId(), ArkOneParser.RemoveXMLTag(send));

        rootElement.setAttribute("n", element.getAttribute("n"));
        resp.appendChild(rootElement);

        // Send response
        if (accepted) {
            return ArkOneParser.RemoveXMLTag(resp);
        } else {
            return "<notneeded/>";
        }
    }

    public String SendPrivateMessage(Element element) throws ParserConfigurationException, TransformerException {
        // Start of response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("u_spm");
        rootElement.setAttribute("r", "0");
        rootElement.setAttribute("m", element.getAttribute("m"));
        rootElement.setAttribute("t", element.getAttribute("t"));
        rootElement.setAttribute("f", element.getAttribute("f"));
        resp.appendChild(rootElement);

        UUID connID = UUID.randomUUID();

        User buddy = userService.findByUUID(Integer.valueOf(element.getAttribute("t"))).orElse(null);

        // If the user is online, grab their connection id to send the message
        if (buddy.getIsOnline() == 1) {
            connID = buddy.getConnectionId();
        }

        arkOneSender.SendToUser(connID, ArkOneParser.RemoveXMLTag(resp));

        return ArkOneParser.RemoveXMLTag(resp);
    }

    public String DeleteBuddy(Element element, Connection connection) throws ParserConfigurationException, TransformerException {
        User buddy = userService.findByUUID(Integer.valueOf(element.getAttribute("b"))).orElse(null);
        User thisUser = server.getConnectedUsers().get(connection.getClientIdentifier());

        boolean fail = false;

        // Start of response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document respFail = dBuilder.newDocument();
        Element rootElement = respFail.createElement("u_dbd");

        if (buddy.getUsername().equals("") || buddy.getUsername().equals("GUESTUSER")) {
            fail = true;
            rootElement.setAttribute("r", "2");
        }
        rootElement.setAttribute("u", String.valueOf(thisUser.getUUID()));
        rootElement.setAttribute("b", element.getAttribute("b"));
        respFail.appendChild(rootElement);

        if (fail) {
            return ArkOneParser.RemoveXMLTag(respFail);
        } else {
            // Send a remove message to the buddy if they are online
            if (buddy.getIsOnline() == 1) {
                Document send = dBuilder.newDocument();
                Element sendRoot = send.createElement("u_dbr");
                sendRoot.setAttribute("b", String.valueOf(thisUser.getUUID()));
                sendRoot.setAttribute("n", thisUser.getUsername());
                send.appendChild(sendRoot);

                // Send
                arkOneSender.SendToUser(buddy.getConnectionId(), ArkOneParser.RemoveXMLTag(send));
            }

            // Remove the users from each other's buddy list
            ArrayList<String> buddyList = new ArrayList<>(Arrays.asList(buddy.getRawBuddyList().split(",")));
            buddyList.remove(String.valueOf(thisUser.getUUID()));
            if (buddyList.size() > 0) {
                buddy.setRawBuddyList(String.join(",", buddyList));
            } else {
                buddy.setRawBuddyList("");
            }
            if (buddy.getIsOnline() == 1) {
                userService.updateUserOnServer(buddy.getConnectionId(), buddy);
            } else {
                userService.save(buddy);
            }


            ArrayList<String> buddyList2 = new ArrayList<>(Arrays.asList(thisUser.getRawBuddyList().split(",")));
            buddyList2.remove(String.valueOf(buddy.getUUID()));
            if (buddyList2.size() > 0) {
                thisUser.setRawBuddyList(String.join(",", buddyList2));
            } else {
                thisUser.setRawBuddyList("");
            }
            userService.updateUserOnServer(thisUser.getConnectionId(), thisUser);

            Document resp = dBuilder.newDocument();
            Element respRoot = resp.createElement("u_dbd");
            respRoot.setAttribute("r", "0");
            respRoot.setAttribute("u", String.valueOf(thisUser.getUUID()));
            respRoot.setAttribute("b", element.getAttribute("b"));
            resp.appendChild(respRoot);

            // Return response
            return ArkOneParser.RemoveXMLTag(resp);
        }
    }

    public String DeleteBuddyResponse(Element element, Connection connection) throws ParserConfigurationException,
            TransformerException {
        User thisUser = server.getConnectedUsers().get(connection.getClientIdentifier());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("u_dbd");

        rootElement.setAttribute("r", "0");
        rootElement.setAttribute("u", String.valueOf(thisUser.getUUID()));
        rootElement.setAttribute("b", element.getAttribute("b"));

        resp.appendChild(rootElement);

        return ArkOneParser.RemoveXMLTag(resp);
    }

    public String Ping(Connection connection) {
        User user = server.getConnectedUsers().get(connection.getClientIdentifier());

        user.setLastPing(LocalDateTime.now());

        userService.updateUserOnServer(connection, user);

        return "<p t=\"30\" />";
    }
}
