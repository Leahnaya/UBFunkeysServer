package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.Cleaning;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CleaningRepository extends CrudRepository<Cleaning, Integer> {

    List<Cleaning> findAll();

    @Query("select cleaning from Cleaning cleaning where cleaning.rid = :rid")
    Optional<Cleaning> findByRid(@Param("rid") String rid);

    @Query("select cleaning.cost from Cleaning cleaning where cleaning.id = :id")
    Integer getCostById(@Param("id") Integer id);

    @Query("select cleaning.rid from Cleaning cleaning where cleaning.id = :id")
    String getRidById(@Param("id") Integer id);
}
