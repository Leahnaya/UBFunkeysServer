package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

@Service
public class UserPlugin {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

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

    public String Ping() {
        return "<p t=\"30\" />";
    }
}
