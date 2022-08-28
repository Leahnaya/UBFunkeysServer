package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.domain.Multiplayer.RainbowShootout;
import com.icedberries.UBFunkeysServer.repository.RainbowShootoutRepository;
import com.icedberries.UBFunkeysServer.service.RainbowShootoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RainbowShootoutServiceImpl implements RainbowShootoutService {

    private final RainbowShootoutRepository rainbowShootoutRepository;

    @Override
    public List<RainbowShootout> findAll() {
        return rainbowShootoutRepository.findAll();
    }

    @Override
    public void delete(RainbowShootout rainbowShootout) {
        rainbowShootoutRepository.delete(rainbowShootout);
    }

    @Override
    public Optional<RainbowShootout> findByUserId(Integer userId) {
        return rainbowShootoutRepository.findByUserId(userId);
    }

    @Override
    public RainbowShootout save(RainbowShootout rainbowShootout) {
        return rainbowShootoutRepository.save(rainbowShootout);
    }

    @Override
    public List<RainbowShootout> findOtherOpenPlayers(Integer uuid) {
        return rainbowShootoutRepository.findOtherOpenPlayers(uuid);
    }
}
