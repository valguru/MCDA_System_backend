package com.dm_system.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "expert_team",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"expert_id", "team_id"}
        ))
public class ExpertTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}
