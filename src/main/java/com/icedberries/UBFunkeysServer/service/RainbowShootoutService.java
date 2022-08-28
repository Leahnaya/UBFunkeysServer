package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Multiplayer.RainbowShootout;

import java.util.List;
import java.util.Optional;

public interface RainbowShootoutService {

    List<RainbowShootout> findAll();

    void delete(RainbowShootout rainbowShootout);

    Optional<RainbowShootout> findByUserId(Integer userId);

    RainbowShootout save(RainbowShootout rainbowShootout);

    List<RainbowShootout> findOtherOpenPlayers(Integer uuid);
}
