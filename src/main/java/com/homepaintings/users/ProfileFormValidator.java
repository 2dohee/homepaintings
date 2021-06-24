package com.homepaintings.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.constraints.Pattern;

@Component
@RequiredArgsConstructor
public class ProfileFormValidator {

    private final UsersRepository usersRepository;

    public void validate(Object target, Errors errors, Users user) {
        ProfileForm profileForm = (ProfileForm) target;
        if (!user.getNickname().equals(profileForm.getNickname()) && usersRepository.existsByNickname(profileForm.getNickname())) {
            errors.rejectValue("nickname", "already_existed", new Object[]{profileForm.getNickname()}, "이미 존재하는 닉네임입니다.");
        }
    }
}
