package com.nmaravic.movie.api.config;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.addAll(addRealmRoles(jwt));
        grantedAuthorities.addAll(addResourceRoles(jwt));
        return grantedAuthorities;
    }

    private Collection<GrantedAuthority> addRealmRoles(Jwt jwt) {
        Map<String, Collection<String>> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if (realmAccess != null && !realmAccess.isEmpty()) {
            Collection<String> roles = realmAccess.get(ROLES_CLAIM);
            if (roles != null && !roles.isEmpty()) {
                Collection<GrantedAuthority> realmRoles = roles.stream()
                        .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                        .collect(Collectors.toList());
                grantedAuthorities.addAll(realmRoles);
            }
        }
        return grantedAuthorities;
    }

    private Collection<GrantedAuthority> addResourceRoles(Jwt jwt) {
        Map<String, Map<String, Collection<String>>> resourceAccess = jwt.getClaim(RESOURCE_ACCESS_CLAIM);
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        if (resourceAccess != null && !resourceAccess.isEmpty()) {
            resourceAccess.forEach((resource, resourceClaims) -> {
                Collection<String> roles = resourceClaims.get(ROLES_CLAIM);
                if (roles != null) {
                    roles.forEach(role ->
                            grantedAuthorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role))
                    );
                }
            });
        }
        return grantedAuthorities;
    }
}