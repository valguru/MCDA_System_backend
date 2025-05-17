package com.dm_system.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Expert createdBy;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<ExpertTeam> members;
}