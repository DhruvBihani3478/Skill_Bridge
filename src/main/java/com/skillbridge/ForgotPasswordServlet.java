
package com.skillbridge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private static final Logger LOGGER =
            Logger.getLogger(ForgotPasswordServlet.class.getName());

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/forgot-password.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");

        if (email == null || email.isBlank()) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password?error=empty"
            );
            return;
        }

        email = email.trim().toLowerCase();

        String userSql =
                "SELECT USER_ID, FULL_NAME FROM USERS WHERE EMAIL = ?";

        String insertSql =
                "INSERT INTO PASSWORD_RESET_TOKENS "
                + "(USER_ID, TOKEN_HASH, EXPIRES_AT) "
                + "VALUES (?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement userStatement =
                     connection.prepareStatement(userSql)) {

            userStatement.setString(1, email);

            try (ResultSet result = userStatement.executeQuery()) {

                if (result.next()) {

                    long userId = result.getLong("USER_ID");
                    String fullName = result.getString("FULL_NAME");

                    // Generate a secure random token.
                    byte[] randomBytes = new byte[32];
                    RANDOM.nextBytes(randomBytes);

                    String token = Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(randomBytes);

                    // Store only the SHA-256 hash of the token.
                    String tokenHash = sha256(token);

                    Timestamp expiry = Timestamp.from(
                            Instant.now().plus(15, ChronoUnit.MINUTES)
                    );

                    try (PreparedStatement insertStatement =
                                 connection.prepareStatement(insertSql)) {

                        insertStatement.setLong(1, userId);
                        insertStatement.setString(2, tokenHash);
                        insertStatement.setTimestamp(3, expiry);

                        insertStatement.executeUpdate();
                    }

                    // Build the password-reset URL.
                    String port = "";

                    if (request.getServerPort() != 80
                            && request.getServerPort() != 443) {
                        port = ":" + request.getServerPort();
                    }

                    String resetUrl =
                            request.getScheme() + "://"
                            + request.getServerName()
                            + port
                            + request.getContextPath()
                            + "/reset-password?token=" + token;

                    String subject =
                            "Reset Your SkillBridge Password";

                    String body =
                            "Hello " + fullName + ",\n\n"
                            + "We received a request to reset your "
                            + "SkillBridge password.\n\n"
                            + "Use the link below to choose a new password:\n"
                            + resetUrl + "\n\n"
                            + "This link expires in 15 minutes and can "
                            + "only be used once.\n\n"
                            + "If you did not request this, you can "
                            + "ignore this email.\n\n"
                            + "SkillBridge Team";

                    try {
                        EmailService.sendEmail(
                                email, subject, body
                        );
                    } catch (Exception emailError) {
                        LOGGER.log(
                                Level.SEVERE,
                                "Unable to send password reset email.",
                                emailError
                        );
                    }
                }
            }

        } catch (Exception e) {

            throw new ServletException(
                    "Unable to process password reset request.", e
            );
        }

        // Always show the same response, whether the email exists or not.
        response.sendRedirect(
                request.getContextPath()
                + "/forgot-password?sent=true"
        );
    }

    private String sha256(String value) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(
                value.getBytes(StandardCharsets.UTF_8)
        );

        StringBuilder hex = new StringBuilder();

        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }
}
