package com.dm_system.dto.question;

import lombok.Data;

@Data
public class QuestionFilterRequest {
    private Long teamId;
    private String status = "ALL";
}
