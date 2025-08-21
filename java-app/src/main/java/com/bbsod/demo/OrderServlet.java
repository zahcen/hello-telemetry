import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Get Node.js URL from environment variable, default to http://localhost:3000/order
        String nodeJsUrl = System.getenv("NODEJS_ORDER_URL");
        if (nodeJsUrl == null || nodeJsUrl.isEmpty()) {
            nodeJsUrl = "http://localhost:3000/order";
        }
        System.out.println("nodeJsUrl="+nodeJsUrl);
        // Call internal Node.js service
        URL url = new URL(nodeJsUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Read Node.js response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        System.out.println("content="+content);

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();

        // Parse JSON manually (simple approach)
        String json = content.toString();
        String orderId = json.replaceAll(".*\"order_id\":(\\d+).*", "$1");
        String orderCount = json.replaceAll(".*\"order_count\":(\\d+).*", "$1");
        System.out.println("orderCount="+orderCount);

        // Redirect back to JSP with parameters
        response.sendRedirect("/MyWebApp/index.jsp?order_id=" + orderId + "&order_count=" + orderCount);
    }
}
