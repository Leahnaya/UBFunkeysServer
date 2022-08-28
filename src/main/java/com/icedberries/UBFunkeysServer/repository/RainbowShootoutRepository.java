package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.Multiplayer.RainbowShootout;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RainbowShootoutRepository extends CrudRepository<RainbowShootout, Integer> {

    List<RainbowShootout> findAll();

    @Query("select rainbowShootout from RainbowShootout rainbowShootout where rainbowShootout.userId = :uuid")
    Optional<RainbowShootout> findByUserId(@Param("uuid") Integer uuid);

    @Query("select rainbowShootout from RainbowShootout rainbowShootout"
            + " where rainbowShootout.challenge = 0 and not rainbowShootout.userId = :uuid")
    List<RainbowShootout> findOtherOpenPlayers(@Param("uuid") Integer uuid);
}
