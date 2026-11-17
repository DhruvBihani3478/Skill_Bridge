
package com.skillbridge;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (fullName == null || email == null || password == null
                || fullName.isBlank() || email.isBlank()
                || password.isBlank()) {

            response.sendRedirect("register.html?error=empty");
            return;
        }

        String sql = "INSERT INTO USERS "
                   + "(FULL_NAME, EMAIL, PASSWORD_HASH) "
                   + "VALUES (?, ?, ?)";

        String passwordHash = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fullName.trim());
            statement.setString(2, email.trim().toLowerCase());
            statement.setString(3, passwordHash);

            statement.executeUpdate();

            response.sendRedirect("login.html?registered=true");

        } catch (SQLException e) {

            if ("23000".equals(e.getSQLState())
                    || (e.getMessage() != null
                    && e.getMessage().contains("unique constraint"))) {

                response.sendRedirect("register.html?error=exists");

            } else {
                throw new ServletException(
                        "Unable to register user.", e
                );
            }
        }
    }
}
