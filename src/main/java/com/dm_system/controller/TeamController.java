package com.dm_system.controller;

import com.dm_system.dto.team.CreateTeamRequest;
import com.dm_system.dto.team.TeamDto;
import com.dm_system.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @GetMapping()
    public List<TeamDto> getTeams(Authentication authentication) {
        return teamService.getTeamsForCurrentUser(authentication);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> getTeamById(@PathVariable Long id, Authentication authentication) {
        return teamService.getTeamById(id, authentication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    @PostMapping("/create")
    public void createTeam(@RequestBody CreateTeamRequest request, Authentication authentication) {
        teamService.createTeamWithInvitations(request, authentication);
    }
}
