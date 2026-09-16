package ru.dstu.dormitory.requests_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.dstu.dormitory.requests_service.config.SecurityProperties;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ServiceTokenFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Service-Token";
    private static final String ROLE = "ROLE_SERVICE";

    private final SecurityProperties properties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String incoming = request.getHeader(HEADER);
        String expected = properties.getServiceToken();
        if (incoming != null && !incoming.isBlank() && incoming.equals(expected)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            AbstractAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "service", null, List.of(new SimpleGrantedAuthority(ROLE)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
