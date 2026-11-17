
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
    List<Map<String, Object>> skills =
        (List<Map<String, Object>>) request.getAttribute("skills");

    String contextPath = request.getContextPath();
    String success = request.getParameter("success");
    String error = request.getParameter("error");

    int teachCount = 0;
    int learnCount = 0;

    if (skills != null) {
        for (Map<String, Object> skill : skills) {
            String type = (String) skill.get("type");

            if ("TEACH".equals(type)) {
                teachCount++;
            } else if ("LEARN".equals(type)) {
                learnCount++;
            }
        }
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>My Skills | SkillBridge</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css?v=5">

    <style>
        .skills-page {
            max-width: 1120px;
            margin: 0 auto;
            padding: 38px 24px 55px;
        }

        .skills-hero {
            background: #183153;
            color: #ffffff;
            padding: 38px 42px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 25px;
            margin-bottom: 28px;
        }

        .skills-hero-content {
            max-width: 650px;
        }

        .skills-eyebrow {
            display: inline-block;
            color: #c5d5e8;
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 1.5px;
            margin-bottom: 12px;
        }

        .skills-hero h1 {
            font-size: 34px;
            margin: 0 0 12px;
            color: #ffffff;
        }

        .skills-hero p {
            color: #d5dfeb;
            font-size: 15px;
            line-height: 1.7;
            margin: 0;
        }

        .skills-hero-icon {
            font-size: 15px;
            font-weight: 700;
            letter-spacing: 3px;
            color: #d5dfeb;
            border: 1px solid rgba(255,255,255,0.3);
            padding: 18px 24px;
            border-radius: 8px;
        }

        .skills-overview {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 18px;
            margin-bottom: 30px;
        }

        .overview-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            padding: 21px 23px;
        }

        .overview-card span {
            display: block;
            color: #64748b;
            font-size: 13px;
            margin-bottom: 9px;
        }

        .overview-card strong {
            display: block;
            color: #183153;
            font-size: 29px;
            font-weight: 700;
        }

        .skills-panel {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 30px;
            margin-bottom: 25px;
        }

        .skills-panel-header {
            margin-bottom: 23px;
        }

        .skills-panel-header h2 {
            color: #183153;
            font-size: 21px;
            margin: 0 0 8px;
        }

        .skills-panel-header p {
            color: #64748b;
            font-size: 14px;
            line-height: 1.6;
            margin: 0;
        }

        .skill-form {
            display: grid;
            grid-template-columns: 1.4fr 1fr auto;
            align-items: end;
            gap: 18px;
        }

        .skill-form .form-group {
            margin: 0;
        }

        .skill-form label {
            display: block;
            color: #334155;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 9px;
        }

        .skill-form input,
        .skill-form select {
            width: 100%;
            min-height: 45px;
            padding: 11px 13px;
            border: 1px solid #cbd5e1;
            border-radius: 7px;
            background: #ffffff;
            color: #172638;
            font-size: 14px;
            box-sizing: border-box;
        }

        .skill-form input:focus,
        .skill-form select:focus {
            outline: none;
            border-color: #3567a5;
            box-shadow: 0 0 0 3px rgba(53, 103, 165, 0.12);
        }

        .add-skill-button {
            min-height: 45px;
            padding: 0 20px;
            border: none;
            border-radius: 7px;
            background: #183153;
            color: #ffffff;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            white-space: nowrap;
        }

        .add-skill-button:hover {
            background: #285587;
        }

        .skill-message {
            padding: 14px 17px;
            border-radius: 7px;
            margin-bottom: 18px;
            font-size: 14px;
            line-height: 1.5;
        }

        .success-message {
            color: #166534;
            background: #eaf7f0;
            border: 1px solid #c6ead5;
        }

        .error-message {
            color: #991b1b;
            background: #fff0f0;
            border: 1px solid #f2cccc;
        }

        .saved-skills-list {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 15px;
        }

        .saved-skill-card {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 15px;
            padding: 19px;
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 9px;
            transition: border-color 0.2s ease;
        }

        .saved-skill-card:hover {
            border-color: #b5c7dc;
        }

        .saved-skill-info h3 {
            color: #183153;
            font-size: 16px;
            margin: 0 0 10px;
            overflow-wrap: anywhere;
        }

        .skill-badge {
            display: inline-block;
            padding: 5px 9px;
            border-radius: 5px;
            font-size: 10px;
            font-weight: 700;
            letter-spacing: 0.5px;
        }

        .teach-badge {
            color: #176344;
            background: #eaf7f0;
        }

        .learn-badge {
            color: #285587;
            background: #eaf0f7;
        }

        .delete-skill-button {
            padding: 8px 13px;
            color: #b42318;
            background: #ffffff;
            border: 1px solid #e7b8b5;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            cursor: pointer;
        }

        .delete-skill-button:hover {
            background: #fff0f0;
            border-color: #c24141;
        }

        .empty-skills {
            text-align: center;
            padding: 45px 20px;
            border: 1px dashed #cbd5e1;
            border-radius: 9px;
            background: #f8fafc;
        }

        .empty-skills-icon {
            font-size: 38px;
            margin-bottom: 12px;
        }

        .empty-skills h3 {
            color: #183153;
            font-size: 18px;
            margin: 0 0 8px;
        }

        .empty-skills p {
            color: #64748b;
            font-size: 14px;
            margin: 0;
        }

        .skills-footer {
            text-align: center;
            padding: 22px;
            color: #64748b;
            font-size: 13px;
            border-top: 1px solid #e2e8f0;
        }

        @media (max-width: 850px) {
            .skill-form {
                grid-template-columns: 1fr 1fr;
            }

            .add-skill-button {
                grid-column: 1 / -1;
            }
        }

        @media (max-width: 650px) {
            .skills-page {
                padding: 25px 16px 40px;
            }

            .skills-hero {
                padding: 27px 23px;
            }

            .skills-hero h1 {
                font-size: 27px;
            }

            .skills-hero-icon {
                font-size: 42px;
            }

            .skills-overview {
                grid-template-columns: 1fr;
                gap: 12px;
            }

            .skills-panel {
                padding: 22px 17px;
            }

            .skill-form {
                grid-template-columns: 1fr;
            }

            .add-skill-button {
                grid-column: auto;
            }

            .saved-skills-list {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 400px) {
            .skills-hero-icon {
                display: none;
            }

            .saved-skill-card {
                align-items: flex-start;
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
            <a href="<%= contextPath %>/dashboard#find-partners">
                Find Partners
            </a>
            <a href="<%= contextPath %>/dashboard#requests">
                Requests
            </a>
            <a href="<%= contextPath %>/logout" class="nav-button">
                Logout
            </a>
        </nav>

    </header>

    <main class="skills-page">

        <section class="skills-hero">

            <div class="skills-hero-content">

                <span class="skills-eyebrow">YOUR LEARNING PROFILE</span>

                <h1>My Skills</h1>

                <p>
                    Share what you know and discover what you can learn.
                    Build meaningful connections through skill exchange.
                </p>

            </div>

            <div class="skills-hero-icon">
                SKILLS
            </div>

        </section>

        <section class="skills-overview">

            <div class="overview-card">
                <span>Total Skills</span>
                <strong><%= skills == null ? 0 : skills.size() %></strong>
            </div>

            <div class="overview-card">
                <span>Skills I Can Teach</span>
                <strong><%= teachCount %></strong>
            </div>

            <div class="overview-card">
                <span>Skills I Want to Learn</span>
                <strong><%= learnCount %></strong>
            </div>

        </section>

        <% if ("added".equals(success)) { %>
            <div class="skill-message success-message">
                Skill added successfully!
            </div>
        <% } %>

        <% if ("deleted".equals(success)) { %>
            <div class="skill-message success-message">
                Skill deleted successfully!
            </div>
        <% } %>

        <% if ("duplicate".equals(error)) { %>
            <div class="skill-message error-message">
                You have already added this skill under that category.
            </div>
        <% } %>

        <% if ("invalid".equals(error)) { %>
            <div class="skill-message error-message">
                Please enter a valid skill name and category.
            </div>
        <% } %>

        <% if ("notfound".equals(error)) { %>
            <div class="skill-message error-message">
                The selected skill could not be found.
            </div>
        <% } %>

        <% if ("database".equals(error)) { %>
            <div class="skill-message error-message">
                Database error while saving your skill.
                Please check the server logs.
            </div>
        <% } %>

        <section class="skills-panel">

            <div class="skills-panel-header">

                <h2>Add a New Skill</h2>

                <p>
                    Add a skill you can share with others or one
                    you would like to develop.
                </p>

            </div>

            <form action="<%= contextPath %>/skills"
                  method="post"
                  class="skill-form">

                <input type="hidden"
                       name="csrfToken"
                       value="<%= escapeHtml((String) session.getAttribute("csrfToken")) %>">

                <div class="form-group">

                    <label for="skillName">Skill Name</label>

                    <input type="text"
                           id="skillName"
                           name="skillName"
                           placeholder="Example: Java, Python, HTML"
                           maxlength="100"
                           required>

                </div>

                <div class="form-group">

                    <label for="skillType">Skill Category</label>

                    <select id="skillType"
                            name="skillType"
                            required>

                        <option value="">Select a category</option>

                        <option value="TEACH">
                            I can teach this skill
                        </option>

                        <option value="LEARN">
                            I want to learn this skill
                        </option>

                    </select>

                </div>

                <button type="submit" class="add-skill-button">
                    + Add Skill
                </button>

            </form>

        </section>

        <section class="skills-panel">

            <div class="skills-panel-header">

                <h2>My Saved Skills</h2>

                <p>
                    Manage the skills currently connected to your account.
                </p>

            </div>

            <% if (skills == null || skills.isEmpty()) { %>

                <div class="empty-skills">

                <div class="empty-skills-icon">+</div>          

                    <h3>No skills added yet</h3>

                    <p>
                        Add your first skill using the form above.
                    </p>

                </div>

            <% } else { %>

                <div class="saved-skills-list">

                    <% for (Map<String, Object> skill : skills) {

                        long id = ((Number) skill.get("id")).longValue();

                        String name = (String) skill.get("name");

                        String type = (String) skill.get("type");

                    %>

                        <div class="saved-skill-card">

                            <div class="saved-skill-info">

                                <h3><%= escapeHtml(name) %></h3>

                                <% if ("TEACH".equals(type)) { %>

                                    <span class="skill-badge teach-badge">
                                        I CAN TEACH
                                    </span>

                                <% } else { %>

                                    <span class="skill-badge learn-badge">
                                        I WANT TO LEARN
                                    </span>

                                <% } %>

                            </div>

                            <form action="<%= contextPath %>/skills"
                                  method="post"
                                  onsubmit="return confirm('Are you sure you want to delete this skill?');">

                                <input type="hidden"
                                       name="action"
                                       value="delete">

                                <input type="hidden"
                                       name="csrfToken"
                                       value="<%= escapeHtml((String) session.getAttribute("csrfToken")) %>">

                                <input type="hidden"
                                       name="userSkillId"
                                       value="<%= id %>">

                                <button type="submit"
                                        class="delete-skill-button">
                                    Delete
                                </button>

                            </form>

                        </div>

                    <% } %>

                </div>

            <% } %>

        </section>

    </main>

    <footer class="skills-footer">

        <p>
            &copy; 2026 SkillBridge.
            Learn Together. Grow Together.
        </p>

    </footer>

</body>
</html>
