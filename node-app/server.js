const express = require("express");
const cors = require("cors");

const app = express();
const PORT = process.env.PORT || 3000; // Allow dynamic port for Kubernetes

// Middleware
app.use(cors());
app.use(express.json()); // Replaces body-parser.json()

// POST /order → increments counter and generates a random order ID
app.post("/payment", (req, res) => {
 const { orderId, customerId, amount } = req.body;

    // Validate required fields
    if (!orderId || !customerId || amount === undefined) {
        return res.status(400).json({ error: "Missing required fields" });
    }

    // Determine payment status
    let paymentStatus = amount > 400 ? "0" : "1";

    // Send response back
    res.json({
        order_id: orderId,
        customer_id: customerId,
        payment_status: paymentStatus
    });
});

// Start the server
app.listen(PORT, () => {
    console.log(`Node.js Order API running on http://localhost:${PORT}`);
});
