package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Multiplayer.RainbowShootout;

import java.util.List;

public interface RainbowShootoutService {

    List<RainbowShootout> findAll();

    void delete(RainbowShootout rainbowShootout);
}
