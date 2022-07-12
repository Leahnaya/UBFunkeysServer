package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.domain.User;
import com.icedberries.UBFunkeysServer.repository.UserRepository;
import com.icedberries.UBFunkeysServer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public final UserRepository userRepository;


    @Override
    public Optional<User> findByUUID(Integer uuid) {
        return userRepository.findByUUID(uuid);
    }

    @Override
    public Boolean existsByUUID(Integer uuid) {
        return userRepository.existsByUUID(uuid);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }
}
