package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends CrudRepository<Item, Integer> {

    List<Item> findAll();

    @Query("select item from Item item where item.rid = :rid")
    Optional<Item> findByRid(@Param("rid") String rid);

    @Query("select item.cost from Item item where item.id = :id")
    Integer getCostById(@Param("id") Integer id);

    @Query("select item.rid from Item item where item.id = :id")
    String getRidById(@Param("id") Integer id);
}
