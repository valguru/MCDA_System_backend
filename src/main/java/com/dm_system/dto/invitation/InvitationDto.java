package com.dm_system.dto.invitation;

import lombok.Data;

@Data
public class InvitationDto {
    private Long id;
    private String email;
    private String status;
    private String teamName;
    private String senderName;
}
