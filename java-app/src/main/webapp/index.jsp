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
            color: black;
        }
        #errorResult {
            margin-top: 20px;
            font-size: 16px;
            color: red;
        }
  
        #ordersTable {
            margin: 20px auto;       /* Center table */
            width: 70%;              /* Adjust width */
            border-collapse: collapse;
            font-family: Arial, sans-serif;
            text-align: center;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        #ordersTable th, #ordersTable td {
            border: 1px solid #ccc;
            padding: 10px;
        }

        #ordersTable th {
            background-color: #007bff;
            color: white;
            font-size: 16px;
        }

        #ordersTable td {
            font-size: 14px;
        }

        /* Spinner style */
        #loadingSpinner {
            display: none;
            border: 4px solid #f3f3f3;
            border-top: 4px solid #007bff;
            border-radius: 50%;
            width: 22px;
            height: 22px;
            animation: spin 1s linear infinite;
            margin-left: 10px;
            vertical-align: middle;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

    </style>
</head>
<body>
    <h1>Welcome to Open-Telemetry Test Application</h1>
    <a href="results">View Database Results</a>
    <br/><br/>


    <!-- Create Order Button -->
    <button id="createOrderBtn">Create Order</button>
    <div id="loadingSpinner"></div>
    <label><input type="checkbox" id="addMetricsCheckbox">With Metrics</label>
    
    <h2>Order History</h2>
    <table id="ordersTable">
        <thead>
            <tr>
                <th>#</th>
                <th>Date/Time</th>
                <th>Order ID</th>
                <th>Customer ID</th>
                <th>Amount</th>
                <th>Payment Method</th>
                <th>Payment Status</th>
            </tr>
        </thead>
        <tbody id="ordersBody">
            <!-- Orders will be appended here -->
        </tbody>
    </table>

    <!-- Result Container -->
    <div id="orderResult"></div>
    <div id="errorResult"></div>

    <%
    if (1==2){
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
    <% } %>
    <script>
        let orderCount = 0; // global counter

        document.getElementById("createOrderBtn").addEventListener("click", function () {
            const spinner = document.getElementById("loadingSpinner");
            spinner.style.display = "inline-block";

            // Clear previous errors only, keep old orders
            document.getElementById("errorResult").innerHTML = "";
            // Read checkbox value
            const addMetrics = document.getElementById("addMetricsCheckbox").checked;

            // Prepare form data
            const formData = new URLSearchParams();
            formData.append("add_metrics", addMetrics); // true or false
            
            fetch("order", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: formData.toString()
            })
            .then(response => response.json())
            .then(data => {
                console.log("API Response:", data);

                if (data && data.order_id) {
                    orderCount++; // increment counter
                    // Determine payment status label
                    let paymentLabel;
                    if (data.payment_status) {
                        paymentLabel = '<span style="color:green;font-weight:bold;">Successful ✅</span>';
                    } else if (! data.payment_status) {
                        paymentLabel = '<span style="color:red;font-weight:bold;">Payment Error ❌</span>';
                    } else {
                        paymentLabel = '<span style="color:orange;font-weight:bold;">Unknown</span>';
                    }

                    // Get current timestamp
                    const now = new Date();
                    const formattedDate = now.getFullYear() + "-" +
                    String(now.getMonth() + 1).padStart(2, '0') + "-" +
                    String(now.getDate()).padStart(2, '0') + " " +
                    String(now.getHours()).padStart(2, '0') + ":" +
                    String(now.getMinutes()).padStart(2, '0') + ":" +
                    String(now.getSeconds()).padStart(2, '0');

                    // Append new row to the table
                    const ordersBody = document.getElementById("ordersBody");
                    const newRow = document.createElement("tr");

                    newRow.innerHTML = `
                        <td>\${orderCount}</td>
                        <td>\${formattedDate}</td>
                        <td>\${data.order_id}</td>
                        <td>\${data.customer_id}</td>
                        <td>\${data.amount} €</td>
                        <td>\${data.payment_method}</td>
                        <td>\${paymentLabel}</td>
                    `;

                    ordersBody.appendChild(newRow);
                } else {
                    document.getElementById("errorResult").innerHTML = "Failed to create order!";
                }
            })
            .catch(error => {
                console.error("Error:", error);
                document.getElementById("errorResult").innerHTML = "An error occurred while creating the order!";
            })
            .finally(() => {
                // Hide spinner once the request finishes
                spinner.style.display = "none";
            });
        });



        document.getElementById("createOrderBtn_1").addEventListener("click", function () {
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
                console.log("API Response:", data);
                if (data && data.order_id) {
                    document.getElementById("orderResult").innerHTML = `
                        <h2>Order Created Successfully!</h2>
                        <p><strong>Order ID:</strong> \${data.order_id}</p>
                        <p><strong>Customer ID:</strong> \${data.customer_id}</p>
                        <p><strong>Amount:</strong> \${data.amount}</p>
                    `;
                    // Check payment status
                    if (!data.payment_status) {
                        document.getElementById("errorResult").innerHTML = `
                            <h3 style="color: red;">Payment Error</h3>
                        `;
                    } else if (data.payment_status) {
                        document.getElementById("orderResult").innerHTML += `
                            <p><strong>Payment:</strong> Successful ✅</p>
                        `;
                    } else {
                        document.getElementById("errorResult").innerHTML = `
                            <h3 style="color: orange;">Payment status unknown</h3>
                        `;
                    }                    
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
