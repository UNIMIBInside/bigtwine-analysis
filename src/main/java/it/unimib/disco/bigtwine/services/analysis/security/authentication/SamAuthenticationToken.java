package it.unimib.disco.bigtwine.services.analysis.security.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SamAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final String userId;


    public SamAuthenticationToken(Object principal, Object credentials, String userId) {
        super(principal, credentials);
        this.userId = userId;
    }

    public SamAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, String userId) {
        super(principal, credentials, authorities);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
