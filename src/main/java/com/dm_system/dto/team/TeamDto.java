package com.dm_system.dto.team;

import com.dm_system.dto.expert.ExpertDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TeamDto {
    private Long id;
    private String name;
    private String description;
    private ExpertDto createdBy;
    private List<ExpertDto> members;

    @Data
    @AllArgsConstructor
    public static class SimpleTeamDto {
        private Long id;
        private String name;
    }
}
