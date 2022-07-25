package com.icedberries.UBFunkeysServer.ArkOne;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ArkOneParser {

    public static ArrayList<String> ParseReceivedMessage(String xmlCommand) {
        List<String> rawCommandsList = Arrays.stream(xmlCommand.split("\0"))
                .filter(str -> !isNullOrWhitespace(str))
                .collect(Collectors.toList());

        return new ArrayList<>(rawCommandsList);
    }

    public static Node ParseCommand(String command) throws Exception {
        // Check to see if the command has a routing string at the end of it
        if (command.endsWith("#")) {
            command = command.substring(0, command.lastIndexOf(">") + 1);
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new InputSource(new StringReader(command)));
        doc.getDocumentElement().normalize();

        return doc.getFirstChild();
    }

    public static String RemoveXMLTag(Document doc) throws TransformerException {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString().replaceAll("(<\\?xml.*?\\?>)","");
    }

    public static boolean isNullOrWhitespace(String s) {
        return s == null || isWhitespace(s);
    }

    private static boolean isWhitespace(String s) {
        int length = s.length();
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                if (!Character.isWhitespace(s.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static Node findParentNodeOfPath(NodeList nList, String nodePath) {
        ArrayList<String> path = new ArrayList<>(Arrays.asList(nodePath.split("/")));

        if (path.size() <= 0) {
            return null;
        }

        if (path.size() > 1) {
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeName().equals(path.get(0))) {
                    path.remove(0);
                    return findParentNodeOfPath(node.getChildNodes(), String.join("/", path));
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
