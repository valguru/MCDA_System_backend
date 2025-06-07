package com.dm_system.dto.question;

import com.dm_system.dto.expert.ExpertDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDetailsDto {
    private Long id;
    private String title;
    private String description;
    private List<AlternativeDto> alternatives;
    private List<CriterionDto> criteria;
    private String status;
    private LocalDateTime createdAt;
    private ExpertDto createdBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeDto {
        private Long id;
        private String value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CriterionDto {
        private Long id;
        private String name;
        private String scaleType;
        private String optimization;
    }
}
