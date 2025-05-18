package com.dm_system.repository;

import com.dm_system.model.Question;
import com.dm_system.model.QuestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.team.id = :teamId AND (:status IS NULL OR q.status = :status)")
    List<Question> findByTeamIdAndStatus(Long teamId, QuestionStatus status);
    List<Question> findByTeamId(Long teamId);
}
