package com.homepaintings.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class WithUserSecurityContextFactory implements WithSecurityContextFactory<WithUser> {

    private final UsersService usersService;

    @Override
    public SecurityContext createSecurityContext(WithUser withUser) {
        String email = withUser.value();

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail(email);
        signUpForm.setNickname("test");
        signUpForm.setPassword("12345678");
        signUpForm.setPasswordAgain("12345678");
        usersService.signUp(signUpForm);

        UserDetails principal = usersService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
