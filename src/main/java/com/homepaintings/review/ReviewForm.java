package com.homepaintings.review;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter @Setter
public class ReviewForm {

    @NotBlank
    @Min(1) @Max(5)
    private Integer rank = 5;

    private String image = "";

    @Length(max = 500, message = "최대 500자까지 가능합니다.")
    private String content = "";

}
