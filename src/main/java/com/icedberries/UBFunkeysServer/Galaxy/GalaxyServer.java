package com.icedberries.UBFunkeysServer.Galaxy;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.domain.Crib;
import com.icedberries.UBFunkeysServer.service.CribService;
import com.icedberries.UBFunkeysServer.service.EmailService;
import com.icedberries.UBFunkeysServer.service.FileService;
import com.icedberries.UBFunkeysServer.util.RDFUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
public class GalaxyServer {

    @Autowired
    private EmailService emailService;

    @Autowired
    private CribService cribService;

    @Autowired
    FileService fileService;

    /**
     * This is only used as part of the updater to pass files to the client as requested via URL path
     */
    @GetMapping("/**")
    public void GalaxyGetResponse(HttpServletRequest request, HttpServletResponse response) {
        String pathToFile = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        // Change the file path to use spaces
        pathToFile = StringUtils.removeStart(pathToFile.replace("%20", " "), "/");

        System.out.println("[Galaxy][GET] Request for file: " + pathToFile);

        Resource resource = new ClassPathResource("static/UpdateFiles/" + pathToFile);
        int errorCode = 0;
        try {
            // Try to open the file as a resource
            byte[] fileContent = org.apache.commons.io.IOUtils.toByteArray(resource.getInputStream());

            // Check if the file exists
            if (fileContent.length > 0) {
                // The file exists - Load the file to the stream
                InputStream fileContentStream = new ByteArrayInputStream(fileContent);

                // Copy the stream to the response's output stream
                IOUtils.copy(fileContentStream, response.getOutputStream());

                // Flush the buffer
                // MIGHT ALSO NEED TO SET THE FILE CONTENT LENGTH AS A HEADER
                response.flushBuffer();
            } else {
                throw new Exception("[Galaxy][GET][ERROR] Requested file doesn't exist: " + pathToFile);
            }
        } catch (IOException e) {
            // File read errors caught here
            System.out.println("[Galaxy][GET][ERROR] Problem loading file: " + pathToFile);
            e.printStackTrace();

            // Set the error code
            errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        } catch (Exception e) {
            // File doesn't exist error caught here
            System.out.println(e.getMessage());
            e.printStackTrace();

            // Set the error code
            errorCode = HttpServletResponse.SC_NOT_FOUND;
        } finally {
            // Send the error code if an error was thrown;
            if (errorCode != 0) {
                try {
                    response.sendError(errorCode);
                } catch (IOException e) {
                    System.out.println("[Galaxy][GET][ERROR] Problem sending error response to client: ");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handler for all POST Requests
     * Postcards, LoadCrib and SaveCrib
     */
    @PostMapping("/")
    public ResponseEntity<String> GalaxyPostResponse(@RequestBody String xmlBody) {
        // Log the request
        System.out.println("[Galaxy][POST] New Request: " + xmlBody);

        try {
            // Parse the xml body of the request
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlBody)));
            doc.getDocumentElement().normalize();

            // Get the element for the command
            NodeList nodes = doc.getDocumentElement().getChildNodes();
            String command = nodes.item(0).getNodeName();

            // Handle based on the root element
            switch(command) {
                case "postcard":
                    return sendPostcard((Element)nodes.item(0));
                case "savecrib":
                    return saveCrib(nodes.item(0));
                case "loadcrib":
                    return loadCrib((Element)nodes.item(0));
                case "get_sh_levels":
                    return getShLevels((Element)nodes.item(0));
                case "get_level":
                    return getLevel((Element)nodes.item(0));
                case "add_level":
                    return addLevel((Element)nodes.item(0));
                default:
                    System.out.println("[Galaxy][POST][ERROR] Unhandled type of request for: " + command);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            System.out.println("[Galaxy][POST][ERROR] Thrown Error: ");
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<String> loadCrib(Element element) {
        System.out.println("[Galaxy][POST] loadcrib request received");

        /*
         * 0 - Success
         * 1 - Error Loading Crib / Can't find a crib with that name (need to test codes)
         */
        int resultCode = 0;
        String reason = "";

        // Get the requested crib name
        String cribName = element.getAttribute("name");

        // Check if looking for a specific crib or a random one
        String profileData = "";
        if (cribName.isEmpty()) {
            // Random Crib Requested ("Surprise Me")

            // Get a total number of all cribs
            Integer cribCount = cribService.count();

            // Ensure there are cribs in the DB
            if (cribCount <= 0) {
                // No cribs in the db
                resultCode = 1;
                reason = "Can't find a crib at this time...";
            } else {
                // At least 1 crib in the db

                // Use that total to generate a random id number between 1 and that number
                Integer randomCribId = ThreadLocalRandom.current().nextInt(1, cribCount + 1);

                // Get the crib
                Crib randomCrib = cribService.findById(randomCribId);

                // Make sure the crib was grabbed properly
                if (randomCrib == null) {
                    // Unable to get the crib
                    resultCode = 1;
                    reason = "Can't find a crib at this time...";
                } else {
                    // Random crib grabbed successfully
                    profileData = randomCrib.getProfileData();
                }
            }
        } else {
            // Specific Crib Requested
            Crib requestedCrib = cribService.findByCribName(cribName);

            // Check if a crib with that name exists
            if (requestedCrib == null) {
                // Crib doesn't exist
                resultCode = 1;
                reason = "Can't find a crib by that name...";
            } else {
                // Crib with that name exists
                profileData = requestedCrib.getProfileData();
            }
        }

        // Build a response
        String response = "<loadcrib result=\"" + resultCode + "\" reason=\"" + reason + "\" currCrib=\"1\" name=\"" + cribName + "\">"
                + profileData
                + "</loadcrib>";

        // Send the response
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<String> saveCrib(Node node) {
        System.out.println("[Galaxy][POST] savecrib request received");

        /*
         * 0 - Success
         * 1 - Error Saving Crib / Crib already exists (need to test codes)
         */
        int resultCode = 0;
        String reason = "Crib Successfully Saved!";

        Element rootElement = (Element)node;
        Node profileNode = node.getFirstChild();
        Element profileElement = (Element)profileNode;

        // Set variables we can use to build the crib later
        String cribName = rootElement.getAttribute("name");
        String username = profileElement.getAttribute("name");
        String currCrib = rootElement.getAttribute("currCrib");

        String profileData = "";

        // Turn the node passed in into its own document to generate the profileData
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document newDocument = builder.newDocument();
            Node importedNode = newDocument.importNode(profileNode, true);
            newDocument.appendChild(importedNode);

            profileData = ArkOneParser.RemoveXMLTag(newDocument);
        } catch (ParserConfigurationException | TransformerException e) {
            System.out.println("[Galaxy][POST] Exception thrown when saving crib: ");
            e.printStackTrace();
            resultCode = 1;
            reason = "Error saving crib! Please try again later...";
        }

        // Build a new Crib if no errors were thrown
        if (resultCode != 1) {
            Crib newCrib = Crib.builder()
                    .cribName(cribName)
                    .username(username)
                    .profileData(profileData)
                    .build();

            // Attempt to save the crib

            // Check if a crib with that name already exists
            if (cribService.existsByCribName(cribName)) {
                // That crib already exists, check if the same username
                Crib existingCrib = cribService.findByCribName(cribName);
                if (existingCrib.getUsername().equals(username)) {
                    // Same username, can update crib
                    existingCrib.setProfileData(profileData);
                    cribService.save(existingCrib);
                } else {
                    // Not the same username, can't save crib
                    resultCode = 1;
                    reason = "Crib name already exists!";
                }
            } else {
                // Crib with that name doesn't exist, save it
                cribService.save(newCrib);
            }
        }

        // Send the response
        String response = "<savecrib result=\"" + resultCode + "\" currCrib=\"" + currCrib + "\" reason=\"" + reason + "\" register=\"1\" name=\"" + cribName + "\" />";
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<String> sendPostcard(Element element) {
        System.out.println("[Galaxy][POST] postcard request received");

        // Parse data from the request
        String to = element.getAttribute("to");
        String subject = element.getAttribute("subject");
        String body = element.getAttribute("body");
        String fileName = element.getAttribute("id");

        // Try to send the postcard to the email
        String response;
        if (emailService.sendMailWithAttachment(to, subject, body, fileName)) {
            // Send successful
            response = "<postcard result=\"0\" reason=\"Postcard Sent!\" cost=\"5\" />";
        } else {
            response = "<postcard result=\"1\" reason=\"Unable to send postcard at this time!\" cost=\"5\" />";
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<String> getShLevels(Element element) throws IOException {
        System.out.println("[Galaxy][POST] get_sh_levels request received");

        //TODO: GET THE LEVELS SAVED ON THE SERVER

        return new ResponseEntity<>("<get_sh_levels r=\"0\"><level n=\"\"><level_data /></level></get_sh_levels>", HttpStatus.OK);
    }

    private ResponseEntity<String> getLevel(Element element) {
        //TODO: DO THIS
        return new ResponseEntity<>("<get_level r=\"0\"><level n=\"\"><level_data /></level></get_level>", HttpStatus.OK);
    }

    private ResponseEntity<String> addLevel(Element element) {
        //TODO: DO THIS
        return new ResponseEntity<>("<add_level r=\"0\"><level n=\"\"><level_data /></level></add_level>", HttpStatus.OK);
    }

    /*
    // Game to get
    String gamePath = element.getAttribute("tnpath").replaceFirst("data/", "");
    // Load the users file
    Resource resource = fileService.load(gamePath + "/uglevels.rdf");
    // Load their save to a string
    String content = "";
    byte[] targetArray = org.apache.commons.io.IOUtils.toByteArray(resource.getInputStream());
    content = RDFUtil.decode(new String(targetArray, StandardCharsets.ISO_8859_1));
    */
}
