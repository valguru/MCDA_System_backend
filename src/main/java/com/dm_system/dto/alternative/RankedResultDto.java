package com.dm_system.dto.alternative;

import java.util.List;

public class RankedResultDto {
    private List<RankedAlternativeDto> rankedAlternatives;

    public RankedResultDto(List<RankedAlternativeDto> rankedAlternatives) {
        this.rankedAlternatives = rankedAlternatives;
    }

    public List<RankedAlternativeDto> getRankedAlternatives() {
        return rankedAlternatives;
    }
}
