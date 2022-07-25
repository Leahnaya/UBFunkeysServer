package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Familiar;

import java.util.List;
import java.util.Optional;

public interface FamiliarService {

    Optional<Familiar> findById(String id);

    Familiar save(Familiar familiar);

    List<Familiar> findAll();

    Integer getCostById(String id);
}
