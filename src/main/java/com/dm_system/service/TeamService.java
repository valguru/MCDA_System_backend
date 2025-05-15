package com.dm_system.service;

import com.dm_system.dto.expert.ExpertDto;
import com.dm_system.dto.team.TeamDto;
import com.dm_system.model.Expert;
import com.dm_system.model.Team;
import com.dm_system.repository.ExpertRepository;
import com.dm_system.repository.ExpertTeamRepository;
import com.dm_system.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final ExpertRepository expertRepository;
    private final TeamRepository teamRepository;
    private final ExpertTeamRepository expertTeamRepository;

    @Transactional(readOnly = true)
    public List<TeamDto> getTeamsForCurrentUser(Authentication authentication) {
        String email = authentication.getName();

        Expert currentUser = expertRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Получаем команды для текущего пользователя
        List<Team> teams = teamRepository.findTeamsByExpertId(currentUser.getId());

        return teams.stream().map(team -> {
            // Собираем данные о создателе команды
            Expert createdBy = team.getCreatedBy();
            ExpertDto createdByDto = new ExpertDto(
                    createdBy.getId(),
                    createdBy.getEmail(),
                    createdBy.getName()
            );

            // Получаем список членов команды
            List<ExpertDto> members = expertTeamRepository.findByTeamId(team.getId())
                    .stream()
                    .map(et -> et.getExpert())
                    .distinct()  // чтобы избежать дублирования
                    .map(expert -> new ExpertDto(
                            expert.getId(),
                            expert.getEmail(),
                            expert.getName()
                    ))
                    .collect(Collectors.toList());

            return new TeamDto(
                    team.getId(),
                    team.getName(),
                    createdByDto,
                    members
            );
        }).collect(Collectors.toList());
    }

}
