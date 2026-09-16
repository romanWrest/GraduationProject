package ru.dstu.dormitory.auth_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.auth_service.domain.model.Role;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;
import ru.dstu.dormitory.auth_service.domain.model.User;
import ru.dstu.dormitory.auth_service.domain.repo.RoleRepository;
import ru.dstu.dormitory.auth_service.domain.repo.UserRepository;
import ru.dstu.dormitory.auth_service.util.LogPatterns;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private final AdminProperties adminProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String email = adminProperties.getInitialEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        Optional<Role> adminRole = roleRepository.findByCode(RoleCode.ADMIN);
        if (adminRole.isEmpty()) {
            return;
        }
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole.get());

        User admin = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(adminProperties.getInitialPassword()))
                .fullName(adminProperties.getInitialFullName())
                .active(Boolean.TRUE)
                .roles(roles)
                .build();
        User saved = userRepository.save(admin);
        log.info(LogPatterns.USER_CREATED, saved.getId(), saved.getEmail());
    }
}
