package com.icedberries.UBFunkeysServer.Galaxy;

import com.icedberries.UBFunkeysServer.ArkOne.ArkOneParser;
import com.icedberries.UBFunkeysServer.domain.Crib;
import com.icedberries.UBFunkeysServer.domain.Level;
import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.CribService;
import com.icedberries.UBFunkeysServer.service.EmailService;
import com.icedberries.UBFunkeysServer.service.FileService;
import com.icedberries.UBFunkeysServer.service.LevelService;
import com.icedberries.UBFunkeysServer.service.UserService;
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
import org.springframework.web.bind.annotation.PutMapping;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
public class GalaxyServer {

    @Autowired
    private EmailService emailService;

    @Autowired
    private CribService cribService;

    @Autowired
    private LevelService levelService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    /**
     * This is only used as part of the updater to pass files to the client as requested via URL path
     */
    @GetMapping("/**")
    public void GalaxyGetResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathToFile = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        // Change the file path to use spaces
        pathToFile = StringUtils.removeStart(pathToFile.replace("%20", " "), "/");

        System.out.println("[Galaxy][GET] Request for file: " + pathToFile);

        int errorCode = 0;

        // THIS IF CHECK IS PROBABLY NOT SAFE IF UPDATES ARE BEING DONE AS WELL
        // See if the requested file is a gamemaker image file
        if (pathToFile.startsWith("data/") && pathToFile.endsWith(".jpg")) {
            byte[] imageData = null;

            // Remove the "data/" since it already exists within fileService
            pathToFile = pathToFile.replace("data/", "");

            // See if the requested level image exists
            Resource resource;
            if (fileService.fileExists(pathToFile)) {
                // The file exists - Load the file to the stream
                resource = fileService.load(pathToFile);
            } else {
                // The file doesn't exist - Load the default image
                resource = fileService.load("DEFAULT.jpg");
            }

            try {
                // Copy the bytes to a byte[]
                imageData = FileCopyUtils.copyToByteArray(resource.getInputStream());
            } catch (IOException e) {
                // Print the error to the log but let the data be null, so we return a bad data response
                System.out.println("[Galaxy][GET] Exception thrown when loading image: ");
                e.printStackTrace();
            }

            // Verify that something was loaded
            if (imageData == null) {
                System.out.println("[Galaxy][GET][ERROR] Game maker image not found!");
                errorCode = HttpServletResponse.SC_NOT_FOUND;
                response.sendError(errorCode);
                return;
            }

            // Now that the image has been loaded we can send it back
            // The file exists - Load the file to the stream
            InputStream fileContentStream = new ByteArrayInputStream(imageData);

            // Copy the stream to the response's output stream
            IOUtils.copy(fileContentStream, response.getOutputStream());

            // Flush the buffer
            // MIGHT ALSO NEED TO SET THE FILE CONTENT LENGTH AS A HEADER
            response.flushBuffer();
            System.out.println("[Galaxy][GET] Game maker image sent!");
            return;
        }

        Resource resource = new ClassPathResource("static/UpdateFiles/" + pathToFile);
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

    @PutMapping("/**")
    public void GalaxyPutResponse(@RequestBody byte[] request, HttpServletRequest servletRequest) {
        System.out.println("[Galaxy][PUT] Saving image file: " + servletRequest.getRequestURI());

        fileService.saveGameMakerImage(request, servletRequest.getRequestURI().replace("data/", ""));
    }

    /**
     * Handler for all POST Requests
     * Postcards, LoadCrib and SaveCrib
     */
    @PostMapping("/")
    public ResponseEntity<String> GalaxyPostResponse(@RequestBody String xmlBody) {
        // Log the request
        System.out.println("[Galaxy][POST] Request: " + xmlBody);

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
            ResponseEntity<String> response;
            switch(command) {
                case "postcard":
                    response = sendPostcard((Element)nodes.item(0));
                    break;
                case "savecrib":
                    response = saveCrib(nodes.item(0));
                    break;
                case "loadcrib":
                    response = loadCrib((Element)nodes.item(0));
                    break;
                case "get_sh_levels":
                    response = getShLevels((Element)nodes.item(0));
                    break;
                case "get_level":
                    response = getLevel((Element)nodes.item(0));
                    break;
                case "add_level":
                    response = addLevel((Element)nodes.item(0));
                    break;
                case "save_level":
                    response = saveLevel((Element)nodes.item(0));
                    break;
                case "get_top":
                    response = getTop((Element)nodes.item(0));
                    break;
                case "end_level":
                    response = endLevel((Element)nodes.item(0));
                    break;
                case "search":
                    response = search((Element)nodes.item(0));
                    break;
                case "get_level_info":
                    response = getLevelInfo((Element)nodes.item(0));
                    break;
                default:
                    System.out.println("[Galaxy][POST][ERROR] Unhandled type of request for: " + command);
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            // Log the response
            System.out.println(response.getBody());

            // Return to the client
            return response;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            //System.out.println("[Galaxy][POST][ERROR] Thrown Error: ");
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<String> loadCrib(Element element) {
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

    // ACE AND MULCH's GAMES RESPONSE CODES
    // 0 - Success
    // 1 - User Not Authorized
    // 2 - There was a problem with your request, try again later
    // 3 - Name already exists on server
    // 4 - Cannot find a game with that name

    private ResponseEntity<String> getShLevels(Element element) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<get_sh_levels r=\"0\" >");

        List<Level> sharedLevels = levelService.getLevelsByGameNameAndUserId(element.getAttribute("gn"), Integer.valueOf(element.getAttribute("uid")));

        if (sharedLevels.isEmpty()) {
            // No shared levels
            stringBuilder.append("<level n=\"\" />");
        } else {
            // Add their shared levels to the list
            stringBuilder.append("<level />");
            for (Level level : sharedLevels) {
                User creator = userService.findByUUID(level.getUserId()).orElse(null);
                String creatorName = creator != null ? creator.getUsername() : "UNKNOWN";

                String levelData = "<level id=\"" + level.getId() + "\" sh=\"1\" ver=\"1\" n=\"" + level.getLevelName()
                        + "\" v=\"" + level.getPlayCount() + "\" un=\"" + creatorName + "\" r=\"" + level.getRating()
                        + "\" tnurl=\"" + level.getImagePath() + "\" pos=\"" + level.getPos() + "\"/>";
                stringBuilder.append(levelData);
            }
        }
        stringBuilder.append("</get_sh_levels>");

        return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.OK);
    }

    private ResponseEntity<String> getLevel(Element element) {
        Integer levelId = Integer.valueOf(element.getAttribute("id"));
        Level level = levelService.findLevelById(levelId).orElse(null);

        String levelData = level == null ? "<level tnurl=\"\" />" : level.getLevelData();

        if (level == null) {
            return new ResponseEntity<>("<get_level r=\"0\">" + levelData + "</get_level>", HttpStatus.OK);
        }
        return new ResponseEntity<>("<get_level r=\"0\">" + levelData + "</get_level>", HttpStatus.OK);
    }

    private ResponseEntity<String> addLevel(Element element) {
        int responseCode = 0;

        String levelName = element.getAttribute("n");
        String gameName = element.getAttribute("gn");

        // Check to see if the level with that name already is saved on the server
        if (levelService.existsByLevelNameAndGameName(levelName, gameName)) {
            //TODO: ADD A CHECK TO OVERRIDE IF SAME PERSON (NOT SURE IF ALLOWED AT ALL ANYWAYS)
            responseCode = 3;
        }

        // Return to the client
        return new ResponseEntity<>("<add_level r=\"" + responseCode + "\" n=\"" + levelName
                + "\" gn=\"" + gameName + "\" ></add_level>", HttpStatus.OK);
    }

    private ResponseEntity<String> saveLevel(Element element) {
        int responseCode = 0;

        String levelName = element.getAttribute("n");
        String tnurl = element.getAttribute("tnurl");
        String gameName = element.getAttribute("gn");
        Integer uid = Integer.valueOf(element.getAttribute("uid"));

        String levelData = "";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document newDocument = builder.newDocument();
            Node importedNode = newDocument.importNode(element.getFirstChild(), true);
            newDocument.appendChild(importedNode);

            levelData = ArkOneParser.RemoveXMLTag(newDocument);
        } catch (ParserConfigurationException | TransformerException e) {
            System.out.println("[Galaxy][POST] Exception thrown when saving crib: ");
            e.printStackTrace();
            responseCode = 1;
        }

        // If the level couldn't be parsed via the try-catch above, don't attempt to save it
        Integer levelId = 0;
        if (responseCode != 1) {
            Level level = Level.builder()
                    .userId(uid)
                    .levelName(levelName)
                    .gameName(gameName)
                    .levelData(levelData)
                    .sharedDate(LocalDateTime.now())
                    .imagePath(tnurl)
                    .rating(0)
                    .ratingCount(0)
                    .playCount(0)
                    .pos(0)
                    .build();

            Level savedLevel = levelService.save(level);

            if (savedLevel == null) {
                responseCode = 2;
            } else {
                levelId = savedLevel.getId();
            }
        }

        //TODO: MIGHT NEED TO UPDATE TO ACCOUNT FOR UPDATING ALREADY UPLOADED LEVELS
        return new ResponseEntity<>("<save_level r=\"" + responseCode + "\" n=\"" + levelName +  "\" tnurl=\"" + tnurl + "\" id=\"" + levelId
                + "\" gn=\"" + gameName + "\" uid=\"" + uid + "\" />", HttpStatus.OK);
    }

    private ResponseEntity<String> getTop(Element element) {
        // Get the game name
        String gameName = element.getAttribute("gn");
        String type = element.getAttribute("t");
        int count = Integer.parseInt(element.getAttribute("c"));

        List<Level> allLevelsByGame = levelService.findAllByGameName(gameName);
        List<Level> filteredLevels = new ArrayList<>();

        int maxSize = Math.min(count, allLevelsByGame.size());
        // Switch based on what type of getTop it is
        List<Level> sortedLevels;
        switch (type) {
            case "b":
                // Best Levels
                sortedLevels = allLevelsByGame.stream()
                        .sorted(Comparator.comparing(Level::getRating))
                        .collect(Collectors.toList());
                Collections.reverse(sortedLevels);
                break;
            case "r":
                // Random Level
                sortedLevels = allLevelsByGame;
                if (!sortedLevels.isEmpty()) {
                    Collections.shuffle(sortedLevels);
                }
                break;
            case "l":
                // Latest Levels
                sortedLevels = allLevelsByGame.stream()
                        .sorted(Comparator.comparing(Level::getSharedDate))
                        .collect(Collectors.toList());
                Collections.reverse(sortedLevels);
                break;
            default:
                // Unhandled type
                System.out.println("[Galaxy][POST] Unhandled type: " + type);
                sortedLevels = new ArrayList<>();
                break;
        }

        // Filter down to the top X levels
        for (int i = 0; i < maxSize; i++) {
            filteredLevels.add(sortedLevels.get(i));
        }

        StringBuilder stringBuilder = new StringBuilder();
        String openingTag = "<get_top r=\"0\" c=\"" + maxSize + "\" t=\"" + type + "\" gn=\"" + gameName + "\">";
        stringBuilder.append(openingTag);

        if (filteredLevels.isEmpty()) {
            // No levels match
            stringBuilder.append("<level n=\"\" />");
        } else {
            // Add all the found levels to the response
            for (Level level : filteredLevels) {
                User creator = userService.findByUUID(level.getUserId()).orElse(null);
                String creatorName = creator != null ? creator.getUsername() : "UNKNOWN";

                String levelData = "<level id=\"" + level.getId() + "\" uid=\"" + level.getUserId() + "\" sh=\"1\" ver=\"1\" n=\"" + level.getLevelName()
                        + "\" v=\"" + level.getPlayCount() + "\" un=\"" + creatorName + "\" r=\"" + level.getRating()
                        + "\" tnurl=\"" + level.getImagePath() + "\" pos=\"" + level.getPos() + "\"/>";
                stringBuilder.append(levelData);
            }
        }

        stringBuilder.append(("</get_top>"));

        return new ResponseEntity<>(stringBuilder.toString(), HttpStatus.OK);
    }

    private ResponseEntity<String> endLevel(Element element) {
        Integer levelId = Integer.valueOf(element.getAttribute("id"));
        Level level = levelService.findLevelById(levelId).orElse(null);

        if (level != null) {
            // INCREMENT PLAY COUNT
            level.setPlayCount(level.getPlayCount() + 1);

            // UPDATE RATING
            // Get the current level ratings
            Integer ratingCount = level.getRatingCount();
            Double rating = level.getRatingRaw();

            // Grab the rating submitted by the user
            Integer submittedRating = Integer.valueOf(element.getAttribute("rat"));

            // Calculate the new rating
            double newRating = rating + submittedRating;
            Integer newRatingCount = ratingCount + 1;

            // Store them back into the level object
            level.setRating(newRating);
            level.setRatingCount(newRatingCount);
            level.setPos(level.getPos() + submittedRating);

            // RE-SAVE THE LEVEL
            levelService.save(level);
        }

        String uid = element.getAttribute("uid");
        String id = element.getAttribute("id");
        String response = "<end_level r=\"0\" end=\"1\" uid=\"" + uid + "\" id=\"" + id + "\" />";
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<String> search(Element element) {
        StringBuilder response = new StringBuilder();

        // Add the starting tag
        String openingTag = "<search r=\"0\">";
        response.append(openingTag);

        String gameName = element.getAttribute("gn");
        String searchType = element.getAttribute("t");
        String searchKeyword = element.getAttribute("w");

        List<Level> searchResults;
        switch(searchType) {
            case "k": // KEYWORD
                searchResults = levelService.findAllByGameNameAndKeyword(gameName, searchKeyword);
                break;
            case "a": // AUTHOR
                searchResults = levelService.findAllByGameNameAndAuthor(gameName, searchKeyword);
                break;
            case "g": // LEVEL NAME
                searchResults = levelService.findAllByGameNameAndLevelName(gameName, searchKeyword);
                break;
            default:
                System.out.println("[GALAXY][POST] Unhandled Game Maker search type: " + searchType);
                searchResults = new ArrayList<>();
                break;
        }

        for (Level level : searchResults) {
            User creator = userService.findByUUID(level.getUserId()).orElse(null);
            String creatorName = creator != null ? creator.getUsername() : "UNKNOWN";

            String levelData = "<level id=\"" + level.getId() + "\" sh=\"1\" ver=\"1\" n=\"" + level.getLevelName()
                    + "\" v=\"" + level.getPlayCount() + "\" un=\"" + creatorName + "\" r=\"" + level.getRating()
                    + "\" tnurl=\"" + level.getImagePath() + "\" pos=\"" + level.getPos() + "\"/>";
            response.append(levelData);
        }

        // Add the closing tag
        response.append("</search>");

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    /**
     * Get the level info for a level by ID
     * NOTES:
     * - This method is only used when challenging a friend
     * - The level should always exist from here as it must've been shared to challenge a friend
     * @param element the request element extracted from the XML
     * @return level info for the requested level
     */
    private ResponseEntity<String> getLevelInfo(Element element) {
        Integer levelId = Integer.valueOf(element.getAttribute("id"));
        Level level = levelService.findLevelById(levelId).orElse(null);

        String levelData = "";
        try {
            User creator = userService.findByUUID(level.getUserId()).orElse(null);
            String creatorName = creator != null ? creator.getUsername() : "UNKNOWN";
            levelData += "<level id=\"" + level.getId() + "\" sh=\"1\" ver=\"1\" n=\"" + level.getLevelName()
                    + "\" v=\"" + level.getPlayCount() + "\" un=\"" + creatorName + "\" r=\"" + level.getRating()
                    + "\" tnurl=\"" + level.getImagePath() + "\" pos=\"" + level.getPos() + "\"/>";
        }
        catch (NullPointerException e) {
            return new ResponseEntity<>("<get_level_info r=\"0\" id=\"" + levelId + "\"><level tnurl=\"\" /></get_level_info>", HttpStatus.OK);
        }
        return new ResponseEntity<>("<get_level_info r=\"0\" id=\"" + levelId + "\">" + levelData + "</get_level_info>", HttpStatus.OK);
    }
}
