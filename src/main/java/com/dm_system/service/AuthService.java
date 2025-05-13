package com.dm_system.service;

import com.dm_system.dto.auth.AuthRequest;
import com.dm_system.dto.auth.RegisterRequest;
import com.dm_system.model.Expert;
import com.dm_system.repository.ExpertRepository;
import com.dm_system.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private ExpertRepository expertRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public void register(RegisterRequest request) {
        if (expertRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        Expert expert = new Expert();
        expert.setEmail(request.getEmail());
        expert.setPassword(passwordEncoder.encode(request.getPassword())); // хэшируем пароль
        expert.setName(request.getName());
        expertRepo.save(expert);
    }

    public String login(AuthRequest request) {
        Expert expert = expertRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), expert.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        return jwtUtil.generateToken(expert.getEmail());
    }
}

