const express = require("express");
const cors = require("cors");

const app = express();
const PORT = process.env.PORT || 3000; // Allow dynamic port for Kubernetes


// Middleware
app.use(cors());
app.use(express.json()); // Replaces body-parser.json()

// Helper function to simulate workload
function simulateWorkload(ms = 300) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// Alternative: CPU-intensive workload simulation
function simulateCPUWorkload(ms = 300) {
    const start = Date.now();
    while (Date.now() - start < ms) {
        // Busy wait to simulate CPU work
        Math.random() * Math.random();
    }
}

// POST /order → increments counter and generates a random order ID
app.post("/payment", (req, res) => {
    

    console.log("=== Incoming /payment request ===");
    console.log("Headers:", req.headers);
    console.log("Body received:", req.body);


    const { orderId, customerId, amount } = req.body;
    
    console.log("Parsed fields => orderId:", orderId, "customerId:", customerId, "amount:", amount);

    // Validate required fields
    if (orderId === undefined || customerId === undefined || amount === undefined) {
        console.error("Missing required fields in request body");
        return res.status(400).json({ 
            error: "Missing required fields", 
            received: req.body 
        });
    }

    simulateCPUWorkload(300);
    
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
