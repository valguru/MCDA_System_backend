package com.dm_system.repository;

import com.dm_system.model.Invitation;
import com.dm_system.model.Team;
import com.dm_system.model.Expert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findBySender(Expert sender);
    List<Invitation> findByEmail(String email);
    Optional<Invitation> findByTeamAndEmail(Team team, String email);
}
