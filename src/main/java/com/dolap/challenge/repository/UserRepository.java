package com.dolap.challenge.repository;

import com.dolap.challenge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "FROM User u WHERE u.username = :username")
    public User getUserByUsername(@Param("username") String username);
}
