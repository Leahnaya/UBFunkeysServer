package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.service.FileService;
import javagrinko.spring.tcp.Connection;
import javagrinko.spring.tcp.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class GalaxyPlugin {

    @Autowired
    Server server;

    @Autowired
    FileService fileService;

    public String LoadProfileVersion(Connection connection) throws ParserConfigurationException, IOException,
            SAXException, TransformerException {
        // Start of response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("h7_0");
        resp.appendChild(rootElement);

        Element subElement = resp.createElement("lpv");

        // Check if this user has a save
        if (fileService.fileExists(server.getConnectedUsers().get(connection.getClientIdentifier()).getUsername() + "/profile")) {
            Resource resource = fileService.load(server.getConnectedUsers().get(connection.getClientIdentifier()).getUsername() + "/profile");

            // Load their save to a string
            String content;
            try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
                content = FileCopyUtils.copyToString(reader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            // Parse the current save
            Document save = dBuilder.parse(new InputSource(new StringReader(content)));
            save.getDocumentElement().normalize();
            Element rootSave = (Element)save.getFirstChild();

            String saveID = rootSave.getAttribute("sid");
            subElement.setAttribute("v", saveID);
        }
        resp.appendChild(subElement);

        // Build response
        return ArkOneParser.RemoveXMLTag(resp);
    }

    public String VersionStatisticsRequest() {
        return "<h7_0><vsu id=\"0\" /></h7_0>";
    }

    public String SaveProfile(Element element, Connection connection) {
        // Set the number of chunks left to save
        //TODO: VERIFY THIS ATTRIBUTE NAME
        connection.setChunksLeft(Integer.valueOf(element.getAttribute("c")));

        // Clear save data
        connection.setSaveData("");

        return "<h7_0><rr /></h7_0>";
    }

    public String SaveProfilePart(Element element, Connection connection) throws ParserConfigurationException,
            TransformerException, IOException, SAXException {
        // Start of response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("h7_0");
        resp.appendChild(rootElement);

        // Continue appending to the saveData
        //TODO: VERIFY THIS ELEMENT ATTRIBUTE
        connection.setSaveData(element.getAttribute("v") + connection.getSaveData());

        if (connection.getChunksLeft() == 1) {
            Element subElement = resp.createElement("sp");

            // Parse the current save
            Document save = dBuilder.parse(new InputSource(new StringReader(connection.getSaveData())));
            save.getDocumentElement().normalize();
            Element rootSave = (Element)save.getFirstChild();

            String profileName = rootSave.getAttribute("gname");
            if (!rootSave.getAttribute("sid").equals("")) {
                subElement.setAttribute("v", String.valueOf(Integer.parseInt(rootSave.getAttribute("sid")) + 1));
            } else {
                subElement.setAttribute("v", "1");
            }

            // Write to file
            fileService.save(new MockMultipartFile("profile", "profile", "text/xml", connection.getSaveData().getBytes()),
                    profileName);

            rootElement.appendChild(subElement);
        } else {
            Element subElement = resp.createElement("rr");
            rootElement.appendChild(subElement);

            // Decrease the number of chunks by 1
            connection.setChunksLeft(connection.getChunksLeft() - 1);
        }

        // Build response
        return ArkOneParser.RemoveXMLTag(resp);
    }

    public String LoadProfile(Connection connection) {
        Resource resource = fileService.load(server.getConnectedUsers().get(connection.getClientIdentifier()).getUsername() + "/profile");

        // Load their save to a string
        String content;
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            content = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return "<h7_0>" + content + "</h7_0>";
    }

    public String GetLeaderboardStats(Element element, Connection connection) throws ParserConfigurationException,
            TransformerException, IOException, SAXException {
        //TODO - When we get multiplayer working, get Most Played (MULTI) added

        Integer category = Integer.valueOf(element.getAttribute("id"));

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document resp = dBuilder.newDocument();
        Element rootElement = resp.createElement("h7_0");
        resp.appendChild(rootElement);

        Element glsElement = resp.createElement("gls");
        glsElement.setAttribute("id", String.valueOf(category));
        rootElement.appendChild(glsElement);

        Element recordsElement = resp.createElement("records");
        recordsElement.setAttribute("id", String.valueOf(category));
        glsElement.appendChild(recordsElement);

        Resource resource = fileService.load(server.getConnectedUsers().get(connection.getClientIdentifier()).getUsername() + "/profile");

        // Load their save to a string
        String content;
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            content = FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Document profile = dBuilder.parse(new InputSource(new StringReader(content)));
        profile.getDocumentElement().normalize();
        switch(category) {
            case 1:
                Node gameNodes = findChildNodeByName(profile.getChildNodes(), "profile/statistics/games/game");
                for (int i = 0; i < gameNodes.getChildNodes().getLength(); i++) {
                    Element record = resp.createElement("record");

                    Element child = (Element)gameNodes.getChildNodes().item(i);
                    record.setAttribute("id", child.getAttribute("id"));
                    record.setAttribute("sp", child.getAttribute("count"));

                    recordsElement.appendChild(record);
                }
                break;
            case 2:
                Node itemNodes = findChildNodeByName(profile.getChildNodes(), "profile/menu/items/item");
                for (int i = 0; i < itemNodes.getChildNodes().getLength(); i++) {
                    Element record = resp.createElement("record");

                    Element child = (Element)itemNodes.getChildNodes().item(i);
                    record.setAttribute("id", child.getAttribute("id"));
                    record.setAttribute("c", child.getAttribute("total"));

                    recordsElement.appendChild(record);
                }
                break;
            default:
                System.out.println("[ArkOne][ERROR] gls had a category value of: " + category);
                break;
        }

        return ArkOneParser.RemoveXMLTag(resp);
    }

    private Node findChildNodeByName(NodeList nList, String nodePath) {
        ArrayList<String> path = new ArrayList<>(Arrays.asList(nodePath.split("/")));

        if (path.size() <= 0) {
            return null;
        }

        if (path.size() > 1) {
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeName().equals(path.get(0))) {
                    path.remove(0);
                    return findChildNodeByName(node.getChildNodes(), String.join("/", path));
                }
            }
        } else {
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeName().equals(path.get(0))) {
                    return node;
                }
            }
        }
        // Nothing found
        return null;
    }
}
