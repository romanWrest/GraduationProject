package ru.dstu.dormitory.notifications_service.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import ru.dstu.dormitory.notifications_service.config.AppMailProperties;
import ru.dstu.dormitory.notifications_service.domain.enums.EmailStatus;
import ru.dstu.dormitory.notifications_service.domain.model.EmailLog;
import ru.dstu.dormitory.notifications_service.domain.repo.EmailLogRepository;

import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    JavaMailSender mailSender;

    @Mock
    EmailLogRepository emailLogRepository;

    AppMailProperties mailProperties;

    @InjectMocks
    EmailService service;

    @BeforeEach
    void setup() {
        mailProperties = new AppMailProperties();
        service = new EmailService(mailSender, emailLogRepository, mailProperties);
        when(mailSender.createMimeMessage()).thenReturn(
                new MimeMessage(jakarta.mail.Session.getInstance(new Properties())));
    }

    @Test
    void successWritesEmailLogWithSent() {
        EmailStatus status = service.send(UUID.randomUUID(), "to@example.com",
                "welcome", "Hi", "<p>Hi</p>");

        assertThat(status).isEqualTo(EmailStatus.SENT);

        ArgumentCaptor<EmailLog> captor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EmailStatus.SENT);
        assertThat(captor.getValue().getTemplate()).isEqualTo("welcome");
    }

    @Test
    void smtpFailureWritesFailedAndDoesNotThrow() {
        doThrow(new MailSendException("smtp down"))
                .when(mailSender).send(any(MimeMessage.class));

        EmailStatus status = service.send(UUID.randomUUID(), "to@example.com",
                "welcome", "Hi", "<p>Hi</p>");

        assertThat(status).isEqualTo(EmailStatus.FAILED);
        ArgumentCaptor<EmailLog> captor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EmailStatus.FAILED);
        assertThat(captor.getValue().getErrorMessage()).contains("smtp down");
    }
}
