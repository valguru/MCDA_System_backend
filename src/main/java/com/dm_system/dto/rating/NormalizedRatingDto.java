package com.dm_system.dto.rating;

public class NormalizedRatingDto {
    private Long ratingId;
    private Long expertId;
    private Long alternativeId;
    private Long criteriaId;
    private Long questionId;
    private double value;

    public NormalizedRatingDto(Long ratingId, Long expert, Long alternative, Long criteria, Long question, double value) {
        this.ratingId = ratingId;
        this.expertId = expert;
        this.alternativeId = alternative;
        this.criteriaId = criteria;
        this.questionId = question;
        this.value = value;
    }

    public Long getExpertId() {
        return expertId;
    }

    public Long getAlternativeId() {
        return alternativeId;
    }

    public Long getCriteriaId() {
        return criteriaId;
    }

    public double getValue() {
        return value;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Long getRatingId() {
        return ratingId;
    }

    public void setExpertId(Long expertId) {
        this.expertId = expertId;
    }

    public void setAlternativeId(Long alternativeId) {
        this.alternativeId = alternativeId;
    }

    public void setCriteriaId(Long criteriaId) {
        this.criteriaId = criteriaId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "NormalizedRatingDto{" +
                "ratingId=" + ratingId +
                ", expertId=" + expertId +
                ", alternativeId=" + alternativeId +
                ", criteriaId=" + criteriaId +
                ", questionId=" + questionId +
                ", value=" + value +
                '}';
    }
}