package com.homepaintings.users;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter @Setter
public class PasswordForm {

    @NotBlank
    @Pattern(regexp = "^.{8,20}$", message = "최소 8자 이상 입력해주세요.")
    @Length(min = 8, max = 20)
    private String password;

    @NotBlank
    @Pattern(regexp = "^.{8,20}$", message = "최소 8자 이상 입력해주세요.")
    @Length(min = 8, max = 20)
    private String passwordAgain;

}
