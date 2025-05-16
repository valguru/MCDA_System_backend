package com.dm_system.service;

import com.dm_system.dto.expert.ExpertDto;
import com.dm_system.dto.team.CreateTeamRequest;
import com.dm_system.dto.team.TeamDto;
import com.dm_system.model.*;
import com.dm_system.repository.ExpertRepository;
import com.dm_system.repository.ExpertTeamRepository;
import com.dm_system.repository.InvitationRepository;
import com.dm_system.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final ExpertRepository expertRepository;
    private final TeamRepository teamRepository;
    private final ExpertTeamRepository expertTeamRepository;
    private final InvitationRepository invitationRepository;

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

    @Transactional
    public void createTeamWithInvitations(CreateTeamRequest request, Authentication auth) {
        String creatorEmail = auth.getName();
        Expert creator = expertRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Создание команды
        Team team = new Team();
        team.setName(request.getName());
        team.setCreatedBy(creator);
        team = teamRepository.save(team);

        // Добавление создателя в команду
        ExpertTeam creatorLink = new ExpertTeam();
        creatorLink.setTeam(team);
        creatorLink.setExpert(creator);
        expertTeamRepository.save(creatorLink);

        // Создание приглашений
        for (String email : request.getEmails()) {
            if (email.equalsIgnoreCase(creatorEmail)) continue; // не приглашать себя

            Invitation invitation = new Invitation();
            invitation.setTeam(team);
            invitation.setSender(creator);
            invitation.setEmail(email);
            invitation.setStatus(InvitationStatus.PENDING);
            invitation.setSentAt(LocalDateTime.now());

            invitationRepository.save(invitation);

            // TODO: отправка email (по желанию)
        }
    }


}
