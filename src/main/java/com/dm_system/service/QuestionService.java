package com.dm_system.service;

import com.dm_system.dto.expert.ExpertDto;
import com.dm_system.dto.question.CreateQuestionRequest;
import com.dm_system.dto.question.QuestionDetailsDto;
import com.dm_system.dto.question.QuestionDto;
import com.dm_system.dto.team.TeamDto.SimpleTeamDto;
import com.dm_system.exceptions.ForbiddenAccessException;
import com.dm_system.exceptions.ResourceNotFoundException;
import com.dm_system.model.*;
import com.dm_system.repository.ExpertRepository;
import com.dm_system.repository.ExpertTeamRepository;
import com.dm_system.repository.QuestionRepository;
import com.dm_system.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
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
        Expert expert = expertRepository.findByEmail(expertEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, expert.getId());
        if (!hasAccess) {
            throw new ForbiddenAccessException("У вас нет доступа к этой команде");
        }

        QuestionStatus status = parseStatus(statusStr);

        List<Question> questions;
        if (status == QuestionStatus.DRAFT) {
            questions = questionRepository.findByTeamIdAndStatusAndCreatedById(teamId, QuestionStatus.DRAFT, expert.getId());
        } else if (status != null) {
            questions = questionRepository.findByTeamIdAndStatus(teamId, status);
        } else {
            questions = questionRepository.findByTeamId(teamId)
                    .stream()
                    .filter(q -> q.getStatus() != QuestionStatus.DRAFT || q.getCreatedBy().getId().equals(expert.getId()))
                    .collect(Collectors.toList());
        }

        return questions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuestionDetailsDto getQuestionDetailsById(Long teamId, Long questionId, String expertEmail) {
        Expert expert = expertRepository.findByEmail(expertEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, expert.getId());
        if (!hasAccess) {
            throw new ForbiddenAccessException("Нет доступа к команде");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос не найден"));

        if (!question.getTeam().getId().equals(teamId)) {
            throw new ForbiddenAccessException("Вопрос не принадлежит команде");
        }

        if (question.getStatus() == QuestionStatus.DRAFT &&
                !question.getCreatedBy().getId().equals(expert.getId())) {
            throw new ForbiddenAccessException("Нет доступа к черновику. Его может просматривать только автор.");
        }

        List<QuestionDetailsDto.CriterionDto> criteriaDtos = question.getCriteria().stream().map(c ->
                new QuestionDetailsDto.CriterionDto(
                        c.getName(),
                        c.getScale().name(),
                        c.getOptimizationDirection().name()
                )
        ).toList();

        List<String> alternativeTitles = question.getAlternatives().stream()
                .map(Alternative::getTitle)
                .toList();

        return new QuestionDetailsDto(
                question.getId(),
                question.getTitle(),
                question.getDescription(),
                alternativeTitles,
                criteriaDtos,
                question.getStatus().name(),
                question.getCreatedAt(),
                mapExpertToDto(question.getCreatedBy())
        );
    }

    @Transactional
    public void createQuestion(Long teamId, CreateQuestionRequest request, String creatorEmail) {
        Expert creator = expertRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, creator.getId());
        if (!hasAccess) {
            throw new ForbiddenAccessException("Нет доступа к команде");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Команда не найдена"));

        Question question = new Question();
        question.setTitle(request.getTitle());
        question.setDescription(request.getDescription());
        question.setTeam(team);
        question.setCreatedBy(creator);
        question.setStatus(QuestionStatus.DRAFT);

        List<Alternative> alternatives = request.getAlternatives().stream().map(title -> {
            Alternative alt = new Alternative();
            alt.setTitle(title);
            alt.setQuestion(question);
            return alt;
        }).collect(Collectors.toList());
        question.setAlternatives(alternatives);

        List<Criteria> criteria = request.getCriteria().stream().map(c -> {
            Criteria crit = new Criteria();
            crit.setName(c.getName());
            crit.setScale(ScaleType.valueOf(c.getScaleType().toUpperCase()));
            crit.setOptimizationDirection(OptimizationDirection.valueOf(c.getOptimization().toUpperCase()));
            crit.setQuestion(question);
            return crit;
        }).collect(Collectors.toList());
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
