
package com.skillbridge;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private int countForUser(
            Connection connection,
            String sql,
            long userId) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, userId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
        }

        return 0;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Protect the dashboard from users who are not logged in.
        if (session == null ||
                !(session.getAttribute("userId") instanceof Number)) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html"
            );
            return;
        }

        long userId = ((Number) session.getAttribute("userId"))
                .longValue();

        String teachSql =
                "SELECT COUNT(*) FROM USER_SKILLS " +
                "WHERE USER_ID = ? AND SKILL_TYPE = 'TEACH'";

        String learnSql =
                "SELECT COUNT(*) FROM USER_SKILLS " +
                "WHERE USER_ID = ? AND SKILL_TYPE = 'LEARN'";

        String partnersSql =
                "SELECT COUNT(DISTINCT teacher.USER_ID) " +
                "FROM USER_SKILLS learner " +
                "JOIN USER_SKILLS teacher " +
                "ON learner.SKILL_ID = teacher.SKILL_ID " +
                "WHERE learner.USER_ID = ? " +
                "AND learner.SKILL_TYPE = 'LEARN' " +
                "AND teacher.SKILL_TYPE = 'TEACH' " +
                "AND teacher.USER_ID <> ?";

        String requestsSql =
                "SELECT COUNT(*) FROM EXCHANGE_REQUESTS " +
                "WHERE RECEIVER_ID = ? AND STATUS = 'PENDING'";

        try (Connection connection = DBConnection.getConnection()) {

            int teachCount = countForUser(
                    connection, teachSql, userId
            );

            int learnCount = countForUser(
                    connection, learnSql, userId
            );

            int partnerCount;

            try (PreparedStatement statement =
                         connection.prepareStatement(partnersSql)) {

                statement.setLong(1, userId);
                statement.setLong(2, userId);

                try (ResultSet result = statement.executeQuery()) {
                    result.next();
                    partnerCount = result.getInt(1);
                }
            }

            int pendingRequests = countForUser(
                    connection, requestsSql, userId
            );

            request.setAttribute(
                    "fullName", session.getAttribute("fullName")
            );

            request.setAttribute("teachCount", teachCount);
            request.setAttribute("learnCount", learnCount);
            request.setAttribute("partnerCount", partnerCount);
            request.setAttribute("pendingRequests", pendingRequests);

            request.getRequestDispatcher("/dashboard.jsp")
                    .forward(request, response);

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to load dashboard.", e
            );
        }
    }
}
