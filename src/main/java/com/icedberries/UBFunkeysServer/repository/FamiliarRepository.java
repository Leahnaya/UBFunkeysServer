package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.Familiar;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamiliarRepository extends CrudRepository<Familiar, Integer> {

    List<Familiar> findAll();

    @Query("select familiar.cost from Familiar familiar where familiar.id = :id")
    Integer getCostById(@Param("id") Integer id);

    Optional<Familiar> findByRid(String rid);

    @Query("select familiar.rid from Familiar familiar where familiar.id = :id")
    String getRidById(@Param("id") Integer id);
}
