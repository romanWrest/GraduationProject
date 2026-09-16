package ru.dstu.dormitory.notifications_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.notifications_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.notifications_service.config.AppMailProperties;
import ru.dstu.dormitory.notifications_service.domain.enums.EmailStatus;
import ru.dstu.dormitory.notifications_service.domain.model.EmailLog;
import ru.dstu.dormitory.notifications_service.domain.repo.EmailLogRepository;
import ru.dstu.dormitory.notifications_service.util.LogPatterns;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/**
 * Отправка email через JavaMailSender с записью результата в email_log.
 * <p>Не бросает исключение — статус FAILED записывается в журнал, чтобы не ронять основной flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final AppMailProperties mailProperties;

    @Transactional
    @LogMethod(value = "Отправка email", logArgs = {"userId", "to", "template", "subject"})
    public EmailStatus send(UUID userId, String to, String template, String subject, String htmlBody) {
        long start = System.currentTimeMillis();
        log.info(LogPatterns.EMAIL_SENDING, to, template);

        EmailLog.EmailLogBuilder logBuilder = EmailLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .toAddress(to)
                .template(template)
                .subject(subject)
                .sentAt(Instant.now());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(buildFromAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);

            emailLogRepository.save(logBuilder.status(EmailStatus.SENT).build());
            log.info(LogPatterns.EMAIL_SENT, to, template, System.currentTimeMillis() - start);
            return EmailStatus.SENT;
        } catch (MessagingException | MailException | UnsupportedEncodingException ex) {
            String reason = ex.getMessage();
            emailLogRepository.save(logBuilder
                    .status(EmailStatus.FAILED)
                    .errorMessage(reason)
                    .build());
            log.error(LogPatterns.EMAIL_FAILED, to, template, reason);
            return EmailStatus.FAILED;
        }
    }

    /**
     * Записать неудачу до отправки (например, не удалось получить email пользователя из auth).
     */
    @Transactional
    public void recordSkip(UUID userId, String fakeAddress, String template, String subject, String reason) {
        emailLogRepository.save(EmailLog.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .toAddress(fakeAddress == null ? "<unknown>" : fakeAddress)
                .template(template)
                .subject(subject)
                .status(EmailStatus.FAILED)
                .errorMessage(reason)
                .sentAt(Instant.now())
                .build());
    }

    private InternetAddress buildFromAddress() throws UnsupportedEncodingException {
        return new InternetAddress(mailProperties.getFromAddress(),
                mailProperties.getFromName(), StandardCharsets.UTF_8.name());
    }
}
