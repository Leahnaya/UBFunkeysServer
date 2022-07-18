package com.icedberries.UBFunkeysServer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class User {

    // User ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer UUID;

    // Username
    private String username;

    // Password
    private String password;

    // Security Question
    private String securityQuestion;

    // Security Answer
    private String securityAnswer;

    // If the user is online
    private Integer isOnline = 0;

    /*
     * Chat Status
     * 0 - Ready to Party
     * 1 - Do Not Disturb
     * 2 - Playing
     * 3 - Partying
     */
    private Integer chatStatus = 0;

    /*
     * If the user has a phone
     * 0 - No Cell Phone
     * 1 - Has Cell Phone
     */
    private Integer phoneStatus = 0;

    // Buddy Lists
    @Column(name = "buddyList")
    private String rawBuddyList;

    @Transient
    private ArrayList<User> buddyList;

    // Connection ID to send data to from other Users
    private String connectionId;
}
