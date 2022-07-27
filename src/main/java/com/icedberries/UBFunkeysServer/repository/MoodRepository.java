package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.Mood;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoodRepository extends CrudRepository<Mood, Integer> {

    List<Mood> findAll();

    @Query("select mood from Mood mood where mood.rid = :rid")
    Optional<Mood> findByRid(@Param("rid") String rid);

    @Query("select mood.cost from Mood mood where mood.id = :id")
    Integer getCostById(@Param("id") Integer id);

    @Query("select mood.rid from Mood mood where mood.id = :id")
    String getRidById(@Param("id") Integer id);
}
