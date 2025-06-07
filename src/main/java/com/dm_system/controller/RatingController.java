package com.dm_system.controller;

import com.dm_system.dto.rating.RatingCreateRequest;
import com.dm_system.dto.rating.RatingDto;
import com.dm_system.model.Expert;
import com.dm_system.service.RatingService;
import com.dm_system.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ratings")
public class RatingController {
    private final RatingService ratingService;
    private final AuthUtils authUtils;

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> submitRatings(
            @RequestBody RatingCreateRequest request,
            Principal principal
    ) {
        Expert expert = authUtils.getCurrentExpert(principal);
        ratingService.submitRatings(request, expert);
        return ResponseEntity.ok(Map.of("message", "Ответ успешно сохранён"));
    }

    @GetMapping("/by_question")
    public ResponseEntity<List<RatingDto>> getMyRatingsForQuestion(
            @RequestParam Long questionId,
            Principal principal
    ) {
        Expert expert = authUtils.getCurrentExpert(principal);
        List<RatingDto> ratings = ratingService.getRatingsByExpertAndQuestion(expert, questionId);
        return ResponseEntity.ok(ratings);
    }
}