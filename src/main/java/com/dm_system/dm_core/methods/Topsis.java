package com.dm_system.dm_core.methods;

import com.dm_system.dm_core.utils.RatingNormalizer;
import com.dm_system.dto.rating.NormalizedRatingDto;
import com.dm_system.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class Topsis {
    public static List<Map.Entry<String, Double>> run(Question question, List<Rating> ratings) {
        List<Map.Entry<String, Double>> result = new ArrayList<>();

        String ANSI_BLUE = "\033[34m";
        String ANSI_GREEN = "\033[32m";
        String ANSI_RESET = "\033[0m";

        System.out.println(ANSI_BLUE + "\n=== Processing question: " + question.getTitle() + " ===" + ANSI_RESET);

        List<Criteria> criteriaList = question.getCriteria();

        if (criteriaList == null || criteriaList.isEmpty()) {
            System.err.println("Error: No criteria found for this question.");
            return result;
        }

        // Step 1: Normalize
        System.out.println(ANSI_GREEN + "\nNormalized ratings:" + ANSI_RESET);
        List<NormalizedRatingDto> normalized = RatingNormalizer.normalizeRatings(ratings, question);
        if (normalized.isEmpty()) {
            System.err.println("Error: No normalized ratings found.");
            return result;
        }
        for (NormalizedRatingDto normalizedRating : normalized) {
            System.out.println(normalizedRating);
        }

        // Step 2: Group and average
        System.out.println(ANSI_GREEN + "\nAveraged ratings:" + ANSI_RESET);
        Map<String, double[]> averagedRatings = groupAndAverageRatings(normalized, criteriaList);

        // Step 3: Ideal/anti-ideal
        List<Boolean> isPositive = criteriaList.stream()
                .map(c -> c.getOptimizationDirection() == OptimizationDirection.MAX)
                .collect(Collectors.toList());
        Map<String, double[]> idealPoints = calculateIdealAndAntiIdealPoints(averagedRatings, isPositive);

        // Step 4: Distances
        System.out.println(ANSI_GREEN + "\nDistances to Ideal and Anti-Ideal:" + ANSI_RESET);
        Map<String, double[]> distances = calculateEuclidDistances(averagedRatings, idealPoints.get("ideal"), idealPoints.get("antiIdeal"));

        // Step 5: Closeness
        System.out.println(ANSI_GREEN + "\nRelative Closeness (h(Ai)):" + ANSI_RESET);
        Map<String, Double> closeness = calculateRelativeCloseness(distances);

        result = rankAlternatives(closeness);
        return result;
    }

    public static Map<String, double[]> groupAndAverageRatings(List<NormalizedRatingDto> normalizedRatings, List<Criteria> criteriaList) {
        // Группировка по экспертам
        Map<Long, List<NormalizedRatingDto>> ratingsByExpert = normalizedRatings.stream()
                .collect(Collectors.groupingBy(NormalizedRatingDto::getExpertId));

        List<NormalizedRatingDto> normalizedPerExpert = new ArrayList<>();

        for (Map.Entry<Long, List<NormalizedRatingDto>> entry : ratingsByExpert.entrySet()) {
            List<NormalizedRatingDto> expertRatings = entry.getValue();

            // Собираем значения по критериям (в порядке criteriaList)
            Map<Long, List<Double>> valuesByCriteria = new HashMap<>();
            for (Criteria crit : criteriaList) {
                valuesByCriteria.put(crit.getId(), new ArrayList<>());
            }

            for (NormalizedRatingDto dto : expertRatings) {
                valuesByCriteria.get(dto.getCriteriaId()).add(dto.getValue());
            }

            // Считаем корень из суммы квадратов по каждому критерию
            Map<Long, Double> sqrtSums = new HashMap<>();
            for (Long critId : valuesByCriteria.keySet()) {
                double sumSquares = valuesByCriteria.get(critId).stream()
                        .mapToDouble(v -> v * v)
                        .sum();
                sqrtSums.put(critId, Math.sqrt(sumSquares));
            }

            // Нормализуем оценки
            for (NormalizedRatingDto dto : expertRatings) {
                double normValue = dto.getValue();
                double divisor = sqrtSums.get(dto.getCriteriaId());
                double normalized = (divisor == 0) ? 0.0 : normValue / divisor;

                NormalizedRatingDto normDto = new NormalizedRatingDto(
                        dto.getRatingId(),
                        dto.getExpertId(),
                        dto.getAlternativeId(),
                        dto.getCriteriaId(),
                        dto.getQuestionId(),
                        normalized
                );

                normalizedPerExpert.add(normDto);
            }
        }

        // Теперь сгруппируем по альтернативам и критериям
        Map<String, Map<Long, List<Double>>> altCritValues = new HashMap<>();
        for (NormalizedRatingDto dto : normalizedPerExpert) {
            String altKey = "" + dto.getAlternativeId();
            Long critId = dto.getCriteriaId();

            altCritValues
                    .computeIfAbsent(altKey, k -> new HashMap<>())
                    .computeIfAbsent(critId, k -> new ArrayList<>())
                    .add(dto.getValue());
        }

        // Усредним значения по альтернативам
        Map<String, double[]> averagedRatings = new HashMap<>();
        for (Map.Entry<String, Map<Long, List<Double>>> entry : altCritValues.entrySet()) {
            String altKey = entry.getKey();
            Map<Long, List<Double>> critMap = entry.getValue();

            double[] averaged = new double[criteriaList.size()];
            for (int i = 0; i < criteriaList.size(); i++) {
                Long critId = criteriaList.get(i).getId();
                List<Double> values = critMap.getOrDefault(critId, Collections.emptyList());

                averaged[i] = values.isEmpty()
                        ? 0.0
                        : values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            }

            averagedRatings.put(altKey, averaged);
            System.out.println("Averaged: " + altKey + " -> " + Arrays.toString(averaged));
        }

        return averagedRatings;
    }


    public static Map<String, double[]> calculateIdealAndAntiIdealPoints(Map<String, double[]> averagedRatings, List<Boolean> isPositiveCriteria) {
        String ANSI_GREEN = "\033[32m";
        String ANSI_RESET = "\033[0m";
        int criteriaCount = averagedRatings.values().iterator().next().length;

        // Берём первую альтернативу как начальную точку
        Iterator<double[]> iterator = averagedRatings.values().iterator();
        double[] firstRatings = iterator.next().clone();

        double[] idealPoint = firstRatings.clone();       // y+ (лучшие оценки)
        double[] antiIdealPoint = firstRatings.clone();   // y- (худшие оценки)

        // Проход по всем альтернативам и критериям
        for (double[] ratings : averagedRatings.values()) {
            for (int i = 0; i < criteriaCount; i++) {
                if (isPositiveCriteria.get(i)) {
                    // Для положительных критериев
                    idealPoint[i] = Math.max(idealPoint[i], ratings[i]);
                    antiIdealPoint[i] = Math.min(antiIdealPoint[i], ratings[i]);
                } else {
                    // Для отрицательных критериев
                    idealPoint[i] = Math.min(idealPoint[i], ratings[i]);
                    antiIdealPoint[i] = Math.max(antiIdealPoint[i], ratings[i]);
                }
            }
        }

        System.out.println(ANSI_GREEN + "\ny+: " + ANSI_RESET + Arrays.toString(idealPoint));
        System.out.println(ANSI_GREEN + "y-: " + ANSI_RESET + Arrays.toString(antiIdealPoint));

        Map<String, double[]> idealPoints = new HashMap<>();
        idealPoints.put("ideal", idealPoint);
        idealPoints.put("antiIdeal", antiIdealPoint);

        return idealPoints;
    }

    private static Map<String, double[]> calculateEuclidDistances(Map<String, double[]> ratings, double[] ideal, double[] antiIdeal) {
        Map<String, double[]> distances = new HashMap<>();

        for (Map.Entry<String, double[]> entry : ratings.entrySet()) {
            String alt = entry.getKey();
            double[] vals = entry.getValue();

            double dPlus = 0;
            double dMinus = 0;

            for (int i = 0; i < vals.length; i++) {
                dPlus += Math.pow(vals[i] - ideal[i], 2);
                dMinus += Math.pow(vals[i] - antiIdeal[i], 2);
            }

            double distPlus = Math.sqrt(dPlus);
            double distMinus = Math.sqrt(dMinus);

            distances.put(alt, new double[]{distPlus, distMinus});
            System.out.println(alt + " -> d+ = " + distPlus + ", d- = " + distMinus);
        }

        return distances;
    }

    private static Map<String, Double> calculateRelativeCloseness(Map<String, double[]> distances) {
        Map<String, Double> closeness = new HashMap<>();
        for (Map.Entry<String, double[]> entry : distances.entrySet()) {
            double dPlus = entry.getValue()[0];
            double dMinus = entry.getValue()[1];
            double h = (dPlus + dMinus) == 0 ? 0 : dMinus / (dPlus + dMinus);
            closeness.put(entry.getKey(), h);
        }
        return closeness;
    }

    private static List<Map.Entry<String, Double>> rankAlternatives(Map<String, Double> closeness) {
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(closeness.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        for (int i = 0; i < sorted.size(); i++) {
            System.out.printf("%d. %s -> h(Ai) = %.3f%n", i + 1, sorted.get(i).getKey(), sorted.get(i).getValue());
        }
        return sorted;
    }
}

