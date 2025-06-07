package com.dm_system.service;

import com.dm_system.dto.rating.RatingCreateRequest;
import com.dm_system.dto.rating.RatingDto;
import com.dm_system.exceptions.ForbiddenAccessException;
import com.dm_system.exceptions.ResourceNotFoundException;
import com.dm_system.model.*;
import com.dm_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final QuestionRepository questionRepository;
    private final AlternativeRepository alternativeRepository;
    private final CriteriaRepository criteriaRepository;
    private final ExpertTeamRepository expertTeamRepository;

    public void submitRatings(RatingCreateRequest request, Expert expert) {
        Long questionId = request.getQuestionId();

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Вопрос не найден"));

        if (!QuestionStatus.ACTIVE.equals(question.getStatus())) {
            throw new ForbiddenAccessException("Голосование доступно только в статусе ACTIVE");
        }

        Long teamId = question.getTeam().getId();
        boolean hasAccess = expertTeamRepository.existsByTeamIdAndExpertId(teamId, expert.getId());
        if (!hasAccess) {
            throw new ForbiddenAccessException("Вы не можете отправить ответ на этот вопрос, так как не состоите в команде, которой он принадлежит");
        }

        boolean alreadyVoted = ratingRepository.existsByExpertIdAndQuestionId(expert.getId(), questionId);
        if (alreadyVoted) {
            throw new ForbiddenAccessException("Вы уже проголосовали по этому вопросу");
        }

        List<Rating> ratings = request.getAnswers().stream().map(dto -> {
            Alternative alternative = alternativeRepository.findById(dto.getAlternativeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Альтернатива не найдена"));

            Criteria criteria = criteriaRepository.findById(dto.getCriteriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Критерий не найден"));

            Rating rating = new Rating();
            rating.setExpert(expert);
            rating.setQuestion(question);
            rating.setAlternative(alternative);
            rating.setCriteria(criteria);
            rating.setValue(dto.getValue());

            return rating;
        }).collect(Collectors.toList());

        ratingRepository.saveAll(ratings);
    }

    public List<RatingDto> getRatingsByExpertAndQuestion(Expert expert, Long questionId) {
        List<Rating> ratings = ratingRepository.findByExpertIdAndQuestionId(expert.getId(), questionId);
        return ratings.stream()
                .map(this::toDto)
                .toList();
    }

    private RatingDto toDto(Rating rating) {
        RatingDto dto = new RatingDto();
        dto.setId(rating.getId());
        dto.setAlternativeId(rating.getAlternative().getId());
        dto.setCriteriaId(rating.getCriteria().getId());
        dto.setValue(rating.getValue());
        return dto;
    }
}