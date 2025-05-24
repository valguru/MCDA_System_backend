package com.dm_system.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "question")
@Getter
@Setter
@ToString(exclude = {"criteria", "alternatives", "team", "createdBy"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionStatus status = QuestionStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
