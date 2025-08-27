<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Web App</title>
    <script src="/MyWebApp/metrics.js"></script>
<!--script src="https://unpkg.com/@elastic/apm-rum/dist/bundles/elastic-apm-rum.umd.min.js" crossorigin></script-->
<script>
  elasticApm.init({
<<<<<<< HEAD
    serviceName: 'hello-telemetry',
    serverUrl: '/apm-server'
=======
    serviceName: 'open-telemetry-test',
    serverUrl: '/metrics-api',
    serverUrlPrefix: '/'
>>>>>>> 5177ad792838583231e22c9d831150de418cf015
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
    <h1>Welcome to Open-Telemetry Test Application</h1>
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