
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

    List<Map<String, Object>> requests =
        (List<Map<String, Object>>) request.getAttribute("requests");

    if (requests == null) {
        requests = java.util.Collections.emptyList();
    }

    String contextPath = request.getContextPath();
    String success = request.getParameter("success");
    String error = request.getParameter("error");

    int incomingCount = 0;
    int outgoingCount = 0;
    int pendingCount = 0;

    for (Map<String, Object> item : requests) {
        boolean incoming = Boolean.TRUE.equals(item.get("incoming"));
        String itemStatus = String.valueOf(item.get("status"));

        if (incoming) {
            incomingCount++;
        } else {
            outgoingCount++;
        }

        if ("PENDING".equals(itemStatus)) {
            pendingCount++;
        }
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Exchange Requests | SkillBridge</title>

    <link rel="stylesheet"
          href="<%= contextPath %>/css/style.css?v=5">

    <style>
        .requests-page {
            max-width: 1120px;
            margin: 0 auto;
            padding: 38px 24px 55px;
        }

        .requests-hero {
            background: #183153;
            color: #ffffff;
            padding: 38px 42px;
            border-radius: 14px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 25px;
            margin-bottom: 28px;
        }

        .requests-hero-content {
            max-width: 680px;
        }

        .requests-eyebrow {
            display: inline-block;
            color: #c5d5e8;
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 1.5px;
            margin-bottom: 12px;
        }

        .requests-hero h1 {
            color: #ffffff;
            font-size: 32px;
            margin: 0 0 12px;
        }

        .requests-hero p {
            color: #d5dfeb;
            font-size: 15px;
            line-height: 1.7;
            margin: 0;
        }

        .requests-hero-label {
            font-size: 13px;
            font-weight: 700;
            letter-spacing: 2px;
            color: #d5dfeb;
            border: 1px solid rgba(255,255,255,0.3);
            padding: 17px 20px;
            border-radius: 8px;
            white-space: nowrap;
        }

        .request-overview {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 17px;
            margin-bottom: 28px;
        }

        .request-stat {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 10px;
            padding: 21px 23px;
        }

        .request-stat span {
            display: block;
            color: #64748b;
            font-size: 13px;
            margin-bottom: 9px;
        }

        .request-stat strong {
            color: #183153;
            font-size: 28px;
        }

        .request-section-header {
            margin-bottom: 20px;
        }

        .request-section-header h2 {
            color: #183153;
            font-size: 21px;
            margin-bottom: 7px;
        }

        .request-section-header p {
            color: #64748b;
            font-size: 14px;
            line-height: 1.6;
        }

        .request-card {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 11px;
            padding: 24px;
            margin-bottom: 16px;
            transition: border-color 0.2s ease;
        }

        .request-card:hover {
            border-color: #b5c7dc;
        }

        .request-card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;
            margin-bottom: 19px;
        }

        .request-card h3 {
            color: #183153;
            font-size: 17px;
        }

        .request-person {
            color: #334155;
            font-size: 14px;
            margin-bottom: 17px;
            overflow-wrap: anywhere;
        }

        .request-details {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 13px;
            margin-bottom: 16px;
        }

        .request-detail {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            padding: 14px;
            border-radius: 7px;
        }

        .request-detail span {
            display: block;
            color: #64748b;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 0.5px;
            margin-bottom: 7px;
        }

        .request-detail strong {
            color: #183153;
            font-size: 14px;
            overflow-wrap: anywhere;
        }

        .request-status {
            display: inline-block;
            padding: 6px 11px;
            border-radius: 5px;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 0.5px;
        }

        .pending {
            background: #fff7e0;
            color: #946200;
        }

        .accepted {
            background: #eaf7f0;
            color: #176344;
        }

        .rejected {
            background: #fff0f0;
            color: #b42318;
        }

        .request-date {
            color: #94a3b8;
            font-size: 12px;
            margin-top: 13px;
        }

        .request-actions {
            display: flex;
            gap: 10px;
            margin-top: 18px;
        }

        .request-action-button {
            min-width: 100px;
            padding: 10px 17px;
            border: none;
            border-radius: 6px;
            color: #ffffff;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
        }

        .accept-button {
            background: #16845b;
        }

        .accept-button:hover {
            background: #116c49;
        }

        .reject-button {
            background: #c24141;
        }

        .reject-button:hover {
            background: #a83232;
        }

        .request-message {
            padding: 14px 17px;
            border-radius: 7px;
            margin-bottom: 17px;
            font-size: 14px;
            line-height: 1.5;
        }

        .success-message {
            background: #eaf7f0;
            color: #176344;
            border: 1px solid #c6ead5;
        }

        .error-message {
            background: #fff0f0;
            color: #991b1b;
            border: 1px solid #f2cccc;
        }

        .empty-requests {
            background: #ffffff;
            border: 1px solid #e2e8f0;
            border-radius: 11px;
            text-align: center;
            padding: 45px 25px;
        }

        .empty-requests h2 {
            color: #183153;
            font-size: 21px;
            margin-bottom: 12px;
        }

        .empty-requests p {
            color: #64748b;
            font-size: 14px;
            line-height: 1.7;
            max-width: 530px;
            margin: 0 auto 22px;
        }

        .requests-link-button {
            display: inline-block;
            padding: 12px 19px;
            background: #183153;
            color: #ffffff;
            border-radius: 7px;
            text-decoration: none;
            font-size: 13px;
            font-weight: 600;
        }

        .requests-link-button:hover {
            background: #285587;
        }

        .requests-footer {
            text-align: center;
            padding: 22px;
            color: #64748b;
            font-size: 13px;
            border-top: 1px solid #e2e8f0;
        }

        @media (max-width: 750px) {
            .requests-page {
                padding: 25px 16px 40px;
            }

            .requests-hero {
                padding: 28px 23px;
            }

            .requests-hero h1 {
                font-size: 26px;
            }

            .requests-hero-label {
                font-size: 11px;
                padding: 13px;
            }

            .request-details {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 480px) {
            .requests-hero-label {
                display: none;
            }

            .request-overview {
                grid-template-columns: 1fr;
                gap: 12px;
            }

            .request-card {
                padding: 19px;
            }

            .request-actions {
                flex-direction: column;
            }

            .request-action-button {
                width: 100%;
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

    <main class="requests-page">

        <section class="requests-hero">

            <div class="requests-hero-content">

                <span class="requests-eyebrow">
                    YOUR SKILL EXCHANGE ACTIVITY
                </span>

                <h1>Exchange Requests</h1>

                <p>
                    Manage your incoming and outgoing requests.
                    Connect with other learners and exchange knowledge.
                </p>

            </div>

            <div class="requests-hero-label">
                REQUEST CENTER
            </div>

        </section>

        <section class="request-overview">

            <div class="request-stat">
                <span>Total Requests</span>
                <strong><%= requests.size() %></strong>
            </div>

            <div class="request-stat">
                <span>Incoming Requests</span>
                <strong><%= incomingCount %></strong>
            </div>

            <div class="request-stat">
                <span>Pending Requests</span>
                <strong><%= pendingCount %></strong>
            </div>

        </section>

        <% if ("sent".equals(success)) { %>
            <div class="request-message success-message">
                Your exchange request was sent successfully!
            </div>
        <% } else if ("accepted".equals(success)) { %>
            <div class="request-message success-message">
                You accepted the exchange request.
            </div>
        <% } else if ("rejected".equals(success)) { %>
            <div class="request-message success-message">
                You rejected the exchange request.
            </div>
        <% } %>

        <% if ("duplicate".equals(error)) { %>
            <div class="request-message error-message">
                You already have this pending request.
            </div>
        <% } else if ("invalid".equals(error)) { %>
            <div class="request-message error-message">
                Invalid request details. Please try again.
            </div>
        <% } else if ("notfound".equals(error)) { %>
            <div class="request-message error-message">
                This request is no longer pending or cannot be updated.
            </div>
        <% } %>

        <section>

            <div class="request-section-header">

                <h2>All Requests</h2>

                <p>
                    Review the details and current status of your exchanges.
                </p>

            </div>

            <% if (requests.isEmpty()) { %>

                <div class="empty-requests">

                    <h2>No Exchange Requests Yet</h2>

                    <p>
                        Find a learning partner and send your first
                        exchange request to get started.
                    </p>

                    <a class="requests-link-button"
                       href="<%= contextPath %>/partners">
                        Find Partners
                    </a>

                </div>

            <% } else { %>

                <% for (Map<String, Object> item : requests) {

                    boolean incoming = Boolean.TRUE.equals(item.get("incoming"));

                    String status = String.valueOf(item.get("status"));

                    String otherName = incoming
                        ? String.valueOf(item.get("senderName"))
                        : String.valueOf(item.get("receiverName"));

                    String statusClass = status.toLowerCase();
                %>

                    <article class="request-card">

                        <div class="request-card-header">

                            <h3>
                                <%= incoming ? "Incoming Request" : "Outgoing Request" %>
                            </h3>

                            <span class="request-status <%= statusClass %>">
                                <%= escapeHtml(status) %>
                            </span>

                        </div>

                        <p class="request-person">
                            <strong>
                                <%= incoming ? "From:" : "To:" %>
                            </strong>
                            <%= escapeHtml(otherName) %>
                        </p>

                        <div class="request-details">

                            <div class="request-detail">

                                <span>SKILL OFFERED</span>

                                <strong>
                                    <%= escapeHtml(String.valueOf(item.get("offeredSkill"))) %>
                                </strong>

                            </div>

                            <div class="request-detail">

                                <span>SKILL REQUESTED</span>

                                <strong>
                                    <%= escapeHtml(String.valueOf(item.get("requestedSkill"))) %>
                                </strong>

                            </div>

                        </div>

                        <p class="request-date">
                            Created: <%= item.get("createdAt") %>
                        </p>

                        <% if (incoming && "PENDING".equals(status)) { %>

                            <div class="request-actions">

                                <form action="<%= contextPath %>/requests"
                                      method="post">

                                    <input type="hidden"
                                           name="action"
                                           value="accept">

                                    <input type="hidden"
                                           name="csrfToken"
                                           value="<%= escapeHtml((String) session.getAttribute("csrfToken")) %>">

                                    <input type="hidden"
                                           name="requestId"
                                           value="<%= item.get("requestId") %>">

                                    <button class="request-action-button accept-button"
                                            type="submit">
                                        Accept
                                    </button>

                                </form>

                                <form action="<%= contextPath %>/requests"
                                      method="post">

                                    <input type="hidden"
                                           name="action"
                                           value="reject">

                                    <input type="hidden"
                                           name="csrfToken"
                                           value="<%= escapeHtml((String) session.getAttribute("csrfToken")) %>">

                                    <input type="hidden"
                                           name="requestId"
                                           value="<%= item.get("requestId") %>">

                                    <button class="request-action-button reject-button"
                                            type="submit">
                                        Reject
                                    </button>

                                </form>

                            </div>

                        <% } %>

                    </article>

                <% } %>

            <% } %>

        </section>

    </main>

    <footer class="requests-footer">

        <p>
            &copy; 2026 SkillBridge.
            Learn Together. Grow Together.
        </p>

    </footer>

</body>
</html>
