package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Familiar;

import java.util.List;
import java.util.Optional;

public interface FamiliarService {

    Optional<Familiar> findByRid(String rid);

    Familiar save(Familiar familiar);

    List<Familiar> findAll();

    Integer getCostById(Integer id);

    String getRidById(Integer id);
}
