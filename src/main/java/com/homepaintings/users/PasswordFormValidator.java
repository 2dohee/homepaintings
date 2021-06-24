package com.homepaintings.users;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PasswordFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(PasswordForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;
        if (!passwordForm.getPassword().equals(passwordForm.getPasswordAgain())) {
            errors.rejectValue("passwordAgain", "not_same", new Object[]{passwordForm.getPasswordAgain()}, "같은 비밀번호를 입력해주세요.");
        }
    }
}
