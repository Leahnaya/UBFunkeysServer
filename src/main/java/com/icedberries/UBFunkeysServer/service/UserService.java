package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByUUID(Integer uuid);

    Boolean existsByUUID(Integer uuid);

    User save(User user);

    Boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}
