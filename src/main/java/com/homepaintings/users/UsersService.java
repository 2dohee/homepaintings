package com.homepaintings.users;

import com.homepaintings.config.AppProperties;
import com.homepaintings.mail.EmailMessage;
import com.homepaintings.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    public void signUp(SignUpForm signUpForm) {
        Users user = saveNewUsers(signUpForm);
        sendVerificationTokenEmail(user);
        login(user);
    }

    public void login(Users user) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(user),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getAuthority().toString()))
        );
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> user = usersRepository.findByEmail(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        return new UserPrincipal(user.get());
    }

    private void sendVerificationTokenEmail(Users user) {
        Context context = new Context();
        context.setVariable("nickname", user.getNickname());
        context.setVariable("host", appProperties.getHost());
        context.setVariable("link", "/validate-email-token?email=" + user.getEmail() + "&token="
                + user.getEmailToken());
        String message = templateEngine.process("mail/confirm-format", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(user.getEmail())
                .message(message)
                .from("homepaintings ????????? ??????")
                .build();
        emailService.sendEmail(emailMessage);
    }

    private Users saveNewUsers(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Users user = modelMapper.map(signUpForm, Users.class);
        user.setCreatedDateTime(LocalDateTime.now());
        user.generateEmailToken();
        user.setAuthority(Authority.ROLE_USER);
        return usersRepository.save(user);
    }

    public void completeSignUp(Users user) {
        user.setEmailVerified(true);
        login(user);
    }

    public void updateProfile(Users user, ProfileForm profileForm) {
        modelMapper.map(profileForm, user);
        usersRepository.save(user);
    }

    public void changePassword(Users user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        usersRepository.save(user);
    }

    public void resendEmailToken(Users user) {
        user.generateEmailToken();
        usersRepository.save(user);
        sendVerificationTokenEmail(user);
    }

    public void deleteUser(Users user) {
        usersRepository.delete(user);
        SecurityContextHolder.clearContext();
    }

}
