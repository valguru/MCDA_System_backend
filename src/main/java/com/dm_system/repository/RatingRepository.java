package com.dm_system.repository;

import com.dm_system.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Query("SELECT DISTINCT r.expert.id FROM Rating r WHERE r.question.id = :questionId")
    List<Long> findDistinctExpertIdsByQuestionId(@Param("questionId") Long questionId);

}
