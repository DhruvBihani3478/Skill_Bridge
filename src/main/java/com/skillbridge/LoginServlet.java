
package com.skillbridge;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Locale;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final int MAX_ATTEMPTS = 5;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader(
                "Referrer-Policy",
                "strict-origin-when-cross-origin"
        );

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || password == null
                || email.isBlank() || password.isBlank()) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html?error=empty"
            );
            return;
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        String userSql =
                "SELECT USER_ID, FULL_NAME, EMAIL, PASSWORD_HASH "
              + "FROM USERS WHERE EMAIL = ?";

        String attemptSql =
                "MERGE INTO LOGIN_ATTEMPTS t "
              + "USING (SELECT ? AS EMAIL FROM DUAL) s "
              + "ON (t.EMAIL = s.EMAIL) "
              + "WHEN NOT MATCHED THEN "
              + "INSERT (EMAIL, FAILED_ATTEMPTS, LAST_ATTEMPT) "
              + "VALUES (s.EMAIL, 0, SYSTIMESTAMP)";

        String lockSql =
                "SELECT FAILED_ATTEMPTS, LOCKED_UNTIL, "
              + "SYSTIMESTAMP AS DB_NOW "
              + "FROM LOGIN_ATTEMPTS "
              + "WHERE EMAIL = ? FOR UPDATE";

        String resetExpiredSql =
                "UPDATE LOGIN_ATTEMPTS "
              + "SET FAILED_ATTEMPTS = 0, LOCKED_UNTIL = NULL "
              + "WHERE EMAIL = ?";

        String deleteSql =
                "DELETE FROM LOGIN_ATTEMPTS WHERE EMAIL = ?";

        String updateSql =
                "UPDATE LOGIN_ATTEMPTS "
              + "SET FAILED_ATTEMPTS = ?, "
              + "LOCKED_UNTIL = CASE "
              + "WHEN ? >= ? "
              + "THEN SYSTIMESTAMP + INTERVAL '15' MINUTE "
              + "ELSE NULL END, "
              + "LAST_ATTEMPT = SYSTIMESTAMP "
              + "WHERE EMAIL = ?";

        try (Connection connection = DBConnection.getConnection()) {

            connection.setAutoCommit(true);

            long userId = 0;
            String fullName = null;
            String registeredEmail = null;
            String storedHash = null;
            boolean userFound = false;

            // Find the registered account.
            try (PreparedStatement statement =
                         connection.prepareStatement(userSql)) {

                statement.setString(1, email);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        userFound = true;
                        userId = result.getLong("USER_ID");
                        fullName = result.getString("FULL_NAME");
                        registeredEmail = result.getString("EMAIL");
                        storedHash = result.getString("PASSWORD_HASH");
                    }
                }
            }

            // Create the attempt-tracking row if necessary.
            try (PreparedStatement statement =
                         connection.prepareStatement(attemptSql)) {

                statement.setString(1, email);

                try {
                    statement.executeUpdate();

                } catch (SQLException e) {
                    if (e.getErrorCode() != 1) {
                        throw e;
                    }
                }
            }

            connection.setAutoCommit(false);

            try {

                int failedAttempts = 0;
                Timestamp lockedUntil = null;
                Timestamp databaseTime = null;

                // Lock the attempt record for this email.
                try (PreparedStatement statement =
                             connection.prepareStatement(lockSql)) {

                    statement.setString(1, email);

                    try (ResultSet result = statement.executeQuery()) {

                        if (result.next()) {
                            failedAttempts =
                                    result.getInt("FAILED_ATTEMPTS");

                            lockedUntil =
                                    result.getTimestamp("LOCKED_UNTIL");

                            databaseTime =
                                    result.getTimestamp("DB_NOW");
                        }
                    }
                }

                if (databaseTime == null) {
                    throw new SQLException(
                            "Login attempt record not found."
                    );
                }

                // Reject login while lockout is active.
                if (lockedUntil != null
                        && lockedUntil.after(databaseTime)) {

                    long expiryMillis = lockedUntil.getTime();

                    connection.commit();

                    response.sendRedirect(
                            request.getContextPath()
                            + "/login.html?error=locked&until="
                            + expiryMillis
                    );

                    return;
                }

                // Reset ONLY when an existing lockout has expired.
                if (lockedUntil != null
                        && !lockedUntil.after(databaseTime)) {

                    failedAttempts = 0;

                    try (PreparedStatement statement =
                                 connection.prepareStatement(
                                         resetExpiredSql)) {

                        statement.setString(1, email);
                        statement.executeUpdate();
                    }
                }

                // Verify password.
                boolean passwordMatches = false;

                if (userFound && storedHash != null
                        && !storedHash.isBlank()) {

                    try {
                        passwordMatches =
                                BCrypt.checkpw(password, storedHash);

                    } catch (IllegalArgumentException e) {
                        passwordMatches = false;
                    }
                }

                // Successful login.
                if (passwordMatches) {

                    try (PreparedStatement statement =
                                 connection.prepareStatement(deleteSql)) {

                        statement.setString(1, email);
                        statement.executeUpdate();
                    }

                    connection.commit();

                    HttpSession oldSession =
                            request.getSession(false);

                    if (oldSession != null) {
                        oldSession.invalidate();
                    }

                    HttpSession session =
                            request.getSession(true);

                    session.setAttribute("userId", userId);
                    session.setAttribute("fullName", fullName);
                    session.setAttribute("email", registeredEmail);

                    session.setMaxInactiveInterval(30 * 60);

                    response.sendRedirect(
                            request.getContextPath() + "/dashboard"
                    );

                    return;
                }

                // Incorrect password: increment attempts.
                failedAttempts++;

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {

                    statement.setInt(1, failedAttempts);
                    statement.setInt(2, failedAttempts);
                    statement.setInt(3, MAX_ATTEMPTS);
                    statement.setString(4, email);

                    statement.executeUpdate();
                }

                // Read the new lockout expiry if the limit was reached.
                Timestamp newLockedUntil = null;

                if (failedAttempts >= MAX_ATTEMPTS) {

                    String expirySql =
                            "SELECT LOCKED_UNTIL "
                          + "FROM LOGIN_ATTEMPTS WHERE EMAIL = ?";

                    try (PreparedStatement statement =
                                 connection.prepareStatement(expirySql)) {

                        statement.setString(1, email);

                        try (ResultSet result = statement.executeQuery()) {
                            if (result.next()) {
                                newLockedUntil =
                                        result.getTimestamp("LOCKED_UNTIL");
                            }
                        }
                    }
                }

                connection.commit();

                if (failedAttempts >= MAX_ATTEMPTS
                        && newLockedUntil != null) {

                    response.sendRedirect(
                            request.getContextPath()
                            + "/login.html?error=locked&until="
                            + newLockedUntil.getTime()
                    );

                } else {

                    int remaining = MAX_ATTEMPTS - failedAttempts;

                    response.sendRedirect(
                            request.getContextPath()
                            + "/login.html?error=invalid&remaining="
                            + remaining
                    );
                }

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to log in.", e
            );
        }
    }
}
