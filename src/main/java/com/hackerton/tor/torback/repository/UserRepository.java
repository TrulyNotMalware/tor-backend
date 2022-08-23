package com.hackerton.tor.torback.repository;

import com.hackerton.tor.torback.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, String> {// Primary Key type is String.

    @Query("INSERT INTO user(userId,userName,password,email) VALUES(?,?,?,?)")
    public Mono<User> insertUser(String userId,String userName,String password,String email);

    @Query("SELECT * FROM user WHERE userId = :userId;")
    public Mono<User> selectUserById(@Param(value = "userId") String userId);
}
