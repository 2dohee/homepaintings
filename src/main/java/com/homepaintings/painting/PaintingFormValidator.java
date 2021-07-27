package com.homepaintings.painting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
@RequiredArgsConstructor
public class PaintingFormValidator {

    private final PaintingRepository paintingRepository;

    public void validate(PaintingForm paintingForm, Errors errors) {
        if (paintingRepository.existsByName(paintingForm.getName())) {
            errors.rejectValue("name", "already_existed", new Object[]{paintingForm.getName()}, "이미 존재하는 이름입니다.");
        }
    }

    public void validate(PaintingForm paintingForm, Errors errors, Painting painting) {
        if (!painting.getName().equals(paintingForm.getName()) && paintingRepository.existsByName(paintingForm.getName())) {
            errors.rejectValue("name", "already_existed", new Object[]{paintingForm.getName()}, "이미 존재하는 이름입니다.");
        }
    }
}
