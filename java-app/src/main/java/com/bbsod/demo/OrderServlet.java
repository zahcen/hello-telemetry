package com.bbsod.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.extension.annotations.SpanAttribute;
import io.opentelemetry.extension.annotations.WithSpan;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OrderServlet extends HttpServlet {

    String payment_method="";
    boolean payment_status = false;
    private static final Logger logger = LogManager.getLogger(OrderServlet.class);

    @WithSpan()
    protected void getDBdata(){
        // JDBC connection parameters
        String jdbcUrl = "jdbc:mysql://ht-mysql:3306/mydatabase?useSSL=false&allowPublicKeyRetrieval=true";
        jdbcUrl = "jdbc:mysql://ht-mysql:3306/mydatabase";
        String jdbcUser = "myuser";
        String jdbcPassword = "mypassword";
        try {
            logger.info("Load MySQL JDBC Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");

            logger.info("Establish connection using:"+jdbcUrl);
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
            logger.info("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            logger.info("Connection failed.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @WithSpan("Validate Shipping Address")
    private void validate_shipping_address(){

        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @WithSpan()
    private void validate_billing_address(){
        logger.info("Start");
        // Sleep for 2 seconds
        try {
            Thread.sleep(400);
            Random random = new Random();
            if (random.nextInt(10)<3)
                throw new Exception("Invalid billing address");
        } catch (Exception e) {
            logger.error("Exception in validate_billing_address", e);
        }
        logger.info("End");
    }

    @WithSpan()
    private void validate_payment(String orderId, String customerId, double orderAmount) throws MalformedURLException, ProtocolException, IOException{

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
        logger.info("jsonBody="+jsonBody);
        //jsonBody.put("payment_method", payment_method);

        logger.info("nodeJsUrl="+nodeJsUrl);
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
        logger.info("content="+content);

        // Parse JSON manually (simple approach)
        String json = content.toString();
        logger.info("json="+json);

        // Parse the JSON
        JSONObject jsonObject = new JSONObject(json);

        payment_status = "1".equals(jsonObject.getString("payment_status"));
        if (! payment_status)
            throw new RuntimeException("Payment failed: Card declined");

    }

    @WithSpan()
    protected void placeOrder(@SpanAttribute("order_id") String orderId, String customerId, @SpanAttribute("order_amount") double order_amount) throws ProtocolException, IOException{
        
        logger.info("Start");

        validate_shipping_address();
        
        if (order_amount<100){
            payment_method="Invoice";
        }
        else{
            payment_method="Credit card";
        }

        Span currentSpan = Span.current();
        currentSpan.setAttribute("payment_method", payment_method);

        getDBdata();

        validate_billing_address();
        try {
            validate_payment(orderId, customerId, order_amount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("End");

    }

    @WithSpan()
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
        
        Random random = new Random();
        // Generate random orderId between 1000 and 2000 (inclusive)
        int orderId_int = 1000 + random.nextInt(2000 - 1000 + 1);
        String orderId = Integer.toString(orderId_int);

        // Generate random customerId between 10000 and 20000 (inclusive)
        int customerId_int = 10000 + random.nextInt(20000 - 10000 + 1);
        String customerId = Integer.toString(customerId_int);

        logger.info("Generated Order ID: " + orderId);
        logger.info("Generated Customer ID: " + customerId);

        getDBdata();
        double orderAmount = 10 + Math.random() * 500;
        String amount = String.format("%.2f", orderAmount);

        placeOrder(orderId, customerId, orderAmount);

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
