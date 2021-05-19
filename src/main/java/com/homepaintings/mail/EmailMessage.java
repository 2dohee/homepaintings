package com.homepaintings.mail;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class EmailMessage {

    private String to;

    private String message;

    private String from;

}
