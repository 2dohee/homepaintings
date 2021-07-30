package com.homepaintings.cart;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter @Setter
public class CartForm {

    @NotBlank
    @Length(max = 100, message = "최대 100자까지 가능합니다.")
    private String paintingName;

    @Min(1) @Max(9999)
    private int quantity;

}
