import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class chatServer {

    // List to keep track of connected clients
    private static Set<PrintWriter> clients = new HashSet<>();

    // Method to save chat history to a file
    public static void saveChatHistory(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("chat_history.txt", true))) {
            // Format the message with a timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write("[" + timestamp + "] " + message);
            writer.newLine();  // Add a new line after each message
        } catch (IOException e) {
            System.err.println("Error writing to chat history file: " + e.getMessage());
        }
    }

    // ClientHandler class to handle individual client communication
    private static class ClientHandler implements Runnable {
        private BufferedReader in;
        private PrintWriter out;
        private Socket clientSocket;
        private String clientName;

        // Constructor
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                System.err.println("Error initializing client handler: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                // Ask for the client's name
                out.println("Enter your name: ");
                clientName = in.readLine();
                out.println("Welcome " + clientName + "! Type 'exit' to quit.");

                // Add the client to the clients list
                synchronized (clients) {
                    clients.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }

                    // Broadcast message to all clients
                    synchronized (clients) {
                        for (PrintWriter clientOut : clients) {
                            clientOut.println(clientName + ": " + message);
                        }
                    }

                    // Save the message to chat history
                    saveChatHistory(clientName + ": " + message);  // Call saveChatHistory here to store the message
                }
            } catch (IOException e) {
                System.err.println("Error in client handler: " + e.getMessage());
            } finally {
                try {
                    // Clean up after client disconnects
                    synchronized (clients) {
                        clients.remove(out);
                    }
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }

    // Main method to start the server
    public static void main(String[] args) {
        System.out.println("Chat Server started...");
        try (ServerSocket serverSocket = new ServerSocket(12345)) {  // Listening on port 12345
            while (true) {
                // Accept new client connections
                new Thread(new ClientHandler(serverSocket.accept())).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}
