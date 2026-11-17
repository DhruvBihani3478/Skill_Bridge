
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String token = (String) request.getAttribute("token");

    if (token == null) {
        token = request.getParameter("token");
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Reset Password | SkillBridge</title>

    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/css/style.css">
</head>

<body>

    <header class="navbar">

        <a href="<%= request.getContextPath() %>/index.html"
           class="logo">
            SkillBridge
        </a>

        <nav>
            <a href="<%= request.getContextPath() %>/login.html">
                Login
            </a>
        </nav>

    </header>

    <main class="form-page">

        <section class="form-card">

            <h1>Reset Password</h1>

            <p class="form-description">
                Enter your new password below.
            </p>

            <p id="message"
               class="error-message"
               role="alert"
               aria-live="polite"></p>

            <form action="<%= request.getContextPath() %>/reset-password"
                  method="post">

                <input
                    type="hidden"
                    name="token"
                    value="<%= token == null ? "" :
                        token.replace("&", "&amp;")
                             .replace("\"", "&quot;")
                             .replace("<", "&lt;")
                             .replace(">", "&gt;") %>">

                <div class="form-group">

                    <label for="password">
                        New Password
                    </label>

                    <input
                        type="password"
                        id="password"
                        name="password"
                        placeholder="Enter new password"
                        minlength="8"
                        maxlength="72"
                        autocomplete="new-password"
                        required>

                </div>

                <div class="form-group">

                    <label for="confirmPassword">
                        Confirm New Password
                    </label>

                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        placeholder="Confirm new password"
                        minlength="8"
                        maxlength="72"
                        autocomplete="new-password"
                        required>

                </div>

                <button type="submit"
                        class="button primary-button form-submit">
                    Update Password
                </button>

            </form>

            <p class="form-footer">
                <a href="<%= request.getContextPath() %>/login.html">
                    Back to Login
                </a>
            </p>

        </section>

    </main>

    <footer>
        <p>
            &copy; 2026 SkillBridge. Learn Together. Grow Together.
        </p>
    </footer>

    <script>

        const params = new URLSearchParams(
            window.location.search
        );

        const message = document.getElementById("message");

        if (params.get("error") === "password") {

            message.textContent =
                "Password must contain between 8 and 72 characters.";

        }
        else if (params.get("error") === "mismatch") {

            message.textContent =
                "Passwords do not match. Please try again.";

        }

    </script>

</body>
</html>
