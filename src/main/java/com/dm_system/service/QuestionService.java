package com.dm_system.service;

import com.dm_system.dto.expert.ExpertDto;
import com.dm_system.dto.question.CreateQuestionRequest;
import com.dm_system.dto.question.QuestionDto;
import com.dm_system.dto.team.TeamDto.SimpleTeamDto;
import com.dm_system.model.*;
import com.dm_system.repository.ExpertRepository;
import com.dm_system.repository.ExpertTeamRepository;
import com.dm_system.repository.QuestionRepository;
import com.dm_system.repository.TeamRepository;
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
    private final TeamRepository teamRepository;

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

    @Transactional
    public void createQuestion(Long teamId, CreateQuestionRequest request, String creatorEmail) {
        Expert creator = expertRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, creator.getId());
        if (!hasAccess) {
            throw new AccessDeniedException("Нет доступа к команде");
        }

        // Загружаем команду из базы
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Команда не найдена"));

        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setDescription(request.getDescription());
        question.setTeam(team);
        question.setCreatedBy(creator);
        question.setStatus(QuestionStatus.ACTIVE);

        var alternatives = request.getAlternatives().stream().map(title -> {
            Alternative alt = new Alternative();
            alt.setTitle(title);
            alt.setQuestion(question);
            return alt;
        }).collect(Collectors.toSet());
        question.setAlternatives(alternatives);

        var criteria = request.getCriteria().stream().map(c -> {
            Criteria crit = new Criteria();
            crit.setName(c.getName());
            crit.setScale(ScaleType.valueOf(c.getScaleType().toUpperCase()));
            crit.setOptimizationDirection(OptimizationDirection.valueOf(c.getOptimization().toUpperCase()));
            crit.setQuestion(question);
            return crit;
        }).collect(Collectors.toSet());
        question.setCriteria(criteria);

        questionRepository.save(question);
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
