package com.icedberries.UBFunkeysServer.ArkOne;

import com.icedberries.UBFunkeysServer.ArkOne.Plugins.BasePlugin;
import com.icedberries.UBFunkeysServer.ArkOne.Plugins.GalaxyPlugin;
import com.icedberries.UBFunkeysServer.ArkOne.Plugins.Multiplayer.MultiplayerPlugin;
import com.icedberries.UBFunkeysServer.ArkOne.Plugins.Multiplayer.RainbowShootoutPlugin;
import com.icedberries.UBFunkeysServer.ArkOne.Plugins.TrunkPlugin;
import com.icedberries.UBFunkeysServer.ArkOne.Plugins.UserPlugin;
import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.UserService;
import javagrinko.spring.tcp.Connection;
import javagrinko.spring.tcp.Server;
import javagrinko.spring.tcp.TcpController;
import javagrinko.spring.tcp.TcpHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TcpController
public class ArkOneController implements TcpHandler {

    public static final String IP_ADDRESS = "127.0.0.1";

    @Autowired
    Server server;

    // Services
    @Autowired
    private UserService userService;

    @Autowired
    private ArkOneSender arkOneSender;

    // Plugins
    @Autowired
    BasePlugin basePlugin;

    @Autowired
    UserPlugin userPlugin;

    @Autowired
    GalaxyPlugin galaxyPlugin;

    @Autowired
    TrunkPlugin trunkPlugin;

    @Autowired
    MultiplayerPlugin multiplayerPlugin;

    @Autowired
    RainbowShootoutPlugin rainbowShootoutPlugin;

    @Override
    public void receiveData(Connection connection, byte[] data) {
        // Log the received request
        String xmlData = new String(data);
        System.out.println("[ArkOne] New Request: " + xmlData);

        // Create a list of responses to send back
        ArrayList<String> responses = new ArrayList<>();

        // Parse the incoming data into individual commands
        List<String> commands = ArkOneParser.ParseReceivedMessage(xmlData);

        // Handle each command
        for (String command : commands) {
            // Remove the null character from the end if it exists
            command = command.replace("\0", "");

            // Parse out the plugin from the routing string (if it exists)
            List<String> routingString = ArkOneParser.ParseRoutingStrings(command);

            try {
                Element commandInfo = (Element)ArkOneParser.ParseCommand(command);
                switch(commandInfo.getNodeName()) {
                    // ----------------------------- Plugin 0 (Core) ---------------------------- \\
                    case "a_lgu":
                        responses.add(basePlugin.LoginGuestUser());
                        break;
                    case "a_gpd":
                        responses.add(basePlugin.GetPluginDetails(commandInfo.getAttribute("p")));
                        break;
                    case "a_gsd":
                        responses.add(basePlugin.GetServiceDetails(commandInfo.getAttribute("s")));
                        break;
                    case "a_lru":
                        responses.add(basePlugin.LoginRegisteredUser(commandInfo, connection.getClientIdentifier()));
                        break;

                    // ----------------------------- Plugin 1 (User) ---------------------------- \\
                    case "u_reg":
                        responses.add(userPlugin.RegisterUser(commandInfo));
                        break;
                    case "u_gbl":
                        responses.add(userPlugin.GetBuddyList(connection));
                        break;
                    case "u_ccs":
                        responses.add(userPlugin.ChangeChatStatus(commandInfo, connection));
                        break;
                    case "u_cph":
                        responses.add(userPlugin.ChangePhoneStatus(commandInfo, connection));
                        break;
                    case "u_abd":
                        responses.add(userPlugin.AddBuddy(commandInfo, connection));
                        break;
                    case "u_abr":
                        responses.add(userPlugin.AddBuddyResponse(commandInfo, connection));
                        break;
                    case "u_spm":
                        responses.add(userPlugin.SendPrivateMessage(commandInfo));
                        break;
                    case "u_dbd":
                        responses.add(userPlugin.DeleteBuddy(commandInfo, connection));
                        break;
                    case "u_dbr":
                        responses.add(userPlugin.DeleteBuddyResponse(commandInfo, connection));
                        break;
                    case "u_inv":
                        responses.add(userPlugin.InvitePlayer());
                        break;
                    case "u_inr":
                        responses.add(userPlugin.InviteBuddyResponse());
                        break;
                    case "p":
                        responses.add(userPlugin.Ping(connection));
                        break;

                    // ----------------------- Plugin 5 (Rainbow Shootout) ---------------------- \\
                    case "cm":
                        responses.add(rainbowShootoutPlugin.CharacterMove(commandInfo));
                        break;
                    case "bs":
                        responses.add(rainbowShootoutPlugin.BlockShot());
                        break;

                    // ---------------------------- Plugin 7 (Galaxy) --------------------------- \\
                    case "lpv":
                        responses.add(galaxyPlugin.LoadProfileVersion(connection));
                        break;
                    case "vsu":
                        responses.add(galaxyPlugin.VersionStatisticsRequest());
                        break;
                    case "spp":
                        responses.add(galaxyPlugin.SaveProfilePart(commandInfo, connection));
                        break;
                    case "lp":
                        responses.add(galaxyPlugin.LoadProfile(connection));
                        break;
                    case "gls":
                        responses.add(galaxyPlugin.GetLeaderboardStats(commandInfo, connection));
                        break;

                    // ---------------------------- Plugin 10 (Trunk) --------------------------- \\
                    case "gua":
                        responses.add(trunkPlugin.GetUserAssets(connection));
                        break;
                    case "glb":
                        responses.add(trunkPlugin.GetLootBalance());
                        break;
                    case "gfl":
                        responses.add(trunkPlugin.GetFamiliarsList());
                        break;
                    case "gjl":
                        responses.add(trunkPlugin.GetJammersList());
                        break;
                    case "gml":
                        responses.add(trunkPlugin.GetMoodsList());
                        break;
                    case "gcl":
                        responses.add(trunkPlugin.GetCleaningsList());
                        break;
                    case "gil":
                        responses.add(trunkPlugin.GetItemsList());
                        break;
                    case "gsl":
                        responses.add(trunkPlugin.GetSplashList());
                        break;
                    case "gutc":
                        responses.add(trunkPlugin.GetUserTransactionsCount(connection));
                        break;
                    case "gut":
                        responses.add(trunkPlugin.GetUserTransactions(connection));
                        break;
                    case "asp":
                        responses.add(trunkPlugin.AssetParam(commandInfo, connection));
                        break;
                    case "bf":
                        responses.add(trunkPlugin.BuyFamiliar(commandInfo, connection));
                        break;
                    case "bj":
                        responses.add(trunkPlugin.BuyJammer(commandInfo, connection));
                        break;
                    case "bm":
                        responses.add(trunkPlugin.BuyMood(commandInfo, connection));
                        break;
                    case "bc":
                        responses.add(trunkPlugin.BuyCleaning(commandInfo, connection));
                        break;
                    case "bi":
                        responses.add(trunkPlugin.BuyItem(commandInfo, connection));
                        break;

                    // ----------------------- Multiplayer (Shared by all) ---------------------- \\
                    case "lv":
                        responses.add(multiplayerPlugin.LeaveGame());
                        break;
                    case "rp":
                        responses.add(multiplayerPlugin.ReadyPlay());
                        break;
                    case "ms":
                        responses.add(multiplayerPlugin.MessageOpponent(commandInfo, connection, routingString.get(1)));
                        break;
                    case "pa":
                        responses.add(multiplayerPlugin.PlayAgain());

                    // ---------------------------- Conflict Commands --------------------------- \\
                    case "jn":
                        switch(routingString.get(1)) {
                            case "2":
                                //TODO: IMPLEMENT CHAT - For now throw unhandled
                                responses.add("<unknown />");
                                System.out.println("[ArkOne][ERROR] Unhandled command: " + commandInfo.getNodeName());
                                //responses.add(chatPlugin.JoinChat());
                                break;
                            case "5":
                                responses.add(rainbowShootoutPlugin.JoinGame(commandInfo, connection));
                                break;
                            default:
                                responses.add("<unknown />");
                                System.out.println("[ArkOne][Error] Unhandled 'jn' route to plugin: " + routingString.get(1));
                                break;
                        }
                        break;
                    case "sp":
                        switch(routingString.get(1)) {
                            case "5":
                                responses.add(rainbowShootoutPlugin.ShotParameters(commandInfo));
                                break;
                            case "7":
                                responses.add(galaxyPlugin.SaveProfile(commandInfo, connection));
                                break;
                            default:
                                responses.add("<unknown />");
                                System.out.println("[ArkOne][Error] Unhandled 'sp' route to plugin: " + routingString.get(1));
                                break;
                        }
                        break;

                    // -------------------------------------------------------------------------- \\
                    default:
                        responses.add("<unknown />");
                        System.out.println("[ArkOne][ERROR] Unhandled command: " + commandInfo.getNodeName());
                        break;
                }
            } catch (Exception e) {
                System.out.println("[ArkOne][ERROR] Unknown error occurred: ");
                e.printStackTrace();
                responses.add("<unknown />");
            }
        }

        // Send the response
        for(String response : responses) {
            try {
                // Append a 0x00 to the end of the response
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(response.getBytes());
                outputStream.write((byte)0x00);

                connection.send(outputStream.toByteArray());

                System.out.println("[ArkOne] Response: " + response);
            } catch (IOException e) {
                System.out.println("[ArkOne][ERROR] Unknown error occurred: ");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void connectEvent(Connection connection) {
        // No need to log anything
    }

    @Override
    public void disconnectEvent(Connection connection) {
        User user = server.getConnectedUsers().get(connection.getClientIdentifier());

        if (user != null) {
            // Update the online status to offline and clear the connection ID
            user.setIsOnline(0);
            user.setChatStatus(0);
            user.setConnectionId(new UUID(0L, 0L));

            // Update the user in the DB
            userService.save(user);

            // Remove user from HashMap
            userService.removeUserFromServer(connection);

            // Notify other users that they went offline
            try {
                arkOneSender.SendStatusUpdate("u_cos", "o", "0", user.getUUID());
            } catch(Exception e) {
                // Do nothing since the client is disconnecting anyway
            }
        }
    }
}