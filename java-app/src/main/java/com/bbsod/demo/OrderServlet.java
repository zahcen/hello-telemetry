package com.bbsod.demo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Scope;

public class OrderServlet extends HttpServlet {

    private final OrderMetrics metrics = new OrderMetrics();

    private static final Tracer tracer =
        GlobalOpenTelemetry.getTracer("Order");

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        Span span = tracer.spanBuilder("create_order").startSpan();
        span.setAttribute("component", "PaymentService");
        span.setAttribute("status", "OK");

        // Get Node.js URL from environment variable, default to http://localhost:3000/order
        String nodeJsUrl = System.getenv("NODEJS_ORDER_URL");
        if (nodeJsUrl == null || nodeJsUrl.isEmpty()) {
            nodeJsUrl = "http://localhost:3000/order";
        }
        System.out.println("nodeJsUrl="+nodeJsUrl);
        // Call internal Node.js service
        URL url = new URL(nodeJsUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Read Node.js response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        System.out.println("content="+content);

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();

        // Parse JSON manually (simple approach)
        String json = content.toString();
        String orderId = json.replaceAll(".*\"order_id\":(\\d+).*", "$1");
        System.out.println("orderId="+orderId);

        String customerId = json.replaceAll(".*\"customer_id\":(\\d+).*", "$1");
        System.out.println("customerId="+customerId); 

        String amount = json.replaceAll(".*\"amount\":(\\d).*", "$1");
        System.out.println("amount="+amount);

        // Record metrics
        metrics.incrementOrderCount(customerId, Double.parseDouble(amount));

        span.setAttribute("order.id", orderId);
        span.end();
        
        // Redirect back to JSP with parameters
        response.sendRedirect("/MyWebApp/index.jsp?order_id=" + orderId + "&amount=" + amount + "&customer_id=" + customerId);
    }
}
