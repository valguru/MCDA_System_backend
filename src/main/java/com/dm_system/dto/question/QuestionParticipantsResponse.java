package com.dm_system.dto.question;

import com.dm_system.dto.expert.ExpertDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class QuestionParticipantsResponse {
    private List<ExpertDto> responded;
    private List<ExpertDto> pending;
}
