package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.repository.UserRepository;
import com.icedberries.UBFunkeysServer.service.UserService;
import javagrinko.spring.tcp.Connection;
import javagrinko.spring.tcp.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public final UserRepository userRepository;

    @Autowired
    Server server;

    @Override
    public Optional<User> findByUUID(Integer uuid) {
        return userRepository.findByUUID(uuid);
    }

    @Override
    public Boolean existsByUUID(Integer uuid) {
        return userRepository.existsByUUID(uuid);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public String getBuddyList(Integer uuid) {
        return userRepository.getBuddyList(uuid);
    }

    @Override
    public Optional<User> findByConnectionId(UUID connectionId) {
        return userRepository.findByConnectionId(connectionId);
    }

    @Override
    public User updateUserOnServer(Connection connection, User user) {
        return updateUserOnServer(connection.getClientIdentifier(), user);
    }

    @Override
    public User updateUserOnServer(UUID uuid, User user) {
        User returnedUser = save(user);

        // Save user to local map
        server.addConnectedUser(uuid, returnedUser);

        return returnedUser;
    }

    @Override
    public void removeUserFromServer(Connection connection) {
        server.removeConnectedUser(connection.getClientIdentifier());
    }

    @Override
    public List<User> getOnlineUsers() {
        return userRepository.getAllOnlineUsers();
    }
}
