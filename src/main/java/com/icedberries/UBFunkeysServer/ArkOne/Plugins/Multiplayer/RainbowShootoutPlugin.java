package com.icedberries.UBFunkeysServer.ArkOne.Plugins.Multiplayer;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.ArkOne.ArkOneSender;
import com.icedberries.UBFunkeysServer.domain.Multiplayer.RainbowShootout;
import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.RainbowShootoutService;
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
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RainbowShootoutPlugin {

    @Autowired
    Server server;

    @Autowired
    UserService userService;

    @Autowired
    ArkOneSender arkOneSender;

    @Autowired
    RainbowShootoutService rainbowShootoutService;

    public String JoinGame(Element element, Connection connection)
            throws InterruptedException, ParserConfigurationException, TransformerException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        boolean exists = true;
        int challenge = 0;
        int challenger = 0;
        connection.setTeamSide(5);

        String c = element.getAttribute("c");
        String pr = element.getAttribute("pr");

        // See if there is an existing entry for this user in this table
        User thisUser = server.getConnectedUsers().get(connection.getClientIdentifier());
        RainbowShootout rainbowShootout = rainbowShootoutService.findByUserId(thisUser.getUUID()).orElse(null);

        // If exists, grab the details or set to not exist yet
        if (rainbowShootout != null) {
            challenge = rainbowShootout.getChallenge();
            challenger = rainbowShootout.getChallenger();
        } else {
            exists = false;
        }

        // Insert a new matchmaking entry
        if (!exists || challenger == 0) {
            // Build the new entry
            RainbowShootout newRS = RainbowShootout.builder()
                    .username(thisUser.getUsername())
                    .userId(thisUser.getUUID())
                    .challenge(Integer.valueOf(c))
                    .playerInfo(pr)
                    .ready(0)
                    .score(0)
                    .build();

            // Can't be set as part of the builder
            newRS.setConnectionId(thisUser.getConnectionId());

            // Save to the DB
            rainbowShootoutService.save(newRS);
        }

        // Random Matchmaking
        if (c.equals("0") && challenge != 1) {
            String opponentName = "";
            String opponentInfo = "";
            Integer isPlayerFound = 0;
            int i = 0;

            // Time to look for a match
            while (i < 30) {
                // Get open players
                List<RainbowShootout> openPlayers = rainbowShootoutService.findOtherOpenPlayers(thisUser.getUUID());
                if (openPlayers.size() > 0) {
                    // Get a random open one
                    Random rand = new Random();
                    RainbowShootout randomElement = openPlayers.get(rand.nextInt(openPlayers.size()));

                    // Save information about the opponent
                    connection.setOpponentUID(randomElement.getUserId());
                    connection.setOpponentConID(randomElement.getConnectionId());
                    opponentName = randomElement.getUsername();
                    opponentInfo = randomElement.getPlayerInfo();

                    // Get my data
                    RainbowShootout myRS = rainbowShootoutService.findByUserId(thisUser.getUUID()).orElse(null);
                    if (myRS != null) {
                        isPlayerFound = myRS.getChallenge();
                    }
                }

                // See if we found an opponent
                if (connection.getOpponentConIDAsString().equals("") && isPlayerFound == 0) {
                    TimeUnit.SECONDS.sleep(1);
                    i++;
                } else {
                    i = 30;
                }
            }

            // If player found an opponent
            if (!connection.getOpponentConIDAsString().equals("")) {
                // Update the matchmaking entries to reflect the found match
                RainbowShootout myRS = rainbowShootoutService.findByUserId(thisUser.getUUID()).orElse(null);
                RainbowShootout oppRS = rainbowShootoutService.findByUserId(connection.getOpponentUID()).orElse(null);

                if (myRS != null) {
                    myRS.setChallenger(connection.getOpponentUID());
                }
                if (oppRS != null) {
                    oppRS.setChallenger(thisUser.getUUID());
                }
                rainbowShootoutService.save(myRS);
                rainbowShootoutService.save(oppRS);

                // Build the response to send to the opponent
                Document resp1 = dBuilder.newDocument();
                Element rootElement = resp1.createElement("h5_0");
                resp1.appendChild(rootElement);
                Element ojElement = resp1.createElement("oj");
                ojElement.setAttribute("n", thisUser.getUsername());
                ojElement.setAttribute("pr", pr);
                rootElement.appendChild(ojElement);

                arkOneSender.SendToUser(connection.getOpponentConIDAsUUID(), ArkOneParser.RemoveXMLTag(resp1));

                // Build the response to this user
                Document resp2 = dBuilder.newDocument();
                Element rootElement2 = resp2.createElement("h5_0");
                resp2.appendChild(rootElement2);
                Element jnElement = resp2.createElement("jn");
                jnElement.setAttribute("r", "0");
                rootElement2.appendChild(jnElement);
                Element ojElement2 = resp2.createElement("oj");
                ojElement2.setAttribute("n", opponentName);
                ojElement2.setAttribute("pr", opponentInfo);
                rootElement2.appendChild(ojElement2);

                return ArkOneParser.RemoveXMLTag(resp2);
            }
            // If found by another player
            else if (isPlayerFound == 1) {
                RainbowShootout myRS = rainbowShootoutService.findByUserId(thisUser.getUUID()).orElse(null);
                if (myRS != null) {
                    connection.setOpponentUID(myRS.getChallenger());

                    RainbowShootout oppRS = rainbowShootoutService.findByUserId(connection.getOpponentUID()).orElse(null);

                    if (oppRS != null) {
                        connection.setOpponentConID(oppRS.getConnectionId());
                    }
                }

                return "<mm_found />";
            }
            // Matchmaking timed out
            else {
                return "<mm_timeout />";
            }
        }

        // If joining from invite
        if (challenge == 1) {
            String conID = "";
            String opponentName = "";
            String opponentInfo = "";

            User opponent = userService.findByUUID(challenger).orElse(null);
            if (opponent != null) {
                opponentName = opponent.getUsername();
                if (opponent.getIsOnline() == 1) {
                    conID = opponent.getConnectionId().toString();
                }
            }

            RainbowShootout myRS = rainbowShootoutService.findByUserId(thisUser.getUUID()).orElse(null);
            if (myRS != null) {
                opponentInfo = myRS.getChallengerInfo();

                RainbowShootout oppRS = rainbowShootoutService.findByUserId(challenger).orElse(null);
                if (oppRS != null) {
                    oppRS.setChallengerInfo(pr);
                    myRS.setConnectionId(connection.getClientIdentifier());

                    rainbowShootoutService.save(oppRS);
                    rainbowShootoutService.save(myRS);
                }
            }

            connection.setOpponentConID(UUID.fromString(conID));
            connection.setOpponentUID(challenger);

            // Build the response to send to the opponent
            Document resp1 = dBuilder.newDocument();
            Element rootElement = resp1.createElement("h5_0");
            resp1.appendChild(rootElement);
            Element ojElement = resp1.createElement("oj");
            ojElement.setAttribute("n", thisUser.getUsername());
            ojElement.setAttribute("pr", pr);
            rootElement.appendChild(ojElement);

            arkOneSender.SendToUser(connection.getOpponentConIDAsUUID(), ArkOneParser.RemoveXMLTag(resp1));

            // Build the response to this user
            Document resp2 = dBuilder.newDocument();
            Element rootElement2 = resp2.createElement("h5_0");
            resp2.appendChild(rootElement2);
            Element jnElement = resp2.createElement("jn");
            jnElement.setAttribute("r", "0");
            rootElement2.appendChild(jnElement);
            Element ojElement2 = resp2.createElement("oj");
            ojElement2.setAttribute("n", opponentName);
            ojElement2.setAttribute("pr", opponentInfo);
            rootElement2.appendChild(ojElement2);

            return ArkOneParser.RemoveXMLTag(resp2);
        }

        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("h5_0");
        resp.appendChild(rootElement);
        Element jnElement = resp.createElement("jn");
        jnElement.setAttribute("r", "0");
        rootElement.appendChild(jnElement);
        return ArkOneParser.RemoveXMLTag(resp);
    }

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

    public String BlockShot(Element element, Connection connection) throws ParserConfigurationException, TransformerException {
        boolean blocked = false;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document response = dBuilder.newDocument();
        Element rootElement = response.createElement("h5_0");
        response.appendChild(rootElement);

        Element bsElement = response.createElement("bs");
        bsElement.setAttribute("d", element.getAttribute("d"));
        bsElement.setAttribute("lx", element.getAttribute("lx"));
        bsElement.setAttribute("m", element.getAttribute("m"));
        bsElement.setAttribute("c", element.getAttribute("c"));
        bsElement.setAttribute("bid", element.getAttribute("bid"));
        rootElement.appendChild(bsElement);

        // For some reason, the Block Shot commmand is in charge of setting the score.
        switch(element.getAttribute("c")) {
            case "0": // Missed (Above net)
            case "1": // Blocked
            case "3": // Missed (Beside net)
                blocked = true;
                break;
        }

        RainbowShootout myRS = rainbowShootoutService.findByUserId(server.getConnectedUsers().get(
                connection.getClientIdentifier()).getUUID()).orElse(null);
        RainbowShootout oppRS = rainbowShootoutService.findByUserId(connection.getOpponentUID()).orElse(null);

        if (myRS  != null && oppRS != null) {
            if (blocked) {
                myRS.setScore(myRS.getScore() + 1);
                rainbowShootoutService.save(myRS);
            } else {
                oppRS.setScore(myRS.getScore() + 1);
                rainbowShootoutService.save(oppRS);
            }

            Element psElement = response.createElement("ps");
            psElement.setAttribute("s", String.valueOf(oppRS.getScore()));
            rootElement.appendChild(psElement);

            Element osElement = response.createElement("os");
            osElement.setAttribute("s", String.valueOf(myRS.getScore()));
            rootElement.appendChild(osElement);
        }

        arkOneSender.SendToUser(connection.getOpponentConIDAsUUID(), ArkOneParser.RemoveXMLTag(response));

        Document response1 = dBuilder.newDocument();
        Element rootElement1 = response1.createElement("h5_0");
        response1.appendChild(rootElement1);

        Element psElement = response1.createElement("ps");
        assert myRS != null;
        psElement.setAttribute("s", String.valueOf(myRS.getScore()));
        rootElement1.appendChild(psElement);

        Element osElement = response1.createElement("os");
        assert oppRS != null;
        osElement.setAttribute("s", String.valueOf(oppRS.getScore()));
        rootElement1.appendChild(osElement);

        return ArkOneParser.RemoveXMLTag(response1);
    }
}
