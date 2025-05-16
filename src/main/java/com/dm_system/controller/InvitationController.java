package com.dm_system.controller;

import com.dm_system.dto.invitation.InvitationDto;
import com.dm_system.model.Invitation;
import com.dm_system.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @GetMapping("/received")
    public List<InvitationDto> getReceived(Authentication auth) {
        return invitationService.getReceivedInvitations(auth);
    }

    @GetMapping("/sent")
    public List<InvitationDto> getSent(Authentication auth) {
        return invitationService.getSentInvitations(auth);
    }

    @PostMapping("/send")
    public void sendInvitation(@RequestParam Long teamId, @RequestParam String email, Authentication auth) {
        invitationService.sendInvitation(teamId, email, auth);
    }

    @PostMapping("/{id}/respond")
    public void respond(@PathVariable Long id, @RequestParam boolean accepted, Authentication auth) {
        invitationService.respondToInvitation(id, accepted, auth);
    }
}
