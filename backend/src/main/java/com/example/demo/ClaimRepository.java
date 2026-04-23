package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    // JpaRepository provides:
    // save(), saveAll(), findById(), findAll(), delete(), deleteById(), etc.
}
