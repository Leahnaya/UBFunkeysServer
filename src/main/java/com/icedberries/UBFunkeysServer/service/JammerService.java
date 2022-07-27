package com.icedberries.UBFunkeysServer.service;

import com.icedberries.UBFunkeysServer.domain.Jammer;

import java.util.List;
import java.util.Optional;

public interface JammerService {

    Integer getCostById(Integer id);

    Jammer save(Jammer jammer);

    Optional<Jammer> findByQty(Integer qty);

    String getRidById(Integer id);

    List<Jammer> findAll();

    Integer getQtyById(Integer id);
}
