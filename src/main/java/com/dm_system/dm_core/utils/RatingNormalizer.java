package com.dm_system.dm_core.utils;

import com.dm_system.dto.rating.NormalizedRatingDto;
import com.dm_system.model.Question;
import com.dm_system.model.Rating;

import java.util.*;

public class RatingNormalizer {

    public static List<NormalizedRatingDto> normalizeRatings(List<Rating> ratings, Question question) {
        List<NormalizedRatingDto> normalized = new ArrayList<>();

        for (Rating rating : ratings) {
            if (!rating.getQuestion().getId().equals(question.getId())) {
                continue;
            }

            try {
                double numericValue = ScaleConverter.convert(
                        rating.getCriteria().getScale().name(),
                        rating.getValue()
                );

                normalized.add(new NormalizedRatingDto(
                        rating.getId(),
                        rating.getExpert().getId(),
                        rating.getAlternative().getId(),
                        rating.getCriteria().getId(),
                        rating.getQuestion().getId(),
                        numericValue
                ));
            } catch (Exception e) {
                System.err.println("Ошибка нормализации (ratingId=" + rating.getId() + "): " + e.getMessage());
            }
        }

        return normalized;
    }
}