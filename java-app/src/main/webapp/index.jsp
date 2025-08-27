<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Web App</title>
<script src="https://unpkg.com/@elastic/apm-rum/dist/bundles/elastic-apm-rum.umd.min.js" crossorigin></script>
<script>
  elasticApm.init({
    serviceName: 'hello-telemetry',
    serverUrl: '/apm-server'
  })
</script>

    <style>
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
    <h1>Welcome to hello-telemetry Application</h1>
    <a href="results">View Database Results</a>
    <br/>
    <br/>
    <form id="orderForm" method="post" action="order"></form>    
    <button onclick="document.getElementById('orderForm').submit()">Create Order</button>

    <%
        // serverUrl: 'https://apmserver.zitaconseil.fr'
        String orderId = request.getParameter("order_id");
        String orderCount = request.getParameter("order_count");

        if (orderId != null && orderCount != null) {
    %>
        <h2>Order Created Successfully!</h2>
        <p><strong>Order ID:</strong> <%= orderId %></p>
        <p><strong>Total Orders Count:</strong> <%= orderCount %></p>
    <%
        }
    %>

</body>
</html>