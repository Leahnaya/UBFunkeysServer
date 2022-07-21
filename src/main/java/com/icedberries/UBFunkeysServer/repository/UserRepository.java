package com.icedberries.UBFunkeysServer.repository;

import com.icedberries.UBFunkeysServer.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    Boolean existsByUUID(Integer uuid);

    @Query("select user from User user where user.UUID = :uuid")
    Optional<User> findByUUID(@Param("uuid") Integer uuid);

    Boolean existsByUsername(String username);

    @Query("select user from User user where user.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("select user.rawBuddyList from User user where user.UUID = :uuid")
    String getBuddyList(@Param("uuid") Integer uuid);

    @Query("select user from User user where user.connectionId = :connectionId")
    Optional<User> findByConnectionId(@Param("connectionId") UUID connectionId);

    @Query("select user from User user where user.isOnline = 1")
    List<User> getAllOnlineUsers();
}
