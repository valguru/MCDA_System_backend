package com.dm_system.service;

import com.dm_system.dto.alternative.AlternativeDto;
import com.dm_system.dto.expert.ExpertDto;
import com.dm_system.dto.question.QuestionCreateRequest;
import com.dm_system.dto.question.QuestionParticipantsResponse;
import com.dm_system.dto.question.QuestionDetailsDto;
import com.dm_system.dto.question.QuestionDto;
import com.dm_system.dto.team.TeamDto.SimpleTeamDto;
import com.dm_system.exceptions.ForbiddenAccessException;
import com.dm_system.exceptions.ResourceNotFoundException;
import com.dm_system.model.*;
import com.dm_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExpertTeamRepository expertTeamRepository;
    private final ExpertRepository expertRepository;
    private final TeamRepository teamRepository;
    private final RatingRepository ratingRepository;
    private final AlternativeRepository alternativeRepository;

    @Transactional(readOnly = true)
    public Question getQuestionById(Long questionId, String expertEmail) {
        Expert expert = expertRepository.findByEmail(expertEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос не найден"));

        Long teamId = question.getTeam().getId();

        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, expert.getId());
        if (!hasAccess) {
            throw new ForbiddenAccessException("Вы не состоите в команде, к которой относится вопрос");
        }

        return question;
    }

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
    public QuestionDetailsDto getQuestionDetails(Long questionId, String expertEmail) {
        Expert expert = expertRepository.findByEmail(expertEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос не найден"));

        Long teamId = question.getTeam().getId();

        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, expert.getId());
        if (!hasAccess) {
            throw new ForbiddenAccessException("Нет доступа к команде");
        }

        if (question.getStatus() == QuestionStatus.DRAFT &&
                !question.getCreatedBy().getId().equals(expert.getId())) {
            throw new ForbiddenAccessException("Нет доступа к черновику. Его может просматривать только автор.");
        }

        List<QuestionDetailsDto.CriterionDto> criteriaDtos = question.getCriteria().stream()
                .map(c -> new QuestionDetailsDto.CriterionDto(
                        c.getId(),
                        c.getName(),
                        c.getScale().name(),
                        c.getOptimizationDirection().name()
                ))
                .toList();

        List<AlternativeDto> alternativeDtos = question.getAlternatives().stream()
                .map(a -> new AlternativeDto(
                        a.getId(),
                        a.getTitle()
                ))
                .toList();

        QuestionDetailsDto dto = new QuestionDetailsDto(
                question.getId(),
                question.getTitle(),
                question.getDescription(),
                alternativeDtos,
                criteriaDtos,
                question.getStatus().name(),
                question.getCreatedAt(),
                mapExpertToDto(question.getCreatedBy()),
                null
        );

        if (question.getStatus() == QuestionStatus.RESOLVED && question.getSelectedAlternative() != null) {
            dto.setSelectedAlternative(new AlternativeDto(
                    question.getSelectedAlternative().getId(),
                    question.getSelectedAlternative().getTitle() // или getValue(), если поле называется иначе
            ));
        }

        return dto;
    }

    @Transactional
    public Long createQuestion(QuestionCreateRequest request, String creatorEmail) {
        Expert creator = expertRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(request.getTeamId(), creator.getId());
        if (!hasAccess) {
            throw new ForbiddenAccessException("Нет доступа к команде");
        }

        Team team = teamRepository.findById(request.getTeamId())
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
        return question.getId();
    }

    @Transactional
    public void activateQuestion(Long questionId, String expertEmail) {
        Expert expert = expertRepository.findByEmail(expertEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос не найден"));

        Long teamId = question.getTeam().getId();
        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, expert.getId());
        if (!hasAccess) {
            throw new ForbiddenAccessException("Нет доступа к команде вопроса");
        }

        if (question.getStatus() != QuestionStatus.DRAFT) {
            throw new ForbiddenAccessException("Можно активировать только вопрос в статусе DRAFT");
        }

        if (!question.getCreatedBy().getId().equals(expert.getId())) {
            throw new ForbiddenAccessException("Только автор может активировать вопрос");
        }

        if (question.getAlternatives().isEmpty() || question.getCriteria().isEmpty()) {
            throw new ForbiddenAccessException("Нельзя активировать вопрос без альтернатив и критериев");
        }

        question.setStatus(QuestionStatus.ACTIVE);
        questionRepository.save(question);
    }

    @Transactional(readOnly = true)
    public QuestionParticipantsResponse getParticipants(Long questionId, String currentUserEmail) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос с ID " + questionId + " не найден"));

        Long teamId = question.getTeam().getId();

        Expert currentExpert = expertRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с email " + currentUserEmail + " не найден"));

        boolean isMemberOfTeam = expertTeamRepository.existsByTeamIdAndExpertId(teamId, currentExpert.getId());
        if (!isMemberOfTeam) {
            throw new ForbiddenAccessException("Вы не состоите в команде, к которой относится данный вопрос");
        }

        List<Expert> teamExperts = expertTeamRepository.findExpertsByTeamId(teamId);

        List<Long> respondedExpertIds = ratingRepository.findDistinctExpertIdsByQuestionId(questionId);

        List<ExpertDto> responded = new ArrayList<>();
        List<ExpertDto> pending = new ArrayList<>();

        for (Expert expert : teamExperts) {
            ExpertDto dto = new ExpertDto(
                    expert.getId(),
                    expert.getEmail(),
                    expert.getName(),
                    expert.getSurname(),
                    expert.getPosition()
            );

            if (respondedExpertIds.contains(expert.getId())) {
                responded.add(dto);
            } else {
                pending.add(dto);
            }
        }

        return new QuestionParticipantsResponse(responded, pending);
    }

    @Transactional
    public void markAwaitingDecision(Long questionId, String expertEmail) {
        Expert expert = expertRepository.findByEmail(expertEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос не найден"));

        Long teamId = question.getTeam().getId();
        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, expert.getId());
        if (!hasAccess) {
            throw new ForbiddenAccessException("Нет доступа к команде вопроса");
        }

        if (!question.getStatus().equals(QuestionStatus.ACTIVE)) {
            throw new ForbiddenAccessException("Вопрос должен быть в статусе ACTIVE, чтобы перевести его в AWAITING_DECISION");
        }

        question.setStatus(QuestionStatus.AWAITING_DECISION);
        questionRepository.save(question);
    }

    @Transactional
    public void resolveQuestion(Long questionId, Long selectedAlternativeId, String email) {
        Expert expert = expertRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос не найден"));

        Long teamId = question.getTeam().getId();
        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, expert.getId());
        if (!hasAccess) {
            throw new ForbiddenAccessException("Нет доступа к команде вопроса");
        }

        if (question.getStatus() != QuestionStatus.AWAITING_DECISION) {
            throw new ForbiddenAccessException("Можно решить только вопрос в статусе AWAITING_DECISION");
        }

        if (question.getStatus() == QuestionStatus.RESOLVED) {
            throw new ForbiddenAccessException("Вопрос уже решён");
        }


        if (!question.getCreatedBy().getId().equals(expert.getId())) {
            throw new ForbiddenAccessException("Вы не автор этого вопроса");
        }

        Alternative alternative = alternativeRepository.findById(selectedAlternativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Альтернатива не найдена"));

        if (!question.getAlternatives().contains(alternative)) {
            throw new ForbiddenAccessException("Альтернатива не принадлежит вопросу");
        }

        question.setSelectedAlternative(alternative);
        question.setStatus(QuestionStatus.RESOLVED);
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
