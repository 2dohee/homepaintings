package com.homepaintings.mail;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EmailServiceTest {

    @Autowired private EmailService emailService;
    @Mock private Appender mockedAppender;

    @BeforeEach
    public void setup() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).addAppender(mockedAppender);
    }

    @Test
    @DisplayName("이메일이 전송되는지 확인")
    public void sendEmail() {
        EmailMessage emailMessage = EmailMessage.builder()
                .to("test@email.com")
                .message("테스트 이메일")
                .from("test")
                .build();
        emailService.sendEmail(emailMessage);

        ArgumentCaptor<Appender> argumentCaptor = ArgumentCaptor.forClass(Appender.class);
        verify(mockedAppender).doAppend(argumentCaptor.capture());

        assertEquals(1, argumentCaptor.getAllValues().size());
        assertEquals("이메일 전송됨 - 내용: 테스트 이메일", ((LoggingEvent) argumentCaptor.getAllValues().get(0)).getFormattedMessage());
    }

    @AfterEach
    public void tearDown() {
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).detachAppender(mockedAppender);
    }

}

