import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Server {
    public static void main(String[] args) {
        // File path for JSON data
        String filepath = "src/json/data.json";

        ServerSocket serverSocket;

        // Create a new server socket and bind it to port 8080
        try {
            serverSocket = new ServerSocket(10000);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }

        // Continuously listen for incoming connections
        while (true) {
            try {
                // Accept a new incoming connection and create a socket
                Socket socket = serverSocket.accept();
                System.out.println("Client connected successfully!");

                // Set up input and output streams for the socket
                InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

                // Flag to control the keep-alive behavior of the server
                boolean keepAlive = true;

                // Keep processing requests while the connection is alive
                while (keepAlive) {
                    // Read the request line
                    String requestLine = bufferedReader.readLine();
                    System.out.println("Client request: " + requestLine);

                    // If the request line is null, break the loop
                    if (requestLine == null) {
                        break;
                    }

                    // Initialize variables to store request headers
                    int contentLength = -1;
                    String message;
                    String connectionHeader = "";

                    // Read and process request headers
                    while (true) {
                        message = bufferedReader.readLine();
                        if (message == null || message.trim().isEmpty()) {
                            break;
                        }

                        // Get the content length from the request headers
                        if (message.startsWith("Content-Length:")) {
                            contentLength = Integer.parseInt(message.substring("Content-Length:".length()).trim());
                        }

                        // Get the connection header from the request headers
                        if (message.startsWith("Connection:")) {
                            connectionHeader = message.substring("Connection:".length()).trim();
                        }
                    }

                    // Handle GET requests
                    if (requestLine.startsWith("GET")) {
                        JSONParser parser = new JSONParser();
                        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(filepath));

                        // Filter books by title if requested
                        if (requestLine.contains("/book?title=")) {

                            String title = requestLine.split("title=", 2)[1].split(" ", 2)[0];

                            String decodeedTitle = decodeValue(title);

                            System.out.println("sout title: " + title);
                            System.out.println("sout decoded title: " + decodeedTitle);

                            JSONArray filteredArray = new JSONArray();
                            for (Object obj : jsonArray) {
                                JSONObject jsonObject = (JSONObject) obj;
                                if (decodeedTitle.equalsIgnoreCase((String) jsonObject.get("title"))) {
                                    filteredArray.add(jsonObject);
                                }
                            }
                            jsonArray = filteredArray;
                        }

                        // Prepare and send the response
                        String response = jsonArray.toJSONString();
                        System.out.println(response);
                        bufferedWriter.write("HTTP/1.1 200 OK\r\n");
                        bufferedWriter.write("Content-Type: application/json\r\n");
                        bufferedWriter.write("Content-Length: " + response.length() + "\r\n");
                        bufferedWriter.write("Connection: keep-alive\r\n");
                        bufferedWriter.write("\r\n");
                        bufferedWriter.write(response);
                    }
                    // Handle POST requests
                    else if (requestLine.startsWith("POST")) {

                        StringBuilder payloadBuilder = new StringBuilder();
                        // Read the request payload
                        if (contentLength > 0) {
                            char[] contentBuffer = new char[contentLength];
                            bufferedReader.read(contentBuffer);
                            payloadBuilder.append(new String(contentBuffer));
                        }

                        // Parse the payload and add it to the JSON array
                        String payload = payloadBuilder.toString();

                        JSONParser parser = new JSONParser();
                        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(filepath));

                        JSONObject jsonObject = (JSONObject) parser.parse(payload);
                        jsonArray.add(jsonObject);

                        // Write the updated JSON array back to the file
                        FileWriter fileWriter = new FileWriter(filepath);
                        fileWriter.write(jsonArray.toJSONString());
                        fileWriter.close();

                        // Prepare and send the response
                        bufferedWriter.write("HTTP/1.1 201 Created\r\n");
                        bufferedWriter.write("Content-Length: 0\r\n");
                        bufferedWriter.write("Connection: keep-alive\r\n");
                        bufferedWriter.write("\r\n");

                        System.out.println("POST request successfully completed!");
                    }
                    // Handle unsupported requests
                    else {
                        bufferedWriter.write("HTTP/1.1 400 Bad Request\r\n");
                        bufferedWriter.write("Content-Length: 0\r\n");
                        bufferedWriter.write("Connection: keep-alive\r\n");
                        bufferedWriter.write("\r\n");
                    }

                    // Flush the output buffer
                    bufferedWriter.flush();

                    // Check the connection header and update the keep-alive flag accordingly
                    if (!"keep-alive".equalsIgnoreCase(connectionHeader)) {
                        keepAlive = false;
                    }
                }

                // Close all resources
                socket.close();
                inputStreamReader.close();
                outputStreamWriter.close();
                bufferedReader.close();
                bufferedWriter.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public static String decodeValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}

