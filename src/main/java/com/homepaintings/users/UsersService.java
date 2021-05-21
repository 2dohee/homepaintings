package com.homepaintings.users;

import com.homepaintings.config.AppProperties;
import com.homepaintings.mail.EmailMessage;
import com.homepaintings.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    public Users signUp(SignUpForm signUpForm) {
        Users user = saveNewUsers(signUpForm);
        sendSignUpConfirmEmail(user);
        return user;
    }

    private void sendSignUpConfirmEmail(Users user) {
        Context context = new Context();
        context.setVariable("nickname", user.getNickname());
        context.setVariable("host", appProperties.getHost());
        context.setVariable("link", "/validate-email-token?email=" + user.getEmail() + "&token="
                + user.getEmailToken());
        String message = templateEngine.process("mail/confirm-format", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(user.getEmail())
                .message(message)
                .from("homepaintings 이메일 인증")
                .build();
        emailService.sendEmail(emailMessage);
    }

    private Users saveNewUsers(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Users user = modelMapper.map(signUpForm, Users.class);
        user.setCreatedDateTime(LocalDateTime.now());
        user.generateEmailToken();
        return usersRepository.save(user);
    }

    public void completeSignUp(Users user) {
        user.setEmailVerified(true);
        // 로그인
    }
}
