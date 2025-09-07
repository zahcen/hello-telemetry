package com.bbsod.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.json.JSONObject;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class OrderServlet extends HttpServlet {

    private final OrderMetrics metrics = new OrderMetrics();

    private static final Tracer tracer =
        GlobalOpenTelemetry.getTracer("Order");

    protected void getDBdata(){
        // JDBC connection parameters
        String jdbcUrl = "jdbc:mysql://ht-mysql:3306/mydatabase?useSSL=false&allowPublicKeyRetrieval=true";
        jdbcUrl = "jdbc:mysql://ht-mysql:3306/mydatabase";
        String jdbcUser = "myuser";
        String jdbcPassword = "mypassword";
        try {
            System.out.println("Load MySQL JDBC Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");

            System.out.println("Establish connection using:"+jdbcUrl);
            Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser,
                    jdbcPassword);

            // Create a statement
            Statement statement = connection.createStatement();

            // Execute a query
            String query = "SELECT * FROM mytable";
            ResultSet resultSet = statement.executeQuery(query);


            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void validate_shipping_address(boolean addMetrics){
        Span span = null;
        if (addMetrics)
            span = tracer.spanBuilder("Validate Shipping Address").startSpan();

        // Sleep for 2 seconds
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        if (addMetrics)
            span.end();        
    }


    private void validate_billing_address(boolean addMetrics){
        Span span = null;
        if (addMetrics)
            span = tracer.spanBuilder("Validate Billing Address").startSpan();

        // Sleep for 2 seconds
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        if (addMetrics)
            span.end();        
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        
        String addMetricsParam  = request.getParameter("add_metrics"); // "true" or "false"
        boolean addMetrics = Boolean.parseBoolean(addMetricsParam);
        System.out.println("addMetrics="+addMetrics);
        Span span = null;
        if (addMetrics)
            span = tracer.spanBuilder("PlaceOrder").startSpan();
        
        String payment_method="";
        boolean payment_status=true;
        
        Random random = new Random();

        // Generate random orderId between 1000 and 2000 (inclusive)
        int orderId_int = 1000 + random.nextInt(2000 - 1000 + 1);
        String orderId = Integer.toString(orderId_int);

        // Generate random customerId between 10000 and 20000 (inclusive)
        int customerId_int = 10000 + random.nextInt(20000 - 10000 + 1);
        String customerId = Integer.toString(customerId_int);

        System.out.println("Generated Order ID: " + orderId);
        System.out.println("Generated Customer ID: " + customerId);

        getDBdata();

        validate_shipping_address(addMetrics);

        // Add useful attributes to the span
        double orderAmount = 10 + Math.random() * 500;
        String amount = String.format("%.2f", orderAmount);

        try {
            if (addMetrics)
                span.setAttribute("order_amount", orderAmount);
            if (orderAmount<100){
                if (addMetrics)
                    span.setAttribute("payment_method", "Invoice");
                payment_method="Invoice";
            }
            else{
                if (addMetrics)
                    span.setAttribute("payment_method", "Credit card");
                payment_method="Credit card";
            }
        // Get Node.js URL from environment variable, default to http://localhost:3000/order
        String nodeJsUrl = System.getenv("NODEJS_ORDER_URL");
        if (nodeJsUrl == null || nodeJsUrl.isEmpty()) {
            nodeJsUrl = "http://localhost:3000/payment";
        }
        
        // Prepare JSON body
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("orderId", orderId);
        jsonBody.put("customerId", customerId);
        jsonBody.put("amount",  Double.toString(orderAmount));
        System.out.println("jsonBody="+jsonBody);
        //jsonBody.put("payment_method", payment_method);

        System.out.println("nodeJsUrl="+nodeJsUrl);
        // Call internal Node.js service
        URL url = new URL(nodeJsUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        // Send JSON data
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.toString().getBytes("UTF-8"));
            os.flush();
        }

        // Read Node.js response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();
        System.out.println("content="+content);

        validate_billing_address(addMetrics);

        // Parse JSON manually (simple approach)
        String json = content.toString();
        System.out.println("json="+json);

        // Parse the JSON
        JSONObject jsonObject = new JSONObject(json);

        // Extract values
        orderId = jsonObject.getString("order_id");
        customerId = jsonObject.getString("customer_id");
        payment_status = jsonObject.getString("payment_status");

        // Print the results
        System.out.println("Order ID: " + orderId);
        System.out.println("Customer ID: " + customerId);
        System.out.println("Payment Status: " + payment_status);

        // Record metrics
        //metrics.incrementOrderCount(customerId, orderAmount);

        if (addMetrics)
            span.setAttribute("order.id", orderId);

        if (! payment_status) {
            throw new RuntimeException("Payment failed: Card declined");
        }

        //if (orderAmount > 400) {
        //    payment_status=false;
        //    throw new RuntimeException("Payment failed: Card declined");
        //}

        // Simulate successful order placement
        System.out.println("Order placed successfully: " + orderAmount);

        } catch (Exception e) {
            // Attach exception details to the span
            if (addMetrics){
                span.recordException(e);
                span.setStatus(StatusCode.ERROR, "Payment failed");
            }

            System.err.println("Order failed: " + e.getMessage());

        } finally {
            // Always end the span
            if (addMetrics)
                span.end();
            // Redirect back to JSP with parameters
            //response.sendRedirect("/MyWebApp/index.jsp?order_id=" + orderId + "&amount=" + amount + "&customer_id=" + customerId);
            
            // Set response type to JSON
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Build JSON response
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("order_id", orderId);
            jsonResponse.put("customer_id", customerId);
            jsonResponse.put("amount", amount);
            jsonResponse.put("payment_method", payment_method);
            jsonResponse.put("payment_status", payment_status);


            // Send JSON to client
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
            out.flush();
        }
    }


    private  void placeOrder(double orderAmount) {
        // Start a span for the order process
        Span span = tracer.spanBuilder("PlaceOrder").startSpan();

        try {
            // Add useful attributes to the span
            span.setAttribute("order_amount", orderAmount);
            span.setAttribute("payment_method", "credit_card");

            // Simulate a payment error for orders above 400 EUR
            if (orderAmount > 300) {
                throw new RuntimeException("Payment failed: Card declined");
            }

            // Simulate successful order placement
            System.out.println("Order placed successfully: " + orderAmount);

        } catch (Exception e) {
            // Attach exception details to the span
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Payment failed");

            System.err.println("Order failed: " + e.getMessage());

        } finally {
            // Always end the span
            span.end();
        }
    }

}
