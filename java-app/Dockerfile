# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# List files to debug
RUN echo "Listing:" && ls -R .

WORKDIR /app

# List files to debug.
RUN echo "Listing:" && ls -R .

COPY java-app/pom.xml .
COPY java-app/src ./src
RUN mvn clean package -DskipTests

# Use the official Tomcat image from the Docker Hub
#FROM tomcat:11.0

# Copy your web application WAR file to the Tomcat webapps directory
#COPY MyWebApp.war /usr/local/tomcat/webapps/

# Start Tomcat
#CMD ["catalina.sh", "run"]