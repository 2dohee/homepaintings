package com.homepaintings.order;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;

@Getter @Setter
public class OrderForm {

    @NotBlank
    private String receiver;

    @Pattern(regexp = "^01(?:[0|1|6|7|8|9])([0-9]{3,4})([0-9]{4})$|")
    private String phoneNumber;

    @NotBlank
    private String address1;

    @NotBlank
    private String address2;

    @NotBlank
    private String address3;

    private ArrayList<Long> cartIdList = new ArrayList<>();

}
