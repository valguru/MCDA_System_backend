package com.dm_system.controller;

import com.dm_system.dto.team.TeamDto;
import com.dm_system.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/teams")
    public List<TeamDto> getTeams(Authentication authentication) {
        return teamService.getTeamsForCurrentUser(authentication);
    }
}
