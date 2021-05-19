package com.homepaintings.users;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Setter @Getter
public class SignUpForm {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Length(min = 2, max = 20, message = "최소 2자 이상 입력해주세요.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$")
    private String nickname;

    @NotBlank
    @Pattern(regexp = "^.{8,20}$", message = "최소 8자 이상 입력해주세요.")
    @Length(min = 8, max = 20)
    private String password;

    @NotBlank
    @Pattern(regexp = "^.{8,20}$", message = "최소 8자 이상 입력해주세요.")
    @Length(min = 8, max = 20)
    private String passwordAgain;

}
