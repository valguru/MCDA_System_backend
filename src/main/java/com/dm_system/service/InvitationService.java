package com.dm_system.service;

import com.dm_system.dto.invitation.InvitationDto;
import com.dm_system.model.*;
import com.dm_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final ExpertRepository expertRepository;
    private final InvitationRepository invitationRepository;
    private final TeamRepository teamRepository;
    private final ExpertTeamRepository expertTeamRepository;

    private InvitationDto toDto(Invitation invitation) {
        InvitationDto dto = new InvitationDto();
        dto.setId(invitation.getId());
        dto.setEmail(invitation.getEmail());
        dto.setStatus(invitation.getStatus().name());
        dto.setTeamName(invitation.getTeam().getName());
        dto.setSenderName(invitation.getSender().getName());
        return dto;
    }

    public List<InvitationDto> getReceivedInvitations(Authentication auth) {
        String email = auth.getName();
        List<Invitation> invitations = invitationRepository.findByEmail(email);
        return invitations.stream()
                .map(this::toDto)
                .toList();
    }

    public List<InvitationDto> getSentInvitations(Authentication auth) {
        Expert sender = expertRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        List<Invitation> invitations = invitationRepository.findBySender(sender);
        return invitations.stream()
                .map(this::toDto)
                .toList();
    }

    public void sendInvitation(Long teamId, String email, Authentication auth) {
        Expert sender = expertRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        boolean alreadyInvited = invitationRepository.findByTeamAndEmail(team, email).isPresent();
        if (alreadyInvited) throw new RuntimeException("User already invited");

        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setTeam(team);
        invitation.setSender(sender);
        invitation.setStatus(InvitationStatus.PENDING);

        invitationRepository.save(invitation);

        // TODO: можно отправить email здесь
    }

    @Transactional
    public void respondToInvitation(Long invitationId, boolean accepted, Authentication auth) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        String email = auth.getName();

        if (!invitation.getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
            throw new RuntimeException("Invitation already responded");
        }

        if (accepted) {
            Expert expert = expertRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Expert not found"));

            Team team = invitation.getTeam();

            boolean alreadyInTeam = expertTeamRepository.existsByExpertAndTeam(expert, team);
            if (alreadyInTeam) {
                throw new RuntimeException("Already in team");
            }

            ExpertTeam expertTeam = new ExpertTeam();
            expertTeam.setExpert(expert);
            expertTeam.setTeam(team);
            expertTeamRepository.save(expertTeam);

            invitation.setStatus(InvitationStatus.ACCEPTED);
        } else {
            invitation.setStatus(InvitationStatus.REJECTED);
        }

        invitationRepository.save(invitation);
    }

}
