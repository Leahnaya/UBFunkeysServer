package com.icedberries.UBFunkeysServer.service.impl;

import com.icedberries.UBFunkeysServer.domain.Level;
import com.icedberries.UBFunkeysServer.repository.LevelRepository;
import com.icedberries.UBFunkeysServer.service.LevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LevelServiceImpl implements LevelService {

    private final LevelRepository levelRepository;

    @Override
    public Optional<Level> getLevelByName(String levelName) {
        return levelRepository.getLevelByName(levelName);
    }

    @Override
    public List<Level> getLevelsByUserId(Integer userId) {
        return levelRepository.getLevelsByUserId(userId);
    }

    @Override
    public Boolean existsByLevelNameAndGameName(String levelName, String gameVersion) {
        return levelRepository.existsByLevelNameAndGameName(levelName, gameVersion);
    }

    @Override
    public Level save(Level level) {
        return levelRepository.save(level);
    }

    @Override
    public List<Level> findAllByGameName(String gameName) {
        return levelRepository.findAllByGameName(gameName);
    }

    @Override
    public Optional<Level> findLevelById(Integer id) {
        return levelRepository.findLevelById(id);
    }
}
