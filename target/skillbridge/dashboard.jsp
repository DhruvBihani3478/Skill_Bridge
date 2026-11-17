
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%!
    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
%>

<%
    String fullName = (String) request.getAttribute("fullName");

    if (fullName == null) {
        fullName = "Member";
    }

    Number teachValue = (Number) request.getAttribute("teachCount");
    Number learnValue = (Number) request.getAttribute("learnCount");
    Number partnerValue = (Number) request.getAttribute("partnerCount");
    Number pendingValue = (Number) request.getAttribute("pendingRequests");

    int teachCount = teachValue != null ? teachValue.intValue() : 0;
    int learnCount = learnValue != null ? learnValue.intValue() : 0;
    int partnerCount = partnerValue != null ? partnerValue.intValue() : 0;
    int pendingRequests = pendingValue != null ? pendingValue.intValue() : 0;

    String contextPath = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Dashboard | SkillBridge</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css?v=5">

    <style>

        /* =================================
           DASHBOARD DESIGN
        ================================= */

        .dashboard-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 40px 6% 75px;
        }

        /* Welcome Section */

        .dashboard-welcome {
            background: #183153;
            color: #ffffff;
            padding: 42px 45px;
            border-radius: 12px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            position: relative;
            overflow: hidden;
            margin-bottom: 35px;
        }

        .dashboard-welcome::after {
            content: "";
            position: absolute;
            width: 260px;
            height: 260px;
            border: 1px solid rgba(255,255,255,0.12);
            border-radius: 50%;
            right: -80px;
            top: -130px;
        }

        .welcome-content {
            position: relative;
            z-index: 1;
        }

        .welcome-label {
            display: inline-block;
            color: #b8cce4;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 1.7px;
            margin-bottom: 15px;
        }

        .dashboard-welcome h1 {
            color: #ffffff;
            font-size: clamp(27px, 4vw, 38px);
            letter-spacing: -1px;
            margin-bottom: 12px;
        }

        .dashboard-welcome p {
            color: #d0dbea;
            font-size: 14px;
            line-height: 1.8;
        }

        .welcome-icon {
            position: relative;
            z-index: 1;
            width: 85px;
            height: 85px;
            border-radius: 12px;
            background: rgba(255,255,255,0.11);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 39px;
            flex-shrink: 0;
        }

        /* Section headings */

        .dashboard-heading {
            margin-bottom: 20px;
        }

        .dashboard-heading h2 {
            color: #183153;
            font-size: 21px;
            letter-spacing: -0.4px;
            margin-bottom: 7px;
        }

        .dashboard-heading p {
            color: #64748b;
            font-size: 13px;
            line-height: 1.7;
        }

        /* Statistics */

        .dashboard-stats {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 18px;
            margin-bottom: 45px;
        }

        .stat-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            padding: 23px;
            transition: transform 0.2s, border-color 0.2s;
        }

        .stat-card:hover {
            transform: translateY(-3px);
            border-color: #bdccdd;
        }

        .stat-card-top {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 22px;
        }

        .stat-icon {
            width: 43px;
            height: 43px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 9px;
            font-size: 20px;
        }

        .stat-icon.navy {
            background: #eaf0f7;
        }

        .stat-icon.blue {
            background: #edf4ff;
        }

        .stat-icon.green {
            background: #eaf7f0;
        }

        .stat-icon.orange {
            background: #fff4e8;
        }

        .stat-number {
            color: #183153;
            font-size: 34px;
            font-weight: 750;
            line-height: 1;
            margin-bottom: 9px;
        }

        .stat-card h3 {
            color: #334155;
            font-size: 13px;
            font-weight: 650;
            margin-bottom: 8px;
        }

        .stat-card > span {
            color: #94a3b8;
            font-size: 11px;
            line-height: 1.6;
        }

        /* Main sections */

        .dashboard-main-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 20px;
        }

        .dashboard-action-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            padding: 27px;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            min-height: 205px;
            transition: border-color 0.2s, transform 0.2s;
        }

        .dashboard-action-card:hover {
            border-color: #bdccdd;
            transform: translateY(-2px);
        }

        .action-card-icon {
            width: 43px;
            height: 43px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #eaf0f7;
            border-radius: 9px;
            font-size: 20px;
            margin-bottom: 19px;
        }

        .dashboard-action-card h3 {
            color: #183153;
            font-size: 17px;
            margin-bottom: 9px;
        }

        .dashboard-action-card p {
            color: #64748b;
            font-size: 13px;
            line-height: 1.8;
            margin-bottom: 21px;
            flex-grow: 1;
        }

        .dashboard-action-card .button {
            display: inline-block;
            text-decoration: none;
            font-size: 12px;
            padding: 11px 17px;
        }

        /* Quick actions */

        .dashboard-quick-actions {
            margin-bottom: 45px;
        }

        .dashboard-quick-actions-inner {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
        }

        .quick-action-link {
            display: inline-flex;
            align-items: center;
            gap: 9px;
            padding: 12px 17px;
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 7px;
            color: #334155;
            text-decoration: none;
            font-size: 12px;
            font-weight: 650;
            transition: background 0.2s, border-color 0.2s;
        }

        .quick-action-link:hover {
            background: #eaf0f7;
            border-color: #bdccdd;
        }

        /* Footer */

        .dashboard-footer {
            background: #ffffff;
            border-top: 1px solid #e2e8f0;
            padding: 24px 6%;
            text-align: center;
        }

        .dashboard-footer p {
            color: #8491a2;
            font-size: 12px;
        }

        /* Responsive */

        @media (max-width: 1000px) {

            .dashboard-stats {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }

        }

        @media (max-width: 700px) {

            .dashboard-container {
                padding: 30px 5% 55px;
            }

            .dashboard-main-grid {
                grid-template-columns: 1fr;
            }

            .dashboard-welcome {
                padding: 32px;
            }

            .welcome-icon {
                width: 65px;
                height: 65px;
                font-size: 30px;
            }

        }

        @media (max-width: 480px) {

            .dashboard-welcome {
                padding: 27px 23px;
            }

            .dashboard-welcome h1 {
                font-size: 27px;
            }

            .welcome-icon {
                display: none;
            }

            .dashboard-stats {
                gap: 12px;
            }

            .stat-card {
                padding: 17px;
            }

            .stat-number {
                font-size: 29px;
            }

            .stat-card h3 {
                font-size: 12px;
            }

            .dashboard-action-card {
                padding: 22px;
            }

        }

    </style>

</head>

<body class="dashboard-body">

    <!-- NAVIGATION -->

    <header class="navbar">

        <a href="<%= contextPath %>/dashboard" class="logo">
            SkillBridge
        </a>

        <nav>

            <a href="<%= contextPath %>/dashboard">
                Dashboard
            </a>

            <a href="<%= contextPath %>/skills">
                My Skills
            </a>

            <a href="<%= contextPath %>/partners">
                Find Partners
            </a>

            <a href="<%= contextPath %>/requests">
                Requests
            </a>

            <a href="<%= contextPath %>/logout" class="nav-button">
                Logout
            </a>

        </nav>

    </header>


    <!-- MAIN DASHBOARD -->

    <main class="dashboard-container">


        <!-- WELCOME -->

        <section class="dashboard-welcome">

            <div class="welcome-content">

                <span class="welcome-label">
                    YOUR LEARNING SPACE
                </span>

                <h1>
                    Welcome back, <%= escapeHtml(fullName) %>.
                </h1>

                <p>
                    Keep sharing your knowledge and discovering
                    new opportunities to learn.
                </p>

            </div>

            <div class="welcome-icon">
                🎓
            </div>

        </section>


        <!-- STATISTICS -->

        <section>

            <div class="dashboard-heading">

                <h2>Your Overview</h2>

                <p>
                    A snapshot of your SkillBridge activity.
                </p>

            </div>


            <div class="dashboard-stats">


                <!-- TEACH -->

                <article class="stat-card">

                    <div class="stat-card-top">

                        <h3>Skills I Teach</h3>

                        <div class="stat-icon navy">
                            🎓
                        </div>

                    </div>

                    <p class="stat-number">
                        <%= teachCount %>
                    </p>

                    <span>
                        Skills you can share with others
                    </span>

                </article>


                <!-- LEARN -->

                <article class="stat-card">

                    <div class="stat-card-top">

                        <h3>Skills to Learn</h3>

                        <div class="stat-icon blue">
                            📚
                        </div>

                    </div>

                    <p class="stat-number">
                        <%= learnCount %>
                    </p>

                    <span>
                        Your learning interests
                    </span>

                </article>


                <!-- PARTNERS -->

                <article class="stat-card">

                    <div class="stat-card-top">

                        <h3>Learning Partners</h3>

                        <div class="stat-icon green">
                            🤝
                        </div>

                    </div>

                    <p class="stat-number">
                        <%= partnerCount %>
                    </p>

                    <span>
                        Members matching your interests
                    </span>

                </article>


                <!-- REQUESTS -->

                <article class="stat-card">

                    <div class="stat-card-top">

                        <h3>Pending Requests</h3>

                        <div class="stat-icon orange">
                            ✉️
                        </div>

                    </div>

                    <p class="stat-number">
                        <%= pendingRequests %>
                    </p>

                    <span>
                        Incoming requests awaiting response
                    </span>

                </article>

            </div>

        </section>


        <!-- QUICK ACTIONS -->

        <section class="dashboard-quick-actions">

            <div class="dashboard-heading">

                <h2>Quick Access</h2>

                <p>
                    Jump directly to the sections you use most.
                </p>

            </div>

            <div class="dashboard-quick-actions-inner">

                <a href="<%= contextPath %>/skills"
                   class="quick-action-link">

                    <span>＋</span>
                    Manage My Skills

                </a>

                <a href="<%= contextPath %>/partners"
                   class="quick-action-link">

                    <span>⌕</span>
                    Find Partners

                </a>

                <a href="<%= contextPath %>/requests"
                   class="quick-action-link">

                    <span>✉</span>
                    View Requests

                </a>

            </div>

        </section>


        <!-- DASHBOARD SECTIONS -->

        <section>

            <div class="dashboard-heading">

                <h2>Explore SkillBridge</h2>

                <p>
                    Everything you need to exchange knowledge
                    and build connections.
                </p>

            </div>


            <div class="dashboard-main-grid">


                <!-- MY SKILLS -->

                <article class="dashboard-action-card">

                    <div class="action-card-icon">
                        📚
                    </div>

                    <h3>My Skills</h3>

                    <p>
                        Manage the skills you can teach and
                        the skills you want to learn.
                    </p>

                    <a href="<%= contextPath %>/skills"
                       class="button primary-button">

                        Manage My Skills

                    </a>

                </article>


                <!-- FIND PARTNERS -->

                <article class="dashboard-action-card">

                    <div class="action-card-icon">
                        🤝
                    </div>

                    <h3>Find Learning Partners</h3>

                    <p>
                        Discover members who can teach the skills
                        you want to learn and explore possible matches.
                    </p>

                    <a href="<%= contextPath %>/partners"
                       class="button primary-button">

                        Explore Partners

                    </a>

                </article>


                <!-- REQUESTS -->

                <article class="dashboard-action-card">

                    <div class="action-card-icon">
                        ✉️
                    </div>

                    <h3>Exchange Requests</h3>

                    <p>
                        Review incoming and outgoing requests
                        and manage your skill exchange connections.
                    </p>

                    <a href="<%= contextPath %>/requests"
                       class="button primary-button">

                        View Requests

                    </a>

                </article>


                <!-- ACCOUNT -->

                <article class="dashboard-action-card">

                    <div class="action-card-icon">
                        🌐
                    </div>

                    <h3>Grow Through Learning</h3>

                    <p>
                        Keep your skills updated, discover new
                        matches and make the most of your community.
                    </p>

                    <a href="<%= contextPath %>/skills"
                       class="button primary-button">

                        Explore My Skills

                    </a>

                </article>


            </div>

        </section>

    </main>


    <!-- FOOTER -->

    <footer class="dashboard-footer">

        <p>
            &copy; 2026 SkillBridge. Learn Together. Grow Together.
        </p>

    </footer>

</body>

</html>
