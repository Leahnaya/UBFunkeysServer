package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.Jammer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JammerRepository extends CrudRepository<Jammer, Integer> {

    @Query("select jammer.cost from Jammer jammer where jammer.id = :id")
    Integer getCostById(@Param("id") Integer id);

    @Query("select jammer from Jammer jammer where jammer.qty = :qty")
    Optional<Jammer> findByQty(@Param("qty") Integer qty);

    @Query("select jammer.rid from Jammer jammer where jammer.id = :id")
    String getRidById(@Param("id") Integer id);

    @Query("select jammer.qty from Jammer jammer where jammer.id = :id")
    Integer getQtyById(@Param("id") Integer id);

    List<Jammer> findAll();
}
