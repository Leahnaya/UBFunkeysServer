package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.Level;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends CrudRepository<Level, Integer> {

    @Query("select level from Level level where level.levelName = :levelName")
    Optional<Level> getLevelByName(@Param("levelName") String levelName);

    @Query("select level from Level level where level.userId = :userId")
    List<Level> getLevelsByUserId(@Param("userId") Integer userId);

    Boolean existsByLevelNameAndGameName(String levelName, String gameName);

    @Query("select level from Level level where level.gameName = :gameName")
    List<Level> findAllByGameName(@Param("gameName") String gameName);

    @Query("select level from Level level where level.id = :id")
    Optional<Level> findLevelById(@Param("id") Integer id);
}
