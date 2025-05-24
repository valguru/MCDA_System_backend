package com.dm_system.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "alternative")
@Getter
@Setter
@ToString(exclude = {"question", "ratings"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Alternative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @OneToMany(mappedBy = "alternative", cascade = CascadeType.ALL)
    private Set<Rating> ratings;
}
