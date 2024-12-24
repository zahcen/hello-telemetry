package com.bbsod.demo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// OpenTelemetry API Imports
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

// OpenTelemetry SDK Imports
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;

// OpenTelemetry Exporter & Propgator Imports
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;

public class MyServlet extends HttpServlet {

    // Define Class Fields
    private static final String INSTRUMENTATION_NAME = MyServlet.class.getName();
    private final Meter meter;
    private final LongCounter requestCounter;
    private final Tracer tracer;

    // Default Constructor
    // Initializes OpenTelemetry and calls parameterized constructor with this new
    // instance of OpenTelemetry
    public MyServlet() {
        this(initOpenTelemetry());
    }

    // Parameterized Constructor
    // Accepts OpenTelemetry instance and initializes the meter, request counter and
    // tracer.
    public MyServlet(OpenTelemetry openTelemetry) {
        this.meter = openTelemetry.getMeter(INSTRUMENTATION_NAME);
        this.requestCounter = meter.counterBuilder("app.db.db_requests")
                .setDescription("Counts DB requests")
                .build();
        this.tracer = openTelemetry.getTracer(INSTRUMENTATION_NAME);
    }

    // Initializes OpenTelemetry
    static OpenTelemetry initOpenTelemetry() {
        // Metrics
        OtlpGrpcMetricExporter otlpGrpcMetricExporter = OtlpGrpcMetricExporter.builder()
                .setEndpoint("http://localhost:4317").build();

        PeriodicMetricReader periodicMetricReader = PeriodicMetricReader.builder(otlpGrpcMetricExporter)
                .setInterval(java.time.Duration.ofSeconds(5))
                .build();

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(periodicMetricReader)
                .build();

        // Traces
        OtlpGrpcSpanExporter otlpGrpcSpanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317").build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(otlpGrpcSpanExporter))
                .build();

        // Traces as Logs
        SdkTracerProvider sdkTracerProviderLogs = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                .build();

        // SDK
        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setMeterProvider(sdkMeterProvider)
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                // .setTracerProvider(sdkTracerProviderLogs) // NOTE: This line has to be
                // commented out when using live. The second `.setTracerProvder(..)` will
                // override previous one
                .build();

        // Cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sdkMeterProvider.close();
            sdkTracerProvider.close();
        }));

        return sdk;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");

        // Increment metric counter
        requestCounter.add(1);

        // Start a span
        Span span = tracer.spanBuilder("initiate database query").startSpan();

        // Establish database connection and get data
        try (PrintWriter out = response.getWriter()) {
            // JDBC connection parameters
            //String jdbcUrl = "jdbc:mysql://localhost:3306/mydatabase";
            String jdbcUrl = "jdbc:mysql://mysql_container:3306/mydatabase";
            String jdbcUser = "myuser";
            String jdbcPassword = "mypassword";

            
            
            try {
                //Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish connection
                Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);

                // Create a statement
                Statement statement = connection.createStatement();

                // Execute a query
                String query = "SELECT * FROM mytable";
                ResultSet resultSet = statement.executeQuery(query);

                out.println("<html><body>");
                out.println("<h1>Database Results</h1>");
                out.println("<table border='1'>");
                out.println("<tr><th>ID</th><th>Name</th></tr>");

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    out.println("<tr><td>" + id + "</td><td>" + name + "</td></tr>");
                }

                out.println("</table>");
                out.println("</body></html>");

            } catch (ClassNotFoundException e) {
                System.out.println("MySQL JDBC Driver not found.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("Connection failed.");
                e.printStackTrace();
            } finally {
                span.end(); // Close the span once request complete
            }
        }
    }
}