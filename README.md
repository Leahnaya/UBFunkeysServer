# Unofficial UB Funkeys Server

This is an unofficial fan project to recreate the UB Funkeys servers to allow for things like multiplayer, the Funkey Trunk and chat rooms.

This server is made using Java, Spring Boot and Maven in Java 8.

*If someone wants to update the Java version, be my guest*

```
Lauthai started this server while working in tandem with Lako, who was developing in their own code.  Both servers have many of the same features available, but there are a few that haven't been ported over to this server (Lucky, Sprocket, Snipe and Dyer's games), and will need to be ported at some point by someone.
```

## Project Features
The following lists will help keep track of what features are implemented, partial working, and not yet implemented.

### Working
The following items are currently working and implemented:

#### ArkOne Server
* Plugin 0 - Core
  * Login Guest User
  * Login Registered Users
* Plugin 1 - User
  * Create Account
  * Add Friends
  * PM Friends
  * Invite Player
  * Remove Friends
* Plugin 7 - Galaxy
  * Saving User Profile (save game)
  * Load Profile
* Plugin 10 - Trunk
  * Loot
  * Familiars
  * Jammers
  * Moods
  * Cleanings
  * Items
  * Splashes
  * Transaction History

#### Galaxy Server
* Sending files for updates
* Sending Postcards*
* Saving Cribs
* Loading Cribs
* Getting Shared Levels (Ace/Mulch's Games)
* Sharing Ace/Mulch Games

*see the deployment and testing section for getting this working

### Partially Working
The following items are currently partially implemented:

***Please note these features are currently incomplete and may break your game if you attempt to use them - USE AT YOUR OWN RISK***

#### ArkOne Server
* Plugin 5 - Rainbow Shootout
  * Started migrating Lako's code but majority is still commented out across ArkOneController and MultiplayerPlugin.  Still needs to be finished implementing
* Plugin 7 - Galaxy
  * Leaderboard
    * Missing Most Played (multiplayer games)

#### Galaxy Server
* Challenging Friends to Ace/Mulch Levels
  * Currently sends a game response back but causes the game to only display a black screen.  Needs investigation into why get_level_info doesn't work properly.

### Not Yet Implemented
The following items are not yet implemented:

***TRYING TO ACCESS/USE ANY OF THESE FEATURES HAVE A MUCH HIGHER LIKELYHOOD OF CRASHING YOUR GAME - DO NOT ATTEMPT TO ACCESS WHILE USING THE SERVER***

* Plugin 2 - Chat Rooms
* Plugin 4 - Jongg Challenge
* Plugin 6 - Bombastic Billiards
* Plugin 8 - Funkey Fighters
* Plugin 9 - Checkers
* Plugin 11 - Rasteroids
* Plugin 12 - Lilytadd Tiles

Features from Lako's Server that need to be migrated:
* Sprocket's Game
* Snipe's Game
  * Partially Working
* Dyer's Game
  * Partially Working


## Deployment and Testing

### Prepping the application.properties

#### Database Configuration
This server makes use of an external database so one will need to be configured in the application.properties prior to use.  Make sure to set the following fields to match what your database needs:
```
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=
```

#### Postcard Configuration
In order to send Post cards, you will need to supply the server with credentials for a mail server to send the emails from.  Fill in the following fields:
```
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=
spring.mail.password=
```
^ Note that you will need to change the host and port if you use something other than GMail.

### Local Testing
To get the server to run locally, make use of a Spring Boot deployment run configuration with the main class being UBFunkeysServerApplication.java

### Building the Server
This project makes use of Maven for building.  To package the server into a deployable war file, traverse to the root directory and run the following Maven command:
```
mvn clean install -U
```

### Deploying the Server
The .war file can be deployed using something like Tomcat to host the webapp.  The server will need 2 ports open to it:

* 80 - HTTP
* 20502 - TCP

## Contributing

We welcome people to open pull requests as you complete and implement any of the missing/partially working features!  The more that people help out, the faster we can restore every feature that once existed!

## BUGS?!

If you find a bug, please report it here on GitHub so that we can track it to ensure it is properly taken care of.

## Credits
As people contribute to the development of the server they will be added here:

* Lauthai
* Lako
