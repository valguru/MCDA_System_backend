package com.dm_system.controller;

import com.dm_system.dto.expert.ExpertDto;
import com.dm_system.model.Expert;
import com.dm_system.repository.ExpertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final ExpertRepository expertRepository;

    @GetMapping
    public ExpertDto getCurrentUser(Authentication auth) {
        Expert expert = expertRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Expert not found"));

        return new ExpertDto(expert.getId(), expert.getEmail(), expert.getName());
    }
}
