# Use the official Tomcat image from the Docker Hub
FROM tomcat:11.0

# Copy your web application WAR file to the Tomcat webapps directory
COPY /home/runner/work/hello-telemetry/hello-telemetry/tomcat/MyWebApp.war /usr/local/tomcat/webapps/

# Start Tomcat
CMD ["catalina.sh", "run"]