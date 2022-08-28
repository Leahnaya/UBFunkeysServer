package com.icedberries.UBFunkeysServer.domain.Multiplayer;

import com.icedberries.UBFunkeysServer.config.TableNames;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = TableNames.RAINBOW_SHOOTOUT)
public class RainbowShootout {

    @Id
    private Integer userId;

    private String username;

    private String connectionId;

    // Determines whether the user is matchmaking or not
    private Integer challenge;

    // UserId of the opponent
    private Integer challenger;

    // A string that contains the opponent's bittyID/Funkey and a number at the end that doesn't seem to have a use
    private String challengerInfo;

    // Same as challengerInfo, but for the player. Used for random matchmaking
    private String playerInfo;

    // The player's score
    private Integer score;

    // If the user is ready to start the round
    private Integer ready;

    public java.util.UUID getConnectionId() {
        return java.util.UUID.fromString(connectionId);
    }

    public void setConnectionId(java.util.UUID newUUID) {
        connectionId = newUUID.toString();
    }
}
