package ru.dstu.dormitory.notifications_service.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final UserPrincipal principal;
    private final transient String token;

    public JwtAuthenticationToken(UserPrincipal principal,
                                  Collection<? extends GrantedAuthority> authorities,
                                  String token) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public UserPrincipal getPrincipal() {
        return principal;
    }
}
