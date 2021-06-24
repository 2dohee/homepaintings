package com.homepaintings.users;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter @Setter
public class ProfileForm {

    @NotBlank
    @Length(min = 2, max = 20, message = "최소 2자 이상 입력해주세요.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$")
    private String nickname;

    private String profileImage;

    @Pattern(regexp = "^01(?:[0|1|6|7|8|9])([0-9]{3,4})([0-9]{4})$|")
    private String phoneNumber;

    private String address1;

    private String address2;

    private String address3;

}
