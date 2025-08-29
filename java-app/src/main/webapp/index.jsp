<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Web App</title>

</script>
    <style>
        table {
            border-collapse: collapse;
            width: 80%; /* Table width */
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
        /* Set custom column widths */
        th.header-name, td.header-name {
            width: 150px; /* Adjust as needed */
        }
        th.header-value, td.header-value {
            width: 200px; /* Adjust as needed */
            word-wrap: break-word; /* Handle long header values */
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
    </style>
</head>
<body>
    <h1>Welcome to Open-Telemetry Test Application</h1>
    <a href="results">View Database Results</a>
    <br/>
    <br/>
    <form id="orderForm" method="post" action="order"></form>    
    <button onclick="document.getElementById('orderForm').submit()">Create Order</button>

    <%
        // serverUrl: 'https://apmserver.zitaconseil.fr'
        String orderId = request.getParameter("order_id");
        String customerId = request.getParameter("customer_id");
        String amount = request.getParameter("amount");

        if (orderId != null) {
    %>
        <h2>Order Created Successfully!</h2>
        <p><strong>Order ID:</strong> <%= orderId %></p>
        <p><strong>Customer Id:</strong> <%= customerId %></p>
        <p><strong>Amount:</strong> <%= amount %></p>

    <%
        }
    %>

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

</body>
</html>