package com.dm_system.dto.alternative;

public class RankedAlternativeDto {
    private int rank;
    private AlternativeDto alternative;
    private double weight;

    public RankedAlternativeDto(int rank, AlternativeDto alternative, double weight) {
        this.rank = rank;
        this.alternative = alternative;
        this.weight = weight;
    }

    public int getRank() {
        return rank;
    }

    public AlternativeDto getAlternative() {
        return alternative;
    }

    public double getWeight() {
        return weight;
    }
}
