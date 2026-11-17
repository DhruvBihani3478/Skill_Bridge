
package com.skillbridge;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/requests")
public class ExchangeRequestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int userId = ((Number) session.getAttribute("userId")).intValue();

        List<Map<String, Object>> requests = new ArrayList<>();

        String sql =
            "SELECT er.REQUEST_ID, er.SENDER_ID, er.RECEIVER_ID, " +
            "er.STATUS, er.CREATED_AT, " +
            "sender.FULL_NAME AS SENDER_NAME, " +
            "receiver.FULL_NAME AS RECEIVER_NAME, " +
            "offered.SKILL_NAME AS OFFERED_SKILL, " +
            "requested.SKILL_NAME AS REQUESTED_SKILL " +
            "FROM EXCHANGE_REQUESTS er " +
            "JOIN USERS sender ON sender.USER_ID = er.SENDER_ID " +
            "JOIN USERS receiver ON receiver.USER_ID = er.RECEIVER_ID " +
            "JOIN SKILLS offered ON offered.SKILL_ID = er.OFFERED_SKILL_ID " +
            "JOIN SKILLS requested ON requested.SKILL_ID = er.REQUESTED_SKILL_ID " +
            "WHERE er.SENDER_ID = ? OR er.RECEIVER_ID = ? " +
            "ORDER BY er.CREATED_AT DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();

                    item.put("requestId", rs.getInt("REQUEST_ID"));
                    item.put("senderId", rs.getInt("SENDER_ID"));
                    item.put("receiverId", rs.getInt("RECEIVER_ID"));
                    item.put("senderName", rs.getString("SENDER_NAME"));
                    item.put("receiverName", rs.getString("RECEIVER_NAME"));
                    item.put("offeredSkill", rs.getString("OFFERED_SKILL"));
                    item.put("requestedSkill", rs.getString("REQUESTED_SKILL"));
                    item.put("status", rs.getString("STATUS"));
                    item.put("createdAt", rs.getTimestamp("CREATED_AT"));
                    item.put("incoming", rs.getInt("RECEIVER_ID") == userId);

                    requests.add(item);
                }
            }

            request.setAttribute("requests", requests);

            request.getRequestDispatcher("/requests.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Unable to load exchange requests.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int userId = ((Number) session.getAttribute("userId")).intValue();

        String action = request.getParameter("action");

        if ("send".equals(action)) {
            sendRequest(request, response, userId);
        } else if ("accept".equals(action) || "reject".equals(action)) {
            updateRequest(request, response, userId, action);
        } else {
            response.sendRedirect(
                request.getContextPath() + "/requests?error=invalid"
            );
        }
    }

    private void sendRequest(HttpServletRequest request,
                             HttpServletResponse response,
                             int senderId)
            throws IOException, ServletException {

        int receiverId;
        int offeredSkillId;
        int requestedSkillId;

        try {
            receiverId = Integer.parseInt(request.getParameter("receiverId"));
            offeredSkillId = Integer.parseInt(request.getParameter("offeredSkillId"));
            requestedSkillId = Integer.parseInt(request.getParameter("requestedSkillId"));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/partners?error=invalid");
            return;
        }

        if (senderId == receiverId || offeredSkillId <= 0 || requestedSkillId <= 0) {
            response.sendRedirect(request.getContextPath() + "/partners?error=invalid");
            return;
        }

        // Optimized skill validation using EXISTS.
        String validationSql =
            "SELECT " +
            "CHECK_USER_SKILL(?, ?, 'TEACH') AS OFFER_VALID, " +
            "CHECK_USER_SKILL(?, ?, 'LEARN') AS LEARN_VALID, " +
            "CHECK_USER_SKILL(?, ?, 'TEACH') AS RECEIVER_VALID " +
            "FROM DUAL";

        // Check for an existing pending request using EXISTS.
        String duplicateSql =
            "SELECT CASE WHEN EXISTS (" +
            "SELECT 1 FROM EXCHANGE_REQUESTS " +
            "WHERE SENDER_ID = ? AND RECEIVER_ID = ? " +
            "AND OFFERED_SKILL_ID = ? AND REQUESTED_SKILL_ID = ? " +
            "AND STATUS = 'PENDING') " +
            "THEN 1 ELSE 0 END AS DUPLICATE " +
            "FROM DUAL";

        String insertSql =
            "INSERT INTO EXCHANGE_REQUESTS " +
            "(SENDER_ID, RECEIVER_ID, OFFERED_SKILL_ID, REQUESTED_SKILL_ID) " +
            "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            // Validate all three skill conditions.
            try (PreparedStatement ps = con.prepareStatement(validationSql)) {

                ps.setInt(1, senderId);
                ps.setInt(2, offeredSkillId);

                ps.setInt(3, senderId);
                ps.setInt(4, requestedSkillId);

                ps.setInt(5, receiverId);
                ps.setInt(6, requestedSkillId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()
                            || rs.getInt("OFFER_VALID") == 0
                            || rs.getInt("LEARN_VALID") == 0
                            || rs.getInt("RECEIVER_VALID") == 0) {

                        response.sendRedirect(
                            request.getContextPath() + "/partners?error=invalid"
                        );
                        return;
                    }
                }
            }

            // Check whether the same pending request already exists.
            try (PreparedStatement ps = con.prepareStatement(duplicateSql)) {

                ps.setInt(1, senderId);
                ps.setInt(2, receiverId);
                ps.setInt(3, offeredSkillId);
                ps.setInt(4, requestedSkillId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("DUPLICATE") == 1) {
                        response.sendRedirect(
                            request.getContextPath() + "/requests?error=duplicate"
                        );
                        return;
                    }
                }
            }

            // Insert the exchange request.
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {

                ps.setInt(1, senderId);
                ps.setInt(2, receiverId);
                ps.setInt(3, offeredSkillId);
                ps.setInt(4, requestedSkillId);

                ps.executeUpdate();
            }

            // Fetch email details after the request has been saved.
            String emailSql =
                "SELECT sender.FULL_NAME AS SENDER_NAME, " +
                "receiver.EMAIL AS RECEIVER_EMAIL, " +
                "receiver.FULL_NAME AS RECEIVER_NAME, " +
                "offered.SKILL_NAME AS OFFERED_SKILL, " +
                "requested.SKILL_NAME AS REQUESTED_SKILL " +
                "FROM USERS sender, USERS receiver, SKILLS offered, SKILLS requested " +
                "WHERE sender.USER_ID = ? " +
                "AND receiver.USER_ID = ? " +
                "AND offered.SKILL_ID = ? " +
                "AND requested.SKILL_ID = ?";

            try (PreparedStatement ps = con.prepareStatement(emailSql)) {

                ps.setInt(1, senderId);
                ps.setInt(2, receiverId);
                ps.setInt(3, offeredSkillId);
                ps.setInt(4, requestedSkillId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {

                        String body =
                            "Hello " + rs.getString("RECEIVER_NAME") + ",\n\n" +
                            "You have received a new SkillBridge exchange request!\n\n" +
                            "From: " + rs.getString("SENDER_NAME") + "\n" +
                            "They can teach: " + rs.getString("OFFERED_SKILL") + "\n" +
                            "They want to learn: " + rs.getString("REQUESTED_SKILL") + "\n\n" +
                            "Log in to SkillBridge to view and respond to this request.\n\n" +
                            "Regards,\nSkillBridge Team";

                        sendEmailSafely(
                            rs.getString("RECEIVER_EMAIL"),
                            "New SkillBridge Exchange Request",
                            body
                        );
                    }
                }

            } catch (Exception emailError) {
                log("Unable to prepare new request notification.", emailError);
            }

            response.sendRedirect(
                request.getContextPath() + "/requests?success=sent"
            );

        } catch (SQLException e) {
            throw new ServletException("Unable to send exchange request.", e);
        }
    }

    private void updateRequest(HttpServletRequest request,
                               HttpServletResponse response,
                               int receiverId,
                               String action)
            throws IOException, ServletException {

        int requestId;

        try {
            requestId = Integer.parseInt(request.getParameter("requestId"));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/requests?error=invalid");
            return;
        }

        String newStatus = "accept".equals(action) ? "ACCEPTED" : "REJECTED";

        String sql =
            "UPDATE EXCHANGE_REQUESTS " +
            "SET STATUS = ? " +
            "WHERE REQUEST_ID = ? " +
            "AND RECEIVER_ID = ? " +
            "AND STATUS = 'PENDING'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, requestId);
            ps.setInt(3, receiverId);

            int updated = ps.executeUpdate();

            if (updated > 0) {

                String emailSql =
                    "SELECT sender.EMAIL AS SENDER_EMAIL, " +
                    "sender.FULL_NAME AS SENDER_NAME, " +
                    "receiver.FULL_NAME AS RECEIVER_NAME, " +
                    "offered.SKILL_NAME AS OFFERED_SKILL, " +
                    "requested.SKILL_NAME AS REQUESTED_SKILL " +
                    "FROM EXCHANGE_REQUESTS er " +
                    "JOIN USERS sender ON sender.USER_ID = er.SENDER_ID " +
                    "JOIN USERS receiver ON receiver.USER_ID = er.RECEIVER_ID " +
                    "JOIN SKILLS offered ON offered.SKILL_ID = er.OFFERED_SKILL_ID " +
                    "JOIN SKILLS requested ON requested.SKILL_ID = er.REQUESTED_SKILL_ID " +
                    "WHERE er.REQUEST_ID = ?";

                try (PreparedStatement emailPs = con.prepareStatement(emailSql)) {

                    emailPs.setInt(1, requestId);

                    try (ResultSet rs = emailPs.executeQuery()) {
                        if (rs.next()) {

                            String statusMessage =
                                "ACCEPTED".equals(newStatus)
                                    ? "Your SkillBridge exchange request has been ACCEPTED!"
                                    : "Your SkillBridge exchange request has been REJECTED.";

                            String body =
                                "Hello " + rs.getString("SENDER_NAME") + ",\n\n" +
                                statusMessage + "\n\n" +
                                "Responded by: " + rs.getString("RECEIVER_NAME") + "\n" +
                                "Your offered skill: " + rs.getString("OFFERED_SKILL") + "\n" +
                                "Requested skill: " + rs.getString("REQUESTED_SKILL") + "\n\n" +
                                "Log in to SkillBridge to view your exchange requests.\n\n" +
                                "Regards,\nSkillBridge Team";

                            sendEmailSafely(
                                rs.getString("SENDER_EMAIL"),
                                "SkillBridge Request " + newStatus,
                                body
                            );
                        }
                    }

                } catch (Exception emailError) {
                    log("Unable to prepare request status notification.", emailError);
                }

                response.sendRedirect(
                    request.getContextPath() + "/requests?success=" + newStatus.toLowerCase()
                );

            } else {
                response.sendRedirect(
                    request.getContextPath() + "/requests?error=notfound"
                );
            }

        } catch (SQLException e) {
            throw new ServletException("Unable to update exchange request.", e);
        }
    }

    private void sendEmailSafely(String recipient,
                                 String subject,
                                 String body) {

        try {
            EmailService.sendEmail(recipient, subject, body);
            log("SkillBridge notification email sent to " + recipient);
        } catch (Exception e) {
            log("SkillBridge email notification failed.", e);
        }
    }
}
