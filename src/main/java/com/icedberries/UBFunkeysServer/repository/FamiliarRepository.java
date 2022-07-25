package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.Familiar;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamiliarRepository extends CrudRepository<Familiar, String> {

    List<Familiar> findAll();

    @Query("select familiar.cost from Familiar familiar where familiar.id = :id")
    Integer getCostById(@Param("id") String id);
}
