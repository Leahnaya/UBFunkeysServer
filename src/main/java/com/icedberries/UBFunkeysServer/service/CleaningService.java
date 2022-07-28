package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Cleaning;

import java.util.List;
import java.util.Optional;

public interface CleaningService {

    List<Cleaning> findAll();

    Cleaning save(Cleaning cleaning);

    Optional<Cleaning> findByRid(String rid);

    Integer getCostById(Integer id);

    String getRidById(Integer id);
}
