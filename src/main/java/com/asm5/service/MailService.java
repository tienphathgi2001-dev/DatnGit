package com.asm5.service;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public interface MailService {
    @Data
    @Builder
    public static class Mail {
        @Default
        private String from = "WebShop <web-shop@gmail.com>";
        private String to, cc, bcc, subject, body, filenames;
    }

    void send(Mail mail);

    default void send(String to, String subject, String body) {
        Mail mail =

                Mail.builder().to(to).subject(subject).body(body).build();

        this.send(mail);
    }

    void push(Mail mail);

    default void push(String to, String subject, String body) {
        this.push(Mail.builder().to(to).subject(subject).body(body).build());
    }
}
