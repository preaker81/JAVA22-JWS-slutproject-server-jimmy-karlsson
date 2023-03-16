import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        // Declare and initialize socket, input/output streams, buffered readers/writers, and server socket
        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        ServerSocket serverSocket = null;

        try {
            // Create a new server socket with the specified port number
            serverSocket = new ServerSocket(4321);
        } catch (Exception e) {
            // Print any exceptions that occur while creating the server socket
            System.out.println(e);
            return;
        }

        // Loop forever, waiting for clients to connect
        while (true) {
            try {
                // Wait for a client to connect and accept the connection
                socket = serverSocket.accept();

                // Initialize input and output streams to read and write data from/to the client
                inputStreamReader = new InputStreamReader(socket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                // Create buffered readers/writers to read and write data from/to the client
                bufferedReader = new BufferedReader(inputStreamReader);
                bufferedWriter = new BufferedWriter(outputStreamWriter);

                // Loop until the client sends "quit"
                while (true) {
                    // Read the client's message and print it to the console
                    String message = bufferedReader.readLine();
                    System.out.println("Client: " + message);

                    // Send a response to the client
                    bufferedWriter.write("Message received.");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    // If the client sends "quit", exit the loop
                    if (message.equalsIgnoreCase("quit")) {
                        break;
                    }
                }

                // Close the socket and all open resources
                socket.close();
                inputStreamReader.close();
                outputStreamWriter.close();
                bufferedReader.close();
                bufferedWriter.close();
            } catch (Exception e) {
                // Print any exceptions that occur while communicating with the client
                System.out.println(e);
            }
        }
    }
}
