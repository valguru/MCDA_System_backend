package com.dm_system.service;

import com.dm_system.dto.expert.ExpertDto;
import com.dm_system.dto.question.QuestionDto;
import com.dm_system.dto.team.TeamDto.SimpleTeamDto;
import com.dm_system.model.Expert;
import com.dm_system.model.Question;
import com.dm_system.model.QuestionStatus;
import com.dm_system.model.Team;
import com.dm_system.repository.ExpertRepository;
import com.dm_system.repository.ExpertTeamRepository;
import com.dm_system.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExpertTeamRepository expertTeamRepository;
    private final ExpertRepository expertRepository;

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsByTeam(Long teamId, String statusStr, String expertEmail) {
        // Получаем текущего эксперта по email
        Expert expert = expertRepository.findByEmail(expertEmail)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Проверяем, состоит ли эксперт в команде
        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, expert.getId());
        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа к этой команде");
        }

        // Получаем статус (или null, если "ALL")
        QuestionStatus status = parseStatus(statusStr);

        // Получаем список вопросов по статусу
        List<Question> questions = (status != null)
                ? questionRepository.findByTeamIdAndStatus(teamId, status)
                : questionRepository.findByTeamId(teamId);

        // Преобразуем в DTO
        return questions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private QuestionStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.equalsIgnoreCase("ALL")) {
            return null;
        }
        try {
            return QuestionStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверный статус: " + statusStr);
        }
    }

    private QuestionDto mapToDto(Question question) {
        return new QuestionDto(
                question.getId(),
                question.getTitle(),
                question.getDescription(),
                question.getStatus().name(),
                question.getCreatedAt(),
                mapExpertToDto(question.getCreatedBy()),
                mapTeamToDto(question.getTeam())
        );
    }

    private ExpertDto mapExpertToDto(Expert expert) {
        if (expert == null) return null;
        return new ExpertDto(
                expert.getId(),
                expert.getEmail(),
                expert.getName(),
                expert.getSurname(),
                expert.getPosition()
        );
    }

    private SimpleTeamDto mapTeamToDto(Team team) {
        if (team == null) return null;
        return new SimpleTeamDto(
                team.getId(),
                team.getName()
        );
    }
}
