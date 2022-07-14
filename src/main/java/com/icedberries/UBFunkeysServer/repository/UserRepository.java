package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    Boolean existsByUUID(Integer uuid);

    @Query("select user from User user where user.UUID = :uuid")
    Optional<User> findByUUID(@Param("uuid") Integer uuid);

    Boolean existsByUsername(String username);

    @Query("select user from User user where user.username = :username")
    Optional<User> findByUsername(@Param("username") String username);
}
