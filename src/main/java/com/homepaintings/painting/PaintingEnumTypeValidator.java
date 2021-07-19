package com.homepaintings.painting;

import com.homepaintings.annotation.PaintingEnumType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PaintingEnumTypeValidator implements ConstraintValidator<PaintingEnumType, String> {

    private PaintingEnumType paintingEnumType;

    @Override
    public void initialize(PaintingEnumType constraintAnnotation) {
        this.paintingEnumType = constraintAnnotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Enum<?>[] enumValues = this.paintingEnumType.enumClass().getEnumConstants();
        if (enumValues != null) {
            for (Enum<?> enumValue : enumValues) {
                if (value.equals(enumValue.toString())) return true;
            }
        }
        return false;
    }
}
