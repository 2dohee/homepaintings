package com.homepaintings.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final UsersRepository usersRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;
        if (usersRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "already_existed", new Object[]{signUpForm.getEmail()}, "이미 존재하는 이메일입니다.");
        }
        if (usersRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname", "already_existed", new Object[]{signUpForm.getNickname()}, "이미 존재하는 닉네임입니다.");
        }
        if (!signUpForm.getPassword().equals(signUpForm.getPasswordAgain())) {
            errors.rejectValue("passwordAgain", "not_same", new Object[]{signUpForm.getPasswordAgain()}, "같은 비밀번호를 입력해주세요.");
        }
    }
}
