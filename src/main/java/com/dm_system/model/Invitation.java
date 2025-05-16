package com.dm_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invitation")
public class Invitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status = InvitationStatus.PENDING;

    @ManyToOne
    private Team team;

    @ManyToOne
    private Expert sender;

    private LocalDateTime sentAt = LocalDateTime.now();
}
