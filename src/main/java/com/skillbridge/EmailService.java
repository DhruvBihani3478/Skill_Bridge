
package com.skillbridge;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    private static final HttpClient HTTP_CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

    public static void sendEmail(
            String recipient,
            String subject,
            String body) throws Exception {

        String resendKey = System.getenv("RESEND_API_KEY");

        if (resendKey != null && !resendKey.isBlank()) {
            sendWithResend(recipient, subject, body, resendKey);
        } else {
            sendWithGmail(recipient, subject, body);
        }
    }

    // Used on Render.
    private static void sendWithResend(
            String recipient,
            String subject,
            String body,
            String apiKey) throws Exception {

        String json =
                "{"
                + "\"from\":\"onboarding@resend.dev\","
                + "\"to\":[\"" + escapeJson(recipient) + "\"],"
                + "\"subject\":\"" + escapeJson(subject) + "\","
                + "\"text\":\"" + escapeJson(body) + "\""
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response =
                HTTP_CLIENT.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "Resend email failed. HTTP status: "
                    + response.statusCode()
                    + ". Response: " + response.body()
            );
        }
    }

    // Used during local development.
    private static void sendWithGmail(
            String recipient,
            String subject,
            String body) throws Exception {

        String gmailUser =
                System.getenv("SKILLBRIDGE_GMAIL_USER");

        String gmailPassword =
                System.getenv("SKILLBRIDGE_GMAIL_APP_PASSWORD");

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
        properties.put("mail.smtp.connectiontimeout", "15000");
        properties.put("mail.smtp.timeout", "15000");
        properties.put("mail.smtp.writetimeout", "15000");

        Session session = Session.getInstance(
                properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication
                    getPasswordAuthentication() {

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

    private static String escapeJson(String value) {

        StringBuilder escaped = new StringBuilder();

        for (char c : value.toCharArray()) {

            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;

                case '\\':
                    escaped.append("\\\\");
                    break;

                case '\b':
                    escaped.append("\\b");
                    break;

                case '\f':
                    escaped.append("\\f");
                    break;

                case '\n':
                    escaped.append("\\n");
                    break;

                case '\r':
                    escaped.append("\\r");
                    break;

                case '\t':
                    escaped.append("\\t");
                    break;

                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }

        return escaped.toString();
    }
}
    