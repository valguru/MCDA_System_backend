package com.dm_system.controller;

import com.dm_system.dto.question.CreateQuestionRequest;
import com.dm_system.dto.question.ParticipantsResponse;
import com.dm_system.dto.question.QuestionDetailsDto;
import com.dm_system.dto.question.QuestionDto;
import com.dm_system.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams/{teamId}/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<QuestionDto>> getTeamQuestions(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "ALL") String status,
            Principal principal
    ) {
        List<QuestionDto> result = questionService.getQuestionsByTeam(teamId, status, principal.getName());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionDetailsDto> getQuestionDetails(
            @PathVariable Long teamId,
            @PathVariable Long questionId,
            Principal principal
    ) {
        QuestionDetailsDto dto = questionService.getQuestionDetailsById(teamId, questionId, principal.getName());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createQuestion(
            @PathVariable Long teamId,
            @RequestBody CreateQuestionRequest request,
            Principal principal
    ) {
        questionService.createQuestion(teamId, request, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{questionId}/activate")
    public ResponseEntity<Map<String, String>> activateQuestion(
            @PathVariable Long teamId,
            @PathVariable Long questionId,
            Principal principal
    ) {
        questionService.activateQuestion(teamId, questionId, principal.getName());
        Map<String, String> response = Map.of("message", "Вопрос успешно активирован");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{questionId}/participants")
    public ResponseEntity<ParticipantsResponse> getParticipants(
            @PathVariable Long teamId,
            @PathVariable Long questionId,
            Principal principal
    ) {
        ParticipantsResponse participants = questionService.getParticipants(questionId, principal.getName());
        return ResponseEntity.ok(participants);
    }

}
