package com.dm_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alternative")
@Getter
@Setter
@ToString(exclude = "question")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Alternative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}
