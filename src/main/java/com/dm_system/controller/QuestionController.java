package com.dm_system.controller;

import com.dm_system.dto.question.*;
import com.dm_system.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<List<QuestionDto>> getQuestionsByTeam(
            @RequestBody QuestionFilterRequest payload,
            Principal principal
    ) {
        List<QuestionDto> result = questionService.getQuestionsByTeam(
                payload.getTeamId(),
                payload.getStatus(),
                principal.getName()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDetailsDto> getQuestionDetails(
            @PathVariable Long questionId,
            Principal principal
    ) {
        QuestionDetailsDto dto = questionService.getQuestionDetails(questionId, principal.getName());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createQuestion(
            @RequestBody QuestionCreateRequest request,
            Principal principal
    ) {
        Long questionId = questionService.createQuestion(request, principal.getName());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Вопрос успешно создан");
        response.put("id", questionId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{questionId}/activate")
    public ResponseEntity<Map<String, String>> activateQuestion(
            @PathVariable Long questionId,
            Principal principal
    ) {
        questionService.activateQuestion(questionId, principal.getName());
        Map<String, String> response = Map.of("message", "Вопрос успешно активирован");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{questionId}/await_decision")
    public ResponseEntity<Map<String, String>> markAwaitingDecision(
            @PathVariable Long questionId,
            Principal principal
    ) {
        questionService.markAwaitingDecision(questionId, principal.getName());
        Map<String, String> response = Map.of("message", "Вопрос переведен в статус AWAITING_DECISION");
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{questionId}/participants")
    public ResponseEntity<QuestionParticipantsResponse> getParticipants(
            @PathVariable Long questionId,
            Principal principal
    ) {
        QuestionParticipantsResponse participants = questionService.getParticipants(questionId, principal.getName());
        return ResponseEntity.ok(participants);
    }
}
