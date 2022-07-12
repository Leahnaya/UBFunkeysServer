package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Crib;

public interface CribService {

    Integer count();

    void save(Crib crib);

    Boolean existsByCribName(String cribName);

    Crib findByCribName(String cribName);

    Crib findById(Integer id);
}
