package com.dm_system.dto.question;

import lombok.Data;

import java.util.List;

@Data
public class QuestionCreateRequest {
    private Long teamId;
    private String title;
    private String description;
    private List<String> alternatives;
    private List<CriterionDto> criteria;

    @Data
    public static class CriterionDto {
        private String name;
        private String scaleType;
        private String optimization;
    }
}
