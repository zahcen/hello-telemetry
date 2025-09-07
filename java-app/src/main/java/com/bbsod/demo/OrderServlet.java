package com.bbsod.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        Span span = tracer.spanBuilder("PlaceOrder").startSpan();
        String orderId = "",customerId = "",amount = "";
        boolean payment_status=true;

        try {
            // Add useful attributes to the span
            double orderAmount = 10 + Math.random() * 500;
            span.setAttribute("order_amount", orderAmount);
            if (orderAmount<100)
                span.setAttribute("payment_method", "invoice");
            else
                span.setAttribute("payment_method", "credit_card");

        // Get Node.js URL from environment variable, default to http://localhost:3000/order
        String nodeJsUrl = System.getenv("NODEJS_ORDER_URL");
        if (nodeJsUrl == null || nodeJsUrl.isEmpty()) {
            nodeJsUrl = "http://localhost:3000/payment";
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
        System.out.println("json="+json);

        orderId = json.replaceAll(".*\"order_id\":(\\d+).*", "$1");
        System.out.println("orderId="+orderId);

        customerId = json.replaceAll(".*\"customer_id\":(\\d+).*", "$1");
        System.out.println("customerId="+customerId); 

        //amount = Double.toString(orderAmount);
        amount = String.format("%.2f", orderAmount);
        //amount = json.replaceAll(".*\"amount\":(\\d+).*", "$1");
        System.out.println("amount="+amount);

        // Record metrics
        metrics.incrementOrderCount(customerId, orderAmount);

        span.setAttribute("order.id", orderId);

        // Simulate a payment error for orders above 400 EUR
        if (orderAmount > 350) {
            payment_status=false;
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
            jsonResponse.put("payment_status", payment_status);

            // Send JSON to client
            PrintWriter out = response.getWriter();
            out.print(jsonResponse.toString());
            out.flush();
        }
    }


    public  void placeOrder(double orderAmount) {
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
