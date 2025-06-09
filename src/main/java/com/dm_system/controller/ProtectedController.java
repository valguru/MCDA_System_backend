package com.dm_system.controller;

import com.dm_system.dm_core.methods.Topsis;
import com.dm_system.model.Question;
import com.dm_system.model.Rating;
import com.dm_system.repository.QuestionRepository;
import com.dm_system.repository.RatingRepository;
import com.dm_system.service.TopsisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProtectedController {

    private final TopsisService topsisService;
    private final QuestionRepository questionRepository;
    private final RatingRepository ratingRepository;

    public ProtectedController(TopsisService topsisService, QuestionRepository questionRepository, RatingRepository ratingRepository) {
        this.topsisService = topsisService;
        this.questionRepository = questionRepository;
        this.ratingRepository = ratingRepository;
    }

    @GetMapping("/protected")
    public String protectedEndpoint() {
        return "Это защищённый контент, доступный только авторизованным пользователям";
    }

    @GetMapping("/test-topsis")
    public String testTopsisFromDb() {
        Optional<Question> optionalQuestion = questionRepository.findById(16L);
        if (optionalQuestion.isEmpty()) {
            return "Вопрос с ID 16 не найден";
        }
        Question question = optionalQuestion.get();

        // Получаем все рейтинги, связанные с этим вопросом
        List<Rating> ratings = ratingRepository.findAllByQuestionId(16L);
        if (ratings.isEmpty()) {
            return "Нет рейтингов для вопроса с ID 16";
        }

        List<Map.Entry<String, Double>> result = Topsis.run(question, ratings);

        return result.stream()
                .map(e -> e.getKey() + ": " + String.format("%.3f", e.getValue()))
                .collect(Collectors.joining("\n"));
    }
}

