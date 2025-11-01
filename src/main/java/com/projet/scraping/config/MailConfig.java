package com.projet.scraping.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSenderFallback() {
        // Fallback no-op sender (sans host) pour environnements DEV
        // MailServiceImpl attrapera les exceptions au send() si aucune conf SMTP n'est définie.
        return new JavaMailSenderImpl();
    }
}
