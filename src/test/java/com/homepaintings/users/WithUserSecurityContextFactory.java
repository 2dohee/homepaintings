package com.homepaintings.users;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class WithUserSecurityContextFactory implements WithSecurityContextFactory<WithUser> {

    private final ModelMapper modelMapper;
    private final UsersService usersService;
    private final UsersRepository usersRepository;

    @Override
    public SecurityContext createSecurityContext(WithUser withUser) {
        String email = withUser.value();
        Authority authority = withUser.authority();

        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail(email);
        signUpForm.setNickname("test");
        signUpForm.setPassword("12345678");
        signUpForm.setPasswordAgain("12345678");
        if (authority == Authority.ROLE_USER) {
            usersService.signUp(signUpForm);
        } else if (authority == Authority.ROLE_ADMIN) {
            Users user = modelMapper.map(signUpForm, Users.class);
            user.setCreatedDateTime(LocalDateTime.now());
            user.setEmailVerified(true);
            user.setAuthority(Authority.ROLE_ADMIN);
            usersService.login(usersRepository.save(user));
        }

        UserDetails principal = usersService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
