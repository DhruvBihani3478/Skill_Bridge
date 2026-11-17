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

@WebServlet("/partners")
public class PartnerServlet extends HttpServlet {

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

        List<Map<String, Object>> partners = new ArrayList<>();
        List<Map<String, Object>> myTeachSkills = new ArrayList<>();

        String partnerSql =
            "SELECT u.USER_ID, u.FULL_NAME, u.EMAIL, " +
            "s.SKILL_ID, s.SKILL_NAME " +
            "FROM USER_SKILLS mine " +
            "JOIN USER_SKILLS theirs " +
            "ON theirs.SKILL_ID = mine.SKILL_ID " +
            "AND theirs.SKILL_TYPE = 'TEACH' " +
            "JOIN USERS u ON u.USER_ID = theirs.USER_ID " +
            "JOIN SKILLS s ON s.SKILL_ID = mine.SKILL_ID " +
            "WHERE mine.USER_ID = ? " +
            "AND mine.SKILL_TYPE = 'LEARN' " +
            "AND theirs.USER_ID <> ? " +
            "ORDER BY s.SKILL_NAME, u.FULL_NAME";

        String teachSql =
            "SELECT s.SKILL_ID, s.SKILL_NAME " +
            "FROM USER_SKILLS us " +
            "JOIN SKILLS s ON s.SKILL_ID = us.SKILL_ID " +
            "WHERE us.USER_ID = ? " +
            "AND us.SKILL_TYPE = 'TEACH' " +
            "ORDER BY s.SKILL_NAME";

        try (Connection con = DBConnection.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(partnerSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> partner = new HashMap<>();

                        partner.put("userId", rs.getInt("USER_ID"));
                        partner.put("fullName", rs.getString("FULL_NAME"));
                        partner.put("email", rs.getString("EMAIL"));
                        partner.put("skillId", rs.getInt("SKILL_ID"));
                        partner.put("skillName", rs.getString("SKILL_NAME"));

                        partners.add(partner);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(teachSql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> skill = new HashMap<>();

                        skill.put("id", rs.getInt("SKILL_ID"));
                        skill.put("name", rs.getString("SKILL_NAME"));

                        myTeachSkills.add(skill);
                    }
                }
            }

            request.setAttribute("partners", partners);
            request.setAttribute("myTeachSkills", myTeachSkills);

            request.getRequestDispatcher("/partners.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Unable to load partners.", e);
        }
    }
}