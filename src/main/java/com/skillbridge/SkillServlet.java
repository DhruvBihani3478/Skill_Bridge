
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

@WebServlet("/skills")
public class SkillServlet extends HttpServlet {

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

        List<Map<String, Object>> skills = new ArrayList<>();

        String sql =
            "SELECT us.USER_SKILL_ID, s.SKILL_NAME, us.SKILL_TYPE " +
            "FROM USER_SKILLS us " +
            "JOIN SKILLS s ON us.SKILL_ID = s.SKILL_ID " +
            "WHERE us.USER_ID = ? " +
            "ORDER BY us.SKILL_TYPE, s.SKILL_NAME";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> skill = new HashMap<>();

                    skill.put("id", rs.getInt("USER_SKILL_ID"));
                    skill.put("name", rs.getString("SKILL_NAME"));
                    skill.put("type", rs.getString("SKILL_TYPE"));

                    skills.add(skill);
                }
            }

            request.setAttribute("skills", skills);
            request.getRequestDispatcher("/skills.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Unable to load skills.", e);
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

        if ("delete".equals(action)) {
            deleteSkill(request, response, userId);
        } else {
            addSkill(request, response, userId);
        }
    }

    private void addSkill(HttpServletRequest request,
                          HttpServletResponse response,
                          int userId)
            throws IOException {

        String skillName = request.getParameter("skillName");
        String skillType = request.getParameter("skillType");

        if (skillName == null || skillName.trim().isEmpty()
                || skillName.trim().length() > 100
                || (!"TEACH".equals(skillType)
                && !"LEARN".equals(skillType))) {

            response.sendRedirect(request.getContextPath()
                    + "/skills?error=invalid");
            return;
        }

        skillName = skillName.trim();

        String mergeSql =
            "MERGE INTO SKILLS s " +
            "USING (SELECT ? AS SKILL_NAME FROM DUAL) src " +
            "ON (UPPER(s.SKILL_NAME) = UPPER(src.SKILL_NAME)) " +
            "WHEN NOT MATCHED THEN " +
            "INSERT (SKILL_NAME) VALUES (src.SKILL_NAME)";

        String findSql =
            "SELECT SKILL_ID FROM SKILLS WHERE UPPER(SKILL_NAME) = UPPER(?)";

        String insertSql =
            "INSERT INTO USER_SKILLS (USER_ID, SKILL_ID, SKILL_TYPE) " +
            "VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                try (PreparedStatement merge = con.prepareStatement(mergeSql)) {
                    merge.setString(1, skillName);
                    merge.executeUpdate();
                }

                int skillId;

                try (PreparedStatement find = con.prepareStatement(findSql)) {
                    find.setString(1, skillName);

                    try (ResultSet rs = find.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Skill could not be found.");
                        }

                        skillId = rs.getInt("SKILL_ID");
                    }
                }

                try (PreparedStatement insert = con.prepareStatement(insertSql)) {
                    insert.setInt(1, userId);
                    insert.setInt(2, skillId);
                    insert.setString(3, skillType);
                    insert.executeUpdate();
                }

                con.commit();

                response.sendRedirect(request.getContextPath()
                        + "/skills?success=added");

            } catch (SQLException e) {

                con.rollback();

                if (e.getErrorCode() == 1) {
                    response.sendRedirect(request.getContextPath()
                            + "/skills?error=duplicate");
                } else {
                    throw e;
                }

            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new IOException("Unable to add skill: " + e.getMessage(), e);
        }
    }

    private void deleteSkill(HttpServletRequest request,
                             HttpServletResponse response,
                             int userId)
            throws IOException {

        String idValue = request.getParameter("userSkillId");

        if (idValue == null || !idValue.matches("\\d+")) {
            response.sendRedirect(request.getContextPath()
                    + "/skills?error=invalid");
            return;
        }

        String sql =
            "DELETE FROM USER_SKILLS " +
            "WHERE USER_SKILL_ID = ? AND USER_ID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idValue));
            ps.setInt(2, userId);

            int deleted = ps.executeUpdate();

            if (deleted > 0) {
                response.sendRedirect(request.getContextPath()
                        + "/skills?success=deleted");
            } else {
                response.sendRedirect(request.getContextPath()
                        + "/skills?error=notfound");
            }

        } catch (SQLException e) {
            throw new IOException("Unable to delete skill: " + e.getMessage(), e);
        }
    }
}
