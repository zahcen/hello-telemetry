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

public class MyServlet extends HttpServlet {

    // Constructor
    public MyServlet() {
        // Print initial HTML
        // printInitialHtml(out);

        // Sleep for 2 seconds
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Method to print initial HTML
    private void printInitialHtml(PrintWriter out) {
        out.println("<html><body>");
        out.println("<h1>Database Results</h1>");
        out.println("<div id='data'>Getting data...</div>");
        out.println("<script>");
        out.println("function updateData(html) { document.getElementById('data').innerHTML = html; }");
        out.println("</script>");
        out.println("</body></html>");
        out.flush();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        // PrintWriter out = response.getWriter();

        // Establish database connection and get data
        try (PrintWriter out = response.getWriter()) {
            // JDBC connection parameters
            String jdbcUrl = "jdbc:mysql://mysql_container:3306/mydatabase";
            String jdbcUser = "myuser";
            String jdbcPassword = "mypassword";

            try {
                // Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establish connection
                Connection connection = DriverManager.getConnection(jdbcUrl, jdbcUser,
                        jdbcPassword);

                // Create a statement
                Statement statement = connection.createStatement();

                // Execute a query
                String query = "SELECT * FROM mytable";
                ResultSet resultSet = statement.executeQuery(query);

                out.println("<html><body>");
                out.println("<h1>Database Results</h1>");
                out.println("<table border='1'>");
                out.println("<tr><th>ID</th><th>Name</th><th>Age</th></tr>");

                List<JSONObject> dataList = new ArrayList<>();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int age = resultSet.getInt("age");
                    out.println("<tr><td>" + id + "</td><td>" + name + "</td><td>" + age
                            + "</td></tr>");

                    JSONObject dataObject = new JSONObject();
                    dataObject.put("id", id);
                    dataObject.put("name", name);
                    dataObject.put("age", age);
                    dataList.add(dataObject);

                }
                out.println("</table>");

                // Make a request to the Python microservice
                String averageAge = getAverageAge(dataList);
                out.println("<h2>Average Age: " + averageAge + "</h2>");
                out.println("</body></html>");

            } catch (ClassNotFoundException e) {
                System.out.println("MySQL JDBC Driver not found.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("Connection failed.");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                out.println("<h2>Error: " + e.getMessage() + "</h2>");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private String getAverageAge(List<JSONObject> dataList) throws IOException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("http://python-service:5000/compute_average_age");
            httpPost.setHeader("Content-Type", "application/json");

            JSONObject requestData = new JSONObject();
            requestData.put("data", new JSONArray(dataList));

            StringEntity entity = new StringEntity(requestData.toString());
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject responseJson = new JSONObject(responseString);
                return responseJson.get("average_age").toString();
            }
        }
    }
}