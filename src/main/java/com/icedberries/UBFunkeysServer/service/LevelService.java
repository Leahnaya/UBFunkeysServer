package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Level;

import java.util.List;
import java.util.Optional;

public interface LevelService {

    Optional<Level> getLevelByName(String levelName);

    List<Level> getLevelsByGameNameAndUserId(String gameName, Integer userId);

    Boolean existsByLevelNameAndGameName(String levelName, String gameVersion);

    Level save(Level level);

    List<Level> findAllByGameName(String gameName);

    Optional<Level> findLevelById(Integer id);

    List<Level> findAllByGameNameAndKeyword(String gameName, String keyword);

    List<Level> findAllByGameNameAndAuthor(String gameName, String author);

    List<Level> findAllByGameNameAndLevelName(String gameName, String levelName);
}
