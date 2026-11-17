
package com.skillbridge;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    public static void sendEmail(
            String recipient,
            String subject,
            String body) throws Exception {

        String gmailUser = System.getenv("SKILLBRIDGE_GMAIL_USER");
        String gmailPassword = System.getenv("SKILLBRIDGE_GMAIL_APP_PASSWORD");

        if (gmailUser == null || gmailPassword == null
                || gmailUser.isBlank() || gmailPassword.isBlank()) {
            throw new IllegalStateException(
                "Gmail environment variables are not configured."
            );
        }

        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(
            properties,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        gmailUser, gmailPassword
                    );
                }
            }
        );

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(gmailUser));
        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(recipient)
        );

        message.setSubject(subject, "UTF-8");
        message.setText(body, "UTF-8");

        Transport.send(message);
    }
}
