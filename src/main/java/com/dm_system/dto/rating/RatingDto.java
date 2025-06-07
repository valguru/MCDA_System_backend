package com.dm_system.dto.rating;

import lombok.Data;

@Data
public class RatingDto {
    private Long id;
    private Long alternativeId;
    private Long criteriaId;
    private String value;
}