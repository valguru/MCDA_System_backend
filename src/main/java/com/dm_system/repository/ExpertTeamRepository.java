package com.dm_system.repository;

import com.dm_system.model.Expert;
import com.dm_system.model.ExpertTeam;
import com.dm_system.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpertTeamRepository extends JpaRepository<ExpertTeam, Long> {
    List<ExpertTeam> findByTeamId(Long teamId);
    boolean existsByExpertAndTeam(Expert expert, Team team);
    boolean existsByTeamIdAndExpertId(Long teamId, Long expertId);
}

