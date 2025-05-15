package com.dm_system.repository;

import com.dm_system.model.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpertRepository extends JpaRepository<Expert, Long> {
    Optional<Expert> findByEmail(String email);

    @Query("SELECT e FROM Expert e LEFT JOIN FETCH e.teams WHERE e.email = :email")
    Optional<Expert> findByEmailWithTeams(String email);
}
