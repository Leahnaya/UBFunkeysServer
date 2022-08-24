package com.icedberries.UBFunkeysServer.config;

import com.icedberries.UBFunkeysServer.domain.Multiplayer.RainbowShootout;
import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.service.RainbowShootoutService;
import com.icedberries.UBFunkeysServer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@EnableScheduling
@Component
public class DatabaseCleanup {

    @Autowired
    UserService userService;

    @Autowired
    RainbowShootoutService rainbowShootoutService;

    private boolean firstOfflineScheduledRun = true;

    // Run this every 60 seconds to check for inactive online users
    @Scheduled(fixedRate = 60000)
    public void setOfflineInactiveUsers() {
        List<User> onlineUsers = userService.getOnlineUsers();

        // If there is at least one user in the list
        if (onlineUsers.size() > 0) {
            for (User user : onlineUsers) {
                // Make sure the user has a last ping else turn them offline
                // A ping should be set on login
                if (firstOfflineScheduledRun) {
                    user.setIsOnline(0);
                    userService.save(user);
                    continue;
                }
                if (user.getLastPing() == null && user.getIsOnline() != 0) {
                    user.setIsOnline(0);
                    userService.save(user);
                    continue;
                }

                // Calculate how many milliseconds since last ping/login
                long difference = Math.abs(Duration.between(user.getLastPing(), LocalDateTime.now()).toMillis());
                if (difference > 60000) {
                    // USer has been online for more than 60 seconds without a new ping - set them offline
                    user.setIsOnline(0);
                    userService.save(user);
                }
            }
        }

        if (firstOfflineScheduledRun) {
            firstOfflineScheduledRun = false;
        }
    }

    // Cleanup open Multiplayer games that could be sitting open every 15 minutes and on startup
    // NOTE: This could create issues where a user is no longer matchmaking and will have to restart looking
    @Scheduled(fixedRate = 900000)
    public void clearOpenMultiplayerMatchmaking() {
        // Get all open multiplayer matchmaking entries in all tables
        List<RainbowShootout> openRainbowShootout = rainbowShootoutService.findAll();

        // Iterate over each game's entries
        for (RainbowShootout rainbowShootout : openRainbowShootout) {
            rainbowShootoutService.delete(rainbowShootout);
        }
    }
}
