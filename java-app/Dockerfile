# Use the official Tomcat image from the Docker Hub
FROM tomcat:11.0

# Copy your web application WAR file to the Tomcat webapps directory
COPY java-app/MyWebApp.war /usr/local/tomcat/webapps/
COPY java-app/otel-sdk/elastic-otel-javaagent-1.5.0.jar /opt/elastic-otel-javaagent.jar
COPY java-app/otel-sdk/elastic-otel-javaagent-1.5.0.jar /usr/local/tomcat/lib/elastic-otel-javaagent-1.5.0.jar
COPY java-app/otel-sdk/elastic-otel-javaagent-1.4.2-20250627.150129-72.jar /usr/local/tomcat/lib/elastic-otel-javaagent-1.4.2-20250627.150129-72.jar

ADD --chmod=644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /opt/opentelemetry-javaagent.jar
ADD --chmod=644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /usr/local/tomcat/lib/opentelemetry-javaagent.jar

# Start Tomcat
CMD ["catalina.sh", "run"]