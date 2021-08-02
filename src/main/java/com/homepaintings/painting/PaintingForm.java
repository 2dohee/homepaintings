package com.homepaintings.painting;

import com.homepaintings.annotation.PaintingEnumType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter @Setter
public class PaintingForm {

    @NotBlank
    @Length(max = 100, message = "최대 100자까지 가능합니다.")
    private String name;

    @PaintingEnumType(enumClass = PaintingType.class)
    private String type;

    @Min(0) @Max(Integer.MAX_VALUE)
    private int price;

    @Min(0)
    private int stock;

    private String image;

    private String description;

}
