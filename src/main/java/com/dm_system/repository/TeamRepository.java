package com.dm_system.repository;

import com.dm_system.model.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = {"createdBy"})
    @Query("SELECT DISTINCT t FROM Team t JOIN FETCH t.createdBy JOIN FETCH t.members m WHERE m.expert.id = :expertId")
    List<Team> findTeamsByExpertId(@Param("expertId") Long expertId);
}
