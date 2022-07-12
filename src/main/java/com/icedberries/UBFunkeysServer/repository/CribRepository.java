package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.Crib;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CribRepository extends CrudRepository<Crib, Integer> {

    Boolean existsByCribName(String cribName);

    Crib findByCribName(String cribName);
}
