<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Web App</title>
    <style>
        table {
            border-collapse: collapse;
            width: 80%;
            margin-top: 20px;
            font-family: Arial, sans-serif;
        }
        th, td {
            border: 1px solid #ccc;
            padding: 8px;
            text-align: left;
        }
        th {
            background-color: #f2f2f2;
        }
        th.header-name, td.header-name {
            width: 150px;
        }
        th.header-value, td.header-value {
            width: 200px;
            word-wrap: break-word;
        }
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            margin-top: 50px;
        }
        h1 {
            color: #333;
        }
        a {
            text-decoration: none;
            color: #007bff;
        }
        a:hover {
            text-decoration: underline;
        }
        #orderResult {
            margin-top: 20px;
            font-size: 16px;
            color: green;
        }
        #errorResult {
            margin-top: 20px;
            font-size: 16px;
            color: red;
        }
    </style>
</head>
<body>
    <h1>Welcome to Open-Telemetry Test Application</h1>
    <a href="results">View Database Results</a>
    <br/><br/>

    <!-- Create Order Button -->
    <button id="createOrderBtn">Create Order</button>

    <!-- Result Container -->
    <div id="orderResult"></div>
    <div id="errorResult"></div>

    <h2>HTTP Request Headers</h2>
    <table>
        <tr>
            <th class="header-name">Header Name</th>
            <th class="header-value">Header Value</th>
        </tr>
        <%
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
        %>
        <tr>
            <td class="header-name"><%= headerName %></td>
            <td class="header-value"><%= headerValue %></td>
        </tr>
        <% } %>
    </table>

    <h2>Environment Variables in Tomcat</h2>
    <table border="1">
        <tr>
            <th class="header-name">Variable</th>
            <th class="header-value">Value</th>
        </tr>
        <%
            Map<String, String> env = System.getenv();
            for (Map.Entry<String, String> entry : env.entrySet()) {
        %>
        <tr>
            <td class="header-name"><%= entry.getKey() %></td>
            <td class="header-value"><%= entry.getValue() %></td>
        </tr>
        <% } %>
    </table>

    <script>
        document.getElementById("createOrderBtn").addEventListener("click", function () {
            // Clear previous results
            document.getElementById("orderResult").innerHTML = "";
            document.getElementById("errorResult").innerHTML = "";

            // Send AJAX POST request to /order API
            fetch("order", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            })
            .then(response => response.json()) // Expecting JSON response from backend
            .then(data => {
                if (data && data.order_id) {
                    document.getElementById("orderResult").innerHTML = `
                        <h2>Order Created Successfully!</h2>
                        <p><strong>Order ID:</strong> ${data.order_id}</p>
                        <p><strong>Customer ID:</strong> ${data.customer_id}</p>
                        <p><strong>Amount:</strong> ${data.amount}</p>
                    `;
                } else {
                    document.getElementById("errorResult").innerHTML = "Failed to create order!";
                }
            })
            .catch(error => {
                console.error("Error:", error);
                document.getElementById("errorResult").innerHTML = "An error occurred while creating the order!";
            });
        });
    </script>
</body>
</html>
