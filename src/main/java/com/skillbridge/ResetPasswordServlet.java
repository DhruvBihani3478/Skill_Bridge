
package com.skillbridge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getParameter("token");

        if (token == null || token.isBlank()) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password?error=invalid"
            );
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {

            String sql =
                    "SELECT TOKEN_ID FROM PASSWORD_RESET_TOKENS "
                    + "WHERE TOKEN_HASH = ? "
                    + "AND USED_AT IS NULL "
                    + "AND EXPIRES_AT > CURRENT_TIMESTAMP";

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, sha256(token));

                try (ResultSet result = statement.executeQuery()) {

                    if (!result.next()) {
                        response.sendRedirect(
                                request.getContextPath()
                                + "/forgot-password?error=invalid"
                        );
                        return;
                    }
                }
            }

            request.setAttribute("token", token);

            request.getRequestDispatcher("/reset-password.jsp")
                   .forward(request, response);

        } catch (Exception e) {

            throw new ServletException(
                    "Unable to validate reset token.", e
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String token = request.getParameter("token");
        String password = request.getParameter("password");
        String confirmPassword =
                request.getParameter("confirmPassword");

        if (token == null || token.isBlank()
                || password == null || confirmPassword == null
                || password.isBlank() || confirmPassword.isBlank()) {

            response.sendRedirect(
                    request.getContextPath()
                    + "/forgot-password?error=invalid"
            );
            return;
        }

        if (password.length() < 8 || password.length() > 72) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/reset-password?token="
                    + java.net.URLEncoder.encode(
                            token, StandardCharsets.UTF_8
                    )
                    + "&error=password"
            );
            return;
        }

        if (!password.equals(confirmPassword)) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/reset-password?token="
                    + java.net.URLEncoder.encode(
                            token, StandardCharsets.UTF_8
                    )
                    + "&error=mismatch"
            );
            return;
        }

        String tokenHash;

        try {
            tokenHash = sha256(token);
        } catch (Exception e) {
            throw new ServletException(
                    "Unable to process reset token.", e
            );
        }

        String newPasswordHash = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                long userId;
                long tokenId;

                String selectSql =
                        "SELECT TOKEN_ID, USER_ID "
                        + "FROM PASSWORD_RESET_TOKENS "
                        + "WHERE TOKEN_HASH = ? "
                        + "AND USED_AT IS NULL "
                        + "AND EXPIRES_AT > CURRENT_TIMESTAMP "
                        + "FOR UPDATE";

                try (PreparedStatement statement =
                             connection.prepareStatement(selectSql)) {

                    statement.setString(1, tokenHash);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();

                            response.sendRedirect(
                                    request.getContextPath()
                                    + "/forgot-password?error=invalid"
                            );
                            return;
                        }

                        tokenId = result.getLong("TOKEN_ID");
                        userId = result.getLong("USER_ID");
                    }
                }

                String updatePasswordSql =
                        "UPDATE USERS SET PASSWORD_HASH = ? "
                        + "WHERE USER_ID = ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     updatePasswordSql)) {

                    statement.setString(1, newPasswordHash);
                    statement.setLong(2, userId);

                    if (statement.executeUpdate() != 1) {
                        throw new ServletException(
                                "Unable to update account password."
                        );
                    }
                }

                String updateTokenSql =
                        "UPDATE PASSWORD_RESET_TOKENS "
                        + "SET USED_AT = CURRENT_TIMESTAMP "
                        + "WHERE TOKEN_ID = ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     updateTokenSql)) {

                    statement.setLong(1, tokenId);
                    statement.executeUpdate();
                }

                // Invalidate all other outstanding reset tokens
                // belonging to this user.
                String invalidateSql =
                        "UPDATE PASSWORD_RESET_TOKENS "
                        + "SET USED_AT = CURRENT_TIMESTAMP "
                        + "WHERE USER_ID = ? "
                        + "AND USED_AT IS NULL "
                        + "AND TOKEN_ID <> ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(invalidateSql)) {

                    statement.setLong(1, userId);
                    statement.setLong(2, tokenId);
                    statement.executeUpdate();
                }

                connection.commit();

                response.sendRedirect(
                        request.getContextPath()
                        + "/login.html?reset=success"
                );

            } catch (Exception e) {

                connection.rollback();
                throw e;
            }

        } catch (Exception e) {

            throw new ServletException(
                    "Unable to reset password.", e
            );
        }
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
