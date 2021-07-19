package com.homepaintings.painting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class PaintingFormValidator implements Validator {

    private final PaintingRepository paintingRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(PaintingForm.class);
    }

    @Override
    public void validate(Object o, Errors errors) {
        PaintingForm paintingForm = (PaintingForm) o;
        if (paintingRepository.existsByName(paintingForm.getName())) {
            errors.rejectValue("name", "already_existed", new Object[]{paintingForm.getName()}, "이미 존재하는 이름입니다.");
        }
    }
}
