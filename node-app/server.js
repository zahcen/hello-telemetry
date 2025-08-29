const express = require("express");
const cors = require("cors");

const app = express();
const PORT = process.env.PORT || 3000; // Allow dynamic port for Kubernetes

// Middleware
app.use(cors());
app.use(express.json()); // Replaces body-parser.json()

let orderCount = 0;

// Function to generate a random order ID between 10000 and 99999
function generateOrderId() {
    return Math.floor(Math.random() * (99999 - 10000 + 1)) + 10000;
}

// POST /order → increments counter and generates a random order ID
app.post("/order", (req, res) => {
    orderCount++;
    const orderId = generateOrderId();
    const customerId= generateOrderId();
    const amount=Math.floor(Math.random() * 1000) + 500;
    res.json({
        order_id: orderId,
        customer_id:customerId,
        amount:amount
    });
});

// Start the server
app.listen(PORT, () => {
    console.log(`Node.js Order API running on http://localhost:${PORT}`);
});
