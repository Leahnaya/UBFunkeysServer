package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.Multiplayer.RainbowShootout;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RainbowShootoutRepository extends CrudRepository<RainbowShootout, Integer> {

    List<RainbowShootout> findAll();


}
