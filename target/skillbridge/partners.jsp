
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.util.Map" %>

<%!
    private String escapeHtml(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>

<%
    if (session == null || session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    List<Map<String, Object>> partners =
        (List<Map<String, Object>>) request.getAttribute("partners");

    List<Map<String, Object>> myTeachSkills =
        (List<Map<String, Object>>) request.getAttribute("myTeachSkills");

    if (partners == null) partners = java.util.Collections.emptyList();
    if (myTeachSkills == null) myTeachSkills = java.util.Collections.emptyList();

    String contextPath = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Find Partners | SkillBridge</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css?v=5">

    <style>
        .partners-page {
            max-width: 1120px;
            margin: 0 auto;
            padding: 38px 24px 55px;
        }

        .partners-hero {
            background: #183153;
            color: #ffffff;
            padding: 38px 42px;
            border-radius: 14px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 25px;
            margin-bottom: 30px;
        }

        .partners-hero-content {
            max-width: 680px;
        }

        .partners-eyebrow {
            display: inline-block;
            color: #c5d5e8;
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 1.5px;
            margin-bottom: 12px;
        }

        .partners-hero h1 {
            color: #ffffff;
            font-size: 32px;
            margin: 0 0 12px;
        }

        .partners-hero p {
            color: #d5dfeb;
            font-size: 15px;
            line-height: 1.7;
            margin: 0;
        }

        .partners-hero-label {
            font-size: 13px;
            font-weight: 700;
            letter-spacing: 2px;
            color: #d5dfeb;
            border: 1px solid rgba(255,255,255,0.3);
            padding: 17px 20px;
            border-radius: 8px;
            white-space: nowrap;
        }

        .partners-section-header {
            margin-bottom: 22px;
        }

        .partners-section-header h2 {
            color: #183153;
            font-size: 21px;
            margin-bottom: 8px;
        }

        .partners-section-header p {
            color: #64748b;
            font-size: 14px;
            line-height: 1.6;
        }

        .partner-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 20px;
        }

        .partner-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 11px;
            padding: 25px;
            transition: border-color 0.2s ease;
        }

        .partner-card:hover {
            border-color: #b5c7dc;
        }

        .partner-card h2 {
            color: #183153;
            font-size: 19px;
            margin-bottom: 8px;
            overflow-wrap: anywhere;
        }

        .partner-email {
            color: #64748b;
            font-size: 13px;
            margin-bottom: 20px;
            overflow-wrap: anywhere;
        }

        .partner-skill-label {
            display: inline-block;
            color: #176344;
            background: #eaf7f0;
            border-radius: 5px;
            padding: 8px 11px;
            font-size: 12px;
            font-weight: 600;
            margin-bottom: 22px;
            line-height: 1.5;
        }

        .partner-card form label {
            display: block;
            color: #334155;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 9px;
        }

        .partner-card select {
            width: 100%;
            min-height: 44px;
            padding: 10px 12px;
            background: #ffffff;
            border: 1px solid #cbd5e1;
            border-radius: 7px;
            color: #172638;
            font-size: 14px;
            margin-bottom: 15px;
        }

        .partner-card select:focus {
            outline: none;
            border-color: #3567a5;
            box-shadow: 0 0 0 3px rgba(53,103,165,0.12);
        }

        .partner-button {
            width: 100%;
            min-height: 44px;
            padding: 11px 15px;
            border: none;
            border-radius: 7px;
            background: #183153;
            color: #ffffff;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.2s ease;
        }

        .partner-button:hover {
            background: #285587;
        }

        .empty-partners {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 11px;
            padding: 45px 25px;
            text-align: center;
        }

        .empty-partners h2 {
            color: #183153;
            font-size: 21px;
            margin-bottom: 12px;
        }

        .empty-partners p {
            color: #64748b;
            font-size: 14px;
            line-height: 1.7;
            max-width: 530px;
            margin: 0 auto 22px;
        }

        .partners-notice {
            padding: 17px 19px;
            background: #fff7ed;
            color: #9a3412;
            border: 1px solid #fed7aa;
            border-radius: 8px;
            font-size: 14px;
            line-height: 1.6;
            margin-bottom: 18px;
        }

        .partners-action {
            display: inline-block;
            padding: 12px 19px;
            background: #183153;
            color: #ffffff;
            border-radius: 7px;
            text-decoration: none;
            font-size: 13px;
            font-weight: 600;
        }

        .partners-action:hover {
            background: #285587;
        }

        .partners-footer {
            text-align: center;
            padding: 22px;
            color: #64748b;
            font-size: 13px;
            border-top: 1px solid #e2e8f0;
        }

        @media (max-width: 750px) {
            .partners-page {
                padding: 25px 16px 40px;
            }

            .partners-hero {
                padding: 28px 23px;
            }

            .partners-hero h1 {
                font-size: 26px;
            }

            .partners-hero-label {
                font-size: 11px;
                padding: 13px;
            }

            .partner-grid {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 480px) {
            .partners-hero-label {
                display: none;
            }
        }
    </style>
</head>

<body class="dashboard-body">

    <header class="navbar">

        <a href="<%= contextPath %>/dashboard" class="logo">
            SkillBridge
        </a>

        <nav>
            <a href="<%= contextPath %>/dashboard">Dashboard</a>
            <a href="<%= contextPath %>/skills">My Skills</a>
            <a href="<%= contextPath %>/partners">Find Partners</a>
            <a href="<%= contextPath %>/requests">Requests</a>
            <a href="<%= contextPath %>/logout" class="nav-button">
                Logout
            </a>
        </nav>

    </header>

    <main class="partners-page">

        <section class="partners-hero">

            <div class="partners-hero-content">

                <span class="partners-eyebrow">
                    BUILD YOUR LEARNING NETWORK
                </span>

                <h1>Find Learning Partners</h1>

                <p>
                    Connect with people who can teach the skills
                    you want to learn. Share your knowledge in
                    return and grow together.
                </p>

            </div>

            <div class="partners-hero-label">
                SKILL EXCHANGE
            </div>

        </section>

        <section>

            <div class="partners-section-header">

                <h2>Matching Partners</h2>

                <p>
                    Explore members who teach skills you are interested in.
                </p>

            </div>

            <% if (partners.isEmpty()) { %>

                <div class="empty-partners">

                    <h2>No Matching Partners Found</h2>

                    <p>
                        Add skills under the LEARN category in My Skills.
                        Members teaching those skills will appear here.
                    </p>

                    <a class="partners-action"
                       href="<%= contextPath %>/skills">
                        Manage My Skills
                    </a>

                </div>

            <% } else if (myTeachSkills.isEmpty()) { %>

                <div class="partners-notice">
                    You found matching partners, but you haven't added
                    any skills under TEACH yet. Add a teaching skill
                    before sending an exchange request.
                </div>

                <a class="partners-action"
                   href="<%= contextPath %>/skills">
                    Add a Teaching Skill
                </a>

            <% } else { %>

                <div class="partner-grid">

                    <% for (Map<String, Object> partner : partners) {

                        String name = String.valueOf(partner.get("fullName"));
                        String email = String.valueOf(partner.get("email"));
                        String skillName = String.valueOf(partner.get("skillName"));

                        int receiverId = ((Number) partner.get("userId")).intValue();
                        int requestedSkillId = ((Number) partner.get("skillId")).intValue();
                    %>

                        <article class="partner-card">

                            <h2><%= escapeHtml(name) %></h2>

                            <p class="partner-email">
                                <%= escapeHtml(email) %>
                            </p>

                            <span class="partner-skill-label">
                                Can teach: <%= escapeHtml(skillName) %>
                            </span>

                            <form action="<%= contextPath %>/requests"
                                  method="post">

                                <input type="hidden"
                                       name="action"
                                       value="send">

                                <input type="hidden"
                                       name="csrfToken"
                                       value="<%= escapeHtml((String) session.getAttribute("csrfToken")) %>">

                                <input type="hidden"
                                       name="receiverId"
                                       value="<%= receiverId %>">

                                <input type="hidden"
                                       name="requestedSkillId"
                                       value="<%= requestedSkillId %>">

                                <label>Select a skill you can teach:</label>

                                <select name="offeredSkillId" required>

                                    <option value="">
                                        Choose your skill
                                    </option>

                                    <% for (Map<String, Object> skill : myTeachSkills) { %>

                                        <option value="<%= skill.get("id") %>">
                                            <%= escapeHtml(String.valueOf(skill.get("name"))) %>
                                        </option>

                                    <% } %>

                                </select>

                                <button type="submit" class="partner-button">
                                    Send Exchange Request
                                </button>

                            </form>

                        </article>

                    <% } %>

                </div>

            <% } %>

        </section>

    </main>

    <footer class="partners-footer">

        <p>
            &copy; 2026 SkillBridge.
            Learn Together. Grow Together.
        </p>

    </footer>

</body>
</html>
