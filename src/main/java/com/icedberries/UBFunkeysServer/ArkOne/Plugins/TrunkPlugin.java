package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.domain.Familiar;
import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.FamiliarService;
import com.icedberries.UBFunkeysServer.service.FileService;
import com.icedberries.UBFunkeysServer.service.UserService;
import javagrinko.spring.tcp.Connection;
import javagrinko.spring.tcp.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class TrunkPlugin {

    private final Integer LOOT_BALANCE = 2500;

    private enum PurchaseType {
        FAMILIAR
    }

    @Autowired
    Server server;

    @Autowired
    FileService fileService;

    @Autowired
    FamiliarService familiarService;

    @Autowired
    UserService userService;

    public String GetUserAssets(Connection connection) throws ParserConfigurationException, IOException, SAXException {
        //TODO: IMPLEMENT ITEMS | MOODS | JAMMERS
        // Moods - Tag looks like this: <m id="80041a" />

        // Append the starting tags
        StringBuilder response = new StringBuilder();
        response.append("<h10_0><gua>");

        // Load the users save
        Resource resource = fileService.load(server.getConnectedUsers().get(connection.getClientIdentifier()).getUsername() + "/profile");

        // Load their save to a string
        if (resource != null) {
            String content;
            try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
                content = FileCopyUtils.copyToString(reader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document profile = dBuilder.parse(new InputSource(new StringReader(content)));
            profile.getDocumentElement().normalize();

            // Append the familiars
            Node familiarParentNode = ArkOneParser.findParentNodeOfPath(profile.getChildNodes(), "profile/trunk/familiars");
            for (int i = 0; i < familiarParentNode.getChildNodes().getLength(); i++) {
                Element child = (Element) familiarParentNode.getChildNodes().item(i);

                String id = child.getAttribute("id");
                String p = child.getAttribute("start");
                String cnt = child.getAttribute("time");

                // cnt -> time
                // p -> start
                // id -> item id
                //TODO: FINISH FIXING THIS
                response.append("<f id=\"" + id + "\" p=\"" + (27645298/60) + "\" c=\"" + 720 + "\" />");
            }
        }

        // Append the ending tags
        response.append("</gua></h10_0>");

        return response.toString();
    }

    public String GetLootBalance() {
        // Return a static balance that never changes
        return "<h10_0><glb b=\"" + LOOT_BALANCE + "\" /></h10_0>";
    }

    public String GetFamiliarsList() {

        // Get all the familiars
        List<Familiar> familiars = familiarService.findAll();

        // Start to build the response
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h10_0><gfl>");

        // Iterate over the items in the familiars list to add them to the response
        for (Familiar familiar : familiars) {
            stringBuilder.append("<f rid=\"" + familiar.getId() + "\" id=\"" + familiar.getId() + "\" c=\""
                    + familiar.getCost() + "\" dc=\"" + familiar.getDiscountedCost() + "\" h=\""
                    + familiar.getDuration() + "\" d=\"\" />");
        }

        // Add closing tags
        stringBuilder.append("</gfl></h10_0>");

        // Return the list
        return stringBuilder.toString();
    }

    public String GetUserTransactionsCount(Connection connection) {
        User user = server.getConnectedUsers().get(connection.getClientIdentifier());

        // Make sure a valid user was gotten
        if (user != null) {
            int transactionCount = 0;
            // Make sure the transaction count has been set before
            if (user.getTransactionCount() != null) {
                // They have a transaction count
                transactionCount = user.getTransactionCount();
            } else {
                // Initialize it to 0
                user.setTransactionCount(0);

                // Update their account
                userService.updateUserOnServer(connection, user);
            }
            return "<h10_0><gutc c=\"" + transactionCount + "\" /></h10_0>";
        } else {
            return "<h10_0><gutc c=\"0\" /></h10_0>";
        }
    }

    public String GetUserTransactions(Connection connection) {
        User user = server.getConnectedUsers().get(connection.getClientIdentifier());

        if (user.getTransactionHistory() != null) {
            return "<h10_0><gut>" + user.getTransactionHistory() + "</gut></h10_0>";
        } else {
            return "<h10_0><gut></gut></h10_0>";
        }
    }

    public String BuyFamiliar(Element element, Connection connection) {
        // Save this transaction to the DB
        PostTransaction(connection, PurchaseType.FAMILIAR, element.getAttribute("id"));

        // We always return LOOT_BALANCE so players are never charged for these items
        return "<h10_0><bf id=\"" + element.getAttribute("id") + "\" b=\"" + LOOT_BALANCE + "\" /></h10_0>";
    }

    public void PostTransaction(Connection connection, PurchaseType purchaseType, String itemId) {
        User user = server.getConnectedUsers().get(connection.getClientIdentifier());

        // Get the date for this transaction
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        String date = now.format(formatter);

        // Get the cost of the item
        Integer cost = 0;
        switch(purchaseType) {
            case FAMILIAR:
                cost = familiarService.getCostById(itemId);
                break;
        }

        // Create a transaction xml tag
        String transaction = "<t id=\"" + itemId + "\" rid=\"" + itemId + "\" d=\"" + date + "\" c=\""
                + cost + "\" b=\"" + LOOT_BALANCE + "\" />";

        // Append to the user's transaction history
        if(user.getTransactionHistory() == null) {
            // No history yet
            user.setTransactionHistory(transaction);
        } else {
            // Append to the existing history
            user.setTransactionHistory(user.getTransactionHistory() + transaction);
        }

        // Save transaction
        userService.updateUserOnServer(connection, user);

        // Increment the transaction count for this user
        IncrementTransactionCount(connection);
    }

    private void IncrementTransactionCount(Connection connection) {
        User user = server.getConnectedUsers().get(connection.getClientIdentifier());

        int newTransCount = user.getTransactionCount() + 1;

        user.setTransactionCount(newTransCount);

        // Update their account
        userService.updateUserOnServer(connection, user);
    }
}
