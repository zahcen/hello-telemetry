# Use the official Tomcat image from the Docker Hub
FROM tomcat:11.0

# Copy your web application WAR file to the Tomcat webapps directory
COPY java-app/MyWebApp.war /usr/local/tomcat/webapps/
COPY java-app/otel-sdk/elastic-otel-javaagent-1.5.0.jar /usr/local/tomcat/lib/elastic-otel-javaagent-1.5.0.jar

# Start Tomcat
CMD ["catalina.sh", "run"]