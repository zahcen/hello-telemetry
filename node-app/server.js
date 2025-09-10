const express = require("express");
const cors = require("cors");
const { context, trace } = require('@opentelemetry/api');

const app = express();
const PORT = process.env.PORT || 3000; // Allow dynamic port for Kubernetes


app.use((req, res, next) => {
  // Wait until the response is sent
  res.on("finish", () => {
    const currentSpan = trace.getSpan(context.active());
    if (currentSpan) {
      
      currentSpan.updateName("Product Transaction Name");
      // const xPage = res.getHeader("x-page");
      // if (xPage) {
      //   currentSpan.updateName(`page-${xPage}`);
      // }
    }
  });
  next();
});


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

// Home Page
app.get('/', (req, res) => {

//  const span = trace.getSpan(trace.context.active());
//  if (span) {
//    span.updateName(`Home Page`);
//  }

  res.send('<h1>This is the Home Page</h1>');
});

// Product Pages
app.get('/p/product3.html', (req, res) => {
  const currentSpan = trace.getSpan(context.active());
  console.log("product3");
  if (currentSpan) {
    console.log("Found span");
    // Add the product ID as a custom attribute
    currentSpan.setAttribute("product.id", "product_id");
    // (Optional) Add other useful attributes
    currentSpan.setAttribute("x-page-attribute", "product");
  }
  res.send('<h1>This is the Product Page 3</h1>');
});

app.get('/p/product1.html', (req, res) => {
  res.send('<h1>This is the Product Page 1</h1>');
});

app.get('/p/product2.html', (req, res) => {
  res.send('<h1>This is the Product Page 2</h1>');
});

app.get('/p/*', (req, res) => {
  console.log("Found Add x-page header");
  res.set("x-page", "product");
  res.send('<h1>This is the Product Page v2</h1>');
});

app.get('/p/:slug', (req, res) => {
  res.set("x-page", "product");
  res.send(`<h1>Product Page: ${req.params.slug}</h1>`);
});

app.get('/c/:slug', (req, res) => {
  res.set("x-page", "category");
  res.send(`<h1>Category Page: ${req.params.slug}</h1>`);
});


// Category Page
// app.get('/category', (req, res) => {
//   res.send('<h1>This is the Category Page v1</h1>');
// });

// app.get('/c/*', (req, res) => {
//   res.send('<h1>This is the Category Page v2</h1>');
// });

// Handle 404
app.use((req, res) => {
  res.status(404).send('<h1>404 - Page Not Found</h1>');
});


// Start the server
app.listen(PORT, () => {
    console.log(`Node.js Order API running on http://localhost:${PORT}`);
});
