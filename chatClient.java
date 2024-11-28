import java.io.*;
import java.net.*;

public class chatClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";  // Localhost IP address
    private static final int SERVER_PORT = 12345;  // Port number for the server
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            // Initialize input and output streams for the socket
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            // Get username from user input
            System.out.println("Enter your username:");
            String userName = userInput.readLine();
            out.println(userName);

            // Create a listener thread to receive messages from the server
            Thread listenerThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println(message);  // Print incoming messages
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            listenerThread.start();

            // Send messages to the server
            String message;
            while ((message = userInput.readLine()) != null) {
                out.println(message);
                if (message.equalsIgnoreCase("quit")) {
                    break;  // Exit the loop and close the connection
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
