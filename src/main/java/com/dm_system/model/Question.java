package com.dm_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Data
@Entity
@Table(name = "question")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Expert createdBy;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private Set<Criteria> criteria;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private Set<Alternative> alternatives;
}

