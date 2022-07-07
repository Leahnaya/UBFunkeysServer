package com.icedberries.UBFunkeysServer.Galaxy;

import com.icedberries.UBFunkeysServer.service.EmailService;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

@RestController
public class GalaxyServer {

    @Autowired
    private EmailService emailService;

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
                //TODO: MIGHT ALSO NEED TO SET THE FILE CONTENT LENGTH AS A HEADER
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
        try {
            // Parse the xml body of the request
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlBody)));
            doc.getDocumentElement().normalize();

            // Get the root element
            String root = doc.getDocumentElement().getNodeName();

            // Handle based on the root element
            switch(root) {
                case "postcard":
                    return sendPostcard(doc);
                case "savecrib":
                    return saveCrib(doc);
                case "loadcrib":
                    return loadCrib(doc);
            default:
                System.out.println("[Galaxy][POST][ERROR] Unhandled type of request for: " + root);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            System.out.println("[Galaxy][POST][ERROR] Thrown Error: ");
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //TODO: USE THIS FOR THE XML PARSING: https://www.tutorialspoint.com/java_xml/java_dom_parse_document.htm#

    //TODO: USE THIS FOR THE ARKONE SERVER: https://github.com/JavaGrinko/tcp-spring-boot-starter

    private ResponseEntity<String> loadCrib(Document request) {
        System.out.println("[Galaxy][POST] loadcrib request received");
        //TODO: IMPLEMENT METHOD
        return null;
    }

    private ResponseEntity<String> saveCrib(Document request) {
        System.out.println("[Galaxy][POST] savecrib request received");
        //TODO: IMPLEMENT METHOD
        return null;
    }

    private ResponseEntity<String> sendPostcard(Document request) {
        System.out.println("[Galaxy][POST] postcard request received");
        //TODO: PARSE OUT DATA FROM REQUEST

        // Parse data from the request
        String to = "cockatoo242@gmail.com";
        String subject = "Greetings From Funkiki Island";
        String body = "Wow, check out this awesome postcard!";
        String fileName = "card_0.jpg";

        // Try to send the postcard to the email
        String response = "";
        if (emailService.sendMailWithAttachment(to, subject, body, fileName)) {
            // Send successful
            response = "<postcard result=\"0\" reason=\"Postcard Sent!\" cost=\"5\" />";
        } else {
            response = "<postcard result=\"1\" reason=\"Unable to send postcard at this time!\" cost=\"0\" />";
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
