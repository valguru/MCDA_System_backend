package com.dm_system.dto.question;

import com.dm_system.dto.expert.ExpertDto;
import com.dm_system.dto.team.TeamDto.SimpleTeamDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class QuestionDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private ExpertDto createdBy;
    private SimpleTeamDto team;
}
