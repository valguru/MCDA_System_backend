package com.dm_system.dto.expert;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExpertDto {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String position;
}
