
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Forgot Password | SkillBridge</title>

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
                Back to Login
            </a>
        </nav>

    </header>

    <main class="form-page">

        <section class="form-card">

            <h1>Forgot Password?</h1>

            <p class="form-description">
                Enter your registered email address.
                We'll send you a password reset link.
            </p>

            <p id="message"
               role="status"
               aria-live="polite"></p>

            <form action="<%= request.getContextPath() %>/forgot-password"
                  method="post">

                <div class="form-group">

                    <label for="email">
                        Email Address
                    </label>

                    <input
                        type="email"
                        id="email"
                        name="email"
                        placeholder="Enter your registered email"
                        maxlength="254"
                        autocomplete="email"
                        required>

                </div>

                <button type="submit"
                        class="button primary-button form-submit">
                    Send Reset Link
                </button>

            </form>

            <p class="form-footer">
                Remember your password?

                <a href="<%= request.getContextPath() %>/login.html">
                    Login here
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

        if (params.get("sent") === "true") {

            message.textContent =
                "If an account exists with that email, "
                + "a password reset link has been sent.";

            message.className = "success-message";

        }
        else if (params.get("error") === "empty") {

            message.textContent =
                "Please enter your email address.";

            message.className = "error-message";

        }

    </script>

</body>
</html>
