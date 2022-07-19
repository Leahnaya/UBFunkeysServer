package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.User;
import javagrinko.spring.tcp.Connection;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    Optional<User> findByUUID(Integer uuid);

    Boolean existsByUUID(Integer uuid);

    User save(User user);

    Boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    String getBuddyList(Integer uuid);

    Optional<User> findByConnectionId(UUID connectionId);

    User updateUserOnServer(Connection connection, User user);
    User updateUserOnServer(UUID uuid, User user);

    void removeUserFromServer(Connection connection);
}
