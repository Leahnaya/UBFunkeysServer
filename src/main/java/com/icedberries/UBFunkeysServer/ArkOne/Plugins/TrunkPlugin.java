package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.DatabaseSetup.TrunkData;
import com.icedberries.UBFunkeysServer.domain.Familiar;
import com.icedberries.UBFunkeysServer.domain.Jammer;
import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.FamiliarService;
import com.icedberries.UBFunkeysServer.service.FileService;
import com.icedberries.UBFunkeysServer.service.JammerService;
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
        FAMILIAR,
        JAMMER
    }

    @Autowired
    Server server;

    @Autowired
    FileService fileService;

    @Autowired
    FamiliarService familiarService;

    @Autowired
    JammerService jammerService;

    @Autowired
    UserService userService;

    public String GetUserAssets(Connection connection) throws ParserConfigurationException, IOException, SAXException {
        //TODO: IMPLEMENT ITEMS | MOODS
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
                Integer c = Integer.valueOf(child.getAttribute("time"));

                // c -> time (needs to be divided by 60)
                // p -> start
                // id -> item id
                response.append("<f id=\"" + id + "\" p=\"" + p + "\" c=\"" + (c / 60) + "\" />");
            }

            /* Append the jammers
             * NOTE: We have to store this on the profile since the user doesn't properly save jammer
             * counts to the profile data
             */
            User user = server.getConnectedUsers().get(connection.getClientIdentifier());
            Integer p = user.getJammersUsed() != null ? user.getJammersUsed() : 0;
            Integer c = user.getJammersTotal() != null ? user.getJammersTotal() : 0;
            if (c > 0) {
                response.append("<j id=\"" + TrunkData.JAMMER_RID + "\" p=\"" + p + "\" c=\"" + c + "\" />");
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
            stringBuilder.append("<f rid=\"" + familiar.getRid() + "\" id=\"" + familiar.getId() + "\" c=\""
                    + familiar.getCost() + "\" dc=\"" + familiar.getDiscountedCost() + "\" h=\""
                    + familiar.getDuration() + "\" d=\"\" />");
        }

        // Add closing tags
        stringBuilder.append("</gfl></h10_0>");

        // Return the list
        return stringBuilder.toString();
    }

    public String GetJammerList() {

        // Get all the jammers
        List<Jammer> jammers = jammerService.findAll();

        // Start to build the response
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h10_0><gjl>");

        // Iterate over the items in the familiars list to add them to the response
        for (Jammer jammer : jammers) {
            stringBuilder.append("<j rid=\"" + jammer.getRid() + "\" id=\"" + jammer.getId() + "\" c=\""
                    + jammer.getCost() + "\" q=\"" + jammer.getQty() + "\" d=\"\" />");
        }

        // Add closing tags
        stringBuilder.append("</gjl></h10_0>");

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

    public String AssetParam(Element element, Connection connection) {
        /* This method would be used to update the server profile data for when familiars are started (setting start time)
         * Or when you use a jammer
         *
         * For familiars we can trust the profile data saved and load from those
         * For jammers, the profile doesn't update properly so we need to update the jammer used count when received
         */

        // Check for id equal to the jammer rid
        if (element.getAttribute("id").equals("80014a")) {
            User user = server.getConnectedUsers().get(connection.getClientIdentifier());
            Integer newUsed = Integer.valueOf(element.getAttribute("p"));
            user.setJammersUsed(newUsed);
            userService.updateUserOnServer(connection, user);
        }

        // Return this regardless of outcome (I don't believe a response is read from this)
        return "<h10_0><asp /></h10_0>";
    }

    public String BuyFamiliar(Element element, Connection connection) {
        // Save this transaction to the DB
        PostTransaction(connection, PurchaseType.FAMILIAR, Integer.valueOf(element.getAttribute("id")));

        // We always return LOOT_BALANCE so players are never charged for these items
        return "<h10_0><bf id=\"" + element.getAttribute("id") + "\" b=\"" + LOOT_BALANCE + "\" /></h10_0>";
    }

    public String BuyJammer(Element element, Connection connection) {
        // Save this transaction to the DB
        PostTransaction(connection, PurchaseType.JAMMER, Integer.valueOf(element.getAttribute("id")));

        // Increase the amount of jammers a player has in their account
        User user = server.getConnectedUsers().get(connection.getClientIdentifier());
        Integer qtyBought = jammerService.getQtyById(Integer.valueOf(element.getAttribute("id")));
        Integer currentTotal = user.getJammersTotal() != null ? user.getJammersTotal() : 0;
        user.setJammersTotal(currentTotal + qtyBought);
        userService.updateUserOnServer(connection, user);

        // We always return LOOT_BALANCE so players are never charged for these items
        return "<h10_0><bj id=\"" + element.getAttribute("id") + "\" b=\"" + LOOT_BALANCE + "\" /></h10_0>";
    }

    public void PostTransaction(Connection connection, PurchaseType purchaseType, Integer itemId) {
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
            case JAMMER:
                cost = jammerService.getCostById(itemId);
                break;
        }

        // Get the rid of the item
        String rid = "";
        switch(purchaseType) {
            case FAMILIAR:
                rid = familiarService.getRidById(itemId);
                break;
            case JAMMER:
                rid = jammerService.getRidById(itemId);
                break;
        }

        // Create a transaction xml tag
        String transaction = "<t id=\"" + itemId + "\" rid=\"" + rid + "\" d=\"" + date + "\" c=\""
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
