# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run the application on Tomcat
FROM tomcat:10.1-jdk21-temurin

# Deploy SkillBridge as the root application
COPY --from=build /app/target/skillbridge.war /usr/local/tomcat/webapps/ROOT.war

# Use the port assigned by Render
CMD ["sh", "-c", "sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT:-8080}\\\"/\" /usr/local/tomcat/conf/server.xml && catalina.sh run"]