package com.homepaintings.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailService {

    public void sendEmail(EmailMessage emailMessage) {
        log.info("이메일 전송됨 - 내용: {}", emailMessage.getMessage());
    }

}
