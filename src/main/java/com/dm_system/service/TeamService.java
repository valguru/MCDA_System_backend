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
import java.util.Optional;
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
                    createdBy.getName(),
                    createdBy.getSurname(),
                    createdBy.getPosition()
            );

            // Получаем список членов команды
            List<ExpertDto> members = expertTeamRepository.findByTeamId(team.getId())
                    .stream()
                    .map(et -> et.getExpert())
                    .distinct()  // чтобы избежать дублирования
                    .map(expert -> new ExpertDto(
                            expert.getId(),
                            expert.getEmail(),
                            expert.getName(),
                            expert.getSurname(),
                            expert.getPosition()
                    ))
                    .collect(Collectors.toList());

            return new TeamDto(
                    team.getId(),
                    team.getName(),
                    team.getDescription(),
                    createdByDto,
                    members
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TeamDto> getTeamById(Long teamId, Authentication authentication) {
        String email = authentication.getName();

        Expert currentUser = expertRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, входит ли пользователь в команду
        boolean isMember = expertTeamRepository.existsByTeamIdAndExpertId(teamId, currentUser.getId());
        if (!isMember) {
            return Optional.empty(); // не имеет доступа
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        ExpertDto createdByDto = new ExpertDto(
                team.getCreatedBy().getId(),
                team.getCreatedBy().getEmail(),
                team.getCreatedBy().getName(),
                team.getCreatedBy().getSurname(),
                team.getCreatedBy().getPosition()
        );

        List<ExpertDto> members = expertTeamRepository.findByTeamId(teamId).stream()
                .map(ExpertTeam::getExpert)
                .distinct()
                .map(e -> new ExpertDto(e.getId(), e.getEmail(), e.getName(), e.getSurname(), e.getPosition()))
                .collect(Collectors.toList());

        TeamDto dto = new TeamDto(
                team.getId(),
                team.getName(),
                team.getDescription(),
                createdByDto,
                members
        );

        return Optional.of(dto);
    }


    @Transactional
    public void createTeamWithInvitations(CreateTeamRequest request, Authentication auth) {
        String creatorEmail = auth.getName();
        Expert creator = expertRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Создание команды
        Team team = new Team();
        team.setName(request.getName());
        team.setDescription(request.getDescription());
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
