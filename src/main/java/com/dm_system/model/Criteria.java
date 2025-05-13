package com.dm_system.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "criteria")
public class Criteria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

// Enum для типов шкал
enum ScaleType {
    SHORT,    // Н, С, В
    BASE,     // ОН, Н, С, В, ОВ
    LONG,     // ЭН, ОН, Н, С, В, ОВ, ЭВ
    NUMERIC   // 1-10
}

// Enum для направления оптимизации
enum OptimizationDirection {
    MIN,      // Минимизация значения (чем меньше - тем лучше)
    MAX       // Максимизация значения (чем больше - тем лучше)
}