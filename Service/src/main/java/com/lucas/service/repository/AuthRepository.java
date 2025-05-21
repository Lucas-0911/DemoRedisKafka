package com.lucas.service.repository;

import com.lucas.service.model.entity.Accounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Accounts, Long> {
    Optional<Accounts> findByUsername(String username);
}
