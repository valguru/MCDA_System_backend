package com.dm_system.dto.rating;

import lombok.Data;

import java.util.List;

@Data
public class RatingCreateRequest {
    private Long questionId;
    private List<RatingDto> answers;
}