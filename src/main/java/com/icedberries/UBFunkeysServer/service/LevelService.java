package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Level;

import java.util.List;
import java.util.Optional;

public interface LevelService {

    Optional<Level> getLevelByName(String levelName);

    List<Level> getLevelsByUserId(Integer userId);

    Boolean existsByLevelNameAndGameName(String levelName, String gameVersion);

    Level save(Level level);

    List<Level> findAllByGameName(String gameName);

    Optional<Level> findLevelById(Integer id);
}
