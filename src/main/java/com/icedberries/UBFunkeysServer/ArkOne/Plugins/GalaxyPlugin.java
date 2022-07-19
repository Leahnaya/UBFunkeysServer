package com.icedberries.UBFunkeysServer.ArkOne.Plugins;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.service.FileService;
import javagrinko.spring.tcp.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.StringReader;

@Service
public class GalaxyPlugin {

    @Autowired
    FileService fileService;

    public String LoadProfileVersion() {
        //TODO: IMPLEMENT THIS AFTER PROFILE SAVING
        return "<h7_0><lpv /></h7_0>";
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
}
