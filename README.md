# SkillBridge – Skill Exchange Platform

SkillBridge is a web-based platform that connects people who want to learn new skills with people who are willing to teach them.

The platform encourages collaborative learning by allowing users to share their expertise, discover learning partners, and exchange skills.

## Features

- **User Registration & Login** – Secure account creation and authentication using BCrypt password hashing.
- **Forgot Password** – Reset passwords through email verification.
- **Skill Management** – Add and manage skills you want to teach or learn.
- **Find Learning Partners** – Discover users whose teaching skills match your learning interests.
- **Skill Exchange Requests** – Send, accept, and reject exchange requests.
- **Email Notifications** – Receive email updates about exchange requests.
- **Login Protection** – Temporary account lockout after repeated failed login attempts.
- **CSRF Protection** – Protect authenticated form submissions against cross-site request forgery.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Backend development |
| JSP | Dynamic web pages |
| Jakarta Servlets | Request handling |
| HTML5 | Page structure |
| CSS3 | Styling and responsive design |
| JavaScript | Client-side functionality |
| Oracle Database | Data storage |
| JDBC | Database connectivity |
| Apache Tomcat 10.1 | Application server |
| Maven | Build and dependency management |
| BCrypt | Password hashing |
| Gmail SMTP | Email notifications |

## Project Structure

```text
SkillBridge/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── skillbridge/
│       │           ├── DBConnection.java
│       │           ├── EmailService.java
│       │           └── [Servlet Classes]
│       │
│       └── webapp/
│           ├── css/
│           │   └── style.css
│           ├── index.html
│           ├── login.html
│           ├── register.html
│           ├── dashboard.jsp
│           ├── skills.jsp
│           ├── partners.jsp
│           └── requests.jsp
│
├── pom.xml
└── README.md
```

## Prerequisites

To run SkillBridge locally, install:

- Java JDK
- Apache Maven
- Apache Tomcat 10.1
- Oracle Database access
- Git

## Configuration

SkillBridge uses environment variables to protect sensitive credentials.

Configure the following environment variables before running the application:

| Environment Variable | Description |
|---|---|
| `SKILLBRIDGE_DB_USER` | Oracle database username |
| `SKILLBRIDGE_DB_PASSWORD` | Oracle database password |
| `SKILLBRIDGE_GMAIL_USER` | Gmail account used for notifications |
| `SKILLBRIDGE_GMAIL_APP_PASSWORD` | Gmail App Password |

**Security:** Never commit database credentials, Gmail passwords, or app passwords to GitHub.

## Build the Project

Clone the repository:

```bash
git clone https:/DhruvBihani3478/github.com//SkillBridge.git
```

Navigate into the project:

```bash
cd SkillBridge
```

Build the application:

```bash
mvn clean package
```

This generates:

```text
target/skillbridge.war
```

## Run the Application

1. Configure the required environment variables.
2. Build the project using Maven.
3. Deploy `target/skillbridge.war` to Apache Tomcat 10.1.
4. Start the Tomcat server.
5. Open the application:

```text
http://localhost:8080/skillbridge/
```

## Security Features

- BCrypt password hashing.
- Login attempt protection and temporary lockout.
- CSRF token validation.
- Database constraints and foreign keys.
- Environment-based configuration for sensitive credentials.

## Future Enhancements

- User profile management.
- Real-time messaging between learning partners.
- Skill ratings and reviews.
- Advanced partner recommendations.
- Improved deployment and scalability.

## Author

**SkillBridge – Skill Exchange Platform**

A collaborative platform built to make learning and teaching skills easier.

---

*Learn something. Teach something. Grow together.*
