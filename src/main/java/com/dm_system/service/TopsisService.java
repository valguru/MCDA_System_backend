package com.dm_system.service;

import com.dm_system.dm_core.methods.Topsis;
import com.dm_system.dto.alternative.AlternativeDto;
import com.dm_system.dto.alternative.RankedAlternativeDto;
import com.dm_system.dto.alternative.RankedResultDto;
import com.dm_system.model.Alternative;
import com.dm_system.model.Question;
import com.dm_system.model.Rating;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TopsisService {
    private final QuestionService questionService;
    private final RatingService ratingService;

    public TopsisService(QuestionService questionService, RatingService ratingService) {
        this.questionService = questionService;
        this.ratingService = ratingService;
    }

    public RankedResultDto calculateRanking(Long questionId, String expertEmail) {
        Question question = questionService.getQuestionById(questionId, expertEmail);
        List<Rating> ratings = ratingService.getRatingsByQuestionId(questionId, expertEmail);

        List<Map.Entry<String, Double>> closenessList = Topsis.run(question, ratings);

        Map<Long, Alternative> altMap = question.getAlternatives().stream()
                .collect(Collectors.toMap(Alternative::getId, alt -> alt));

        List<RankedAlternativeDto> ranked = new ArrayList<>();

        for (int i = 0; i < closenessList.size(); i++) {
            String key = closenessList.get(i).getKey();
            Double weight = closenessList.get(i).getValue();

            Long altId = Long.parseLong(key);
            Alternative alt = altMap.get(altId);

            if (alt != null) {
                AlternativeDto dto = new AlternativeDto(alt.getId(), alt.getTitle());
                ranked.add(new RankedAlternativeDto(i + 1, dto, weight));
            }
        }

        return new RankedResultDto(ranked);
    }
}