package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Mood;

import java.util.List;
import java.util.Optional;

public interface MoodService {

    List<Mood> findAll();

    Mood save(Mood mood);

    Optional<Mood> findByRid(String rid);

    Integer getCostById(Integer id);

    String getRidById(Integer id);
}
