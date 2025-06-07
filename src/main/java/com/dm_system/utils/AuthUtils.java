package com.dm_system.utils;

import com.dm_system.exceptions.ResourceNotFoundException;
import com.dm_system.model.Expert;
import com.dm_system.repository.ExpertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class AuthUtils {
    private final ExpertRepository expertRepository;

    public Expert getCurrentExpert(Principal principal) {
        return expertRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }
}