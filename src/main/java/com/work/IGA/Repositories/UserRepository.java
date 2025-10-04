package com.work.IGA.Repositories;

import com.work.IGA.Models.Users.UserSchema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserSchema, UUID> {
   Optional<UserSchema> findByEmail(String email);
   
}
