package com.dm_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "criteria")
@Getter
@Setter
@ToString(exclude = "question")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Criteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScaleType scale;

    @Enumerated(EnumType.STRING)
    @Column(name = "optimization_direction", nullable = false)
    private OptimizationDirection optimizationDirection;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}
