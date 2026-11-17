
package com.skillbridge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = {"/partners", "/requests", "/skills"})
public class CsrfFilter implements Filter {

    private static final String TOKEN_ATTRIBUTE = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(jakarta.servlet.ServletRequest servletRequest,
                         jakarta.servlet.ServletResponse servletResponse,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request =
                (HttpServletRequest) servletRequest;

        HttpServletResponse response =
                (HttpServletResponse) servletResponse;

        HttpSession session = request.getSession(false);

        // Only authenticated users can access these protected actions.
        if (session == null || session.getAttribute("userId") == null) {
            chain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();

        // Generate a token when an authenticated user opens a protected page.
        if ("GET".equalsIgnoreCase(method)) {

            synchronized (session) {
                if (session.getAttribute(TOKEN_ATTRIBUTE) == null) {

                    byte[] randomBytes = new byte[32];
                    RANDOM.nextBytes(randomBytes);

                    String token = Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(randomBytes);

                    session.setAttribute(TOKEN_ATTRIBUTE, token);
                }
            }

            request.setAttribute(
                    TOKEN_ATTRIBUTE,
                    session.getAttribute(TOKEN_ATTRIBUTE)
            );

            chain.doFilter(request, response);
            return;
        }

        // Validate every POST request to /requests and /skills.
        if ("POST".equalsIgnoreCase(method)) {

            String sessionToken =
                    (String) session.getAttribute(TOKEN_ATTRIBUTE);

            String submittedToken =
                    request.getParameter(TOKEN_ATTRIBUTE);

            if (sessionToken == null || submittedToken == null
                    || !MessageDigest.isEqual(
                            sessionToken.getBytes(StandardCharsets.UTF_8),
                            submittedToken.getBytes(StandardCharsets.UTF_8))) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Invalid or missing CSRF token."
                );

                return;
            }
        }

        chain.doFilter(request, response);
    }
}
