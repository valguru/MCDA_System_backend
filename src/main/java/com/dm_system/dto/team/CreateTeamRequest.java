package com.dm_system.dto.team;

import lombok.Data;
import java.util.List;

@Data
public class CreateTeamRequest {
    private String name;
    private List<String> emails;
}
