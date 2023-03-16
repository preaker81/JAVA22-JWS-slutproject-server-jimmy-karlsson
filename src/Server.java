import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            // Create a new server socket that listens on port 10000
            serverSocket = new ServerSocket(10000);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        while (true) {
            try (
                    // Accept a new client connection
                    Socket socket = serverSocket.accept();
                    // Create a buffered reader to read the client input
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    // Create a buffered writer to write the server response
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
            ) {
                // Read the request headers
                String inputLine;
                StringBuilder requestBody = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    // Stop reading headers when an empty line is encountered
                    if (inputLine.isEmpty()) {
                        break;
                    }
                }
                // Read the request body, if any
                while ((inputLine = in.readLine()) != null) {
                    requestBody.append(inputLine);
                }
                // Determine the request method
                String method = "GET";
                if (requestBody.length() > 0) {
                    method = "POST";
                }
                // Call the handleRequest method to generate a response
                String response = handleRequest(method, requestBody.toString());
                // Send the response back to the client
                out.write("HTTP/1.1 200 OK\r\n");
                out.write("Content-Type: application/json\r\n");
                out.write("Content-Length: " + response.length() + "\r\n");
                out.write("\r\n");
                out.write(response);
                out.flush();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private static String handleRequest(String method, String requestBody) {
        if (method.equalsIgnoreCase("GET")) {
            // Handle GET request
            return "{\"message\": \"GET request received\"}";
        } else if (method.equalsIgnoreCase("POST")) {
            // Handle POST request
            return "{\"message\": \"POST request received\", \"data\": " + requestBody + "}";
        } else {
            // Invalid request method
            return "{\"error\": \"Invalid method\"}";
        }
    }
}
