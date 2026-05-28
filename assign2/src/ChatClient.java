import java.net.*;
import java.io.*;

public class ChatClient {
    private static String sessionToken = null;
    private static String currentRoom = "Lobby"; 
    private static volatile boolean running = true;

    public static void main(String[] args) {
        String hostname = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
        
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        while (running) {
            try {
                connectAndRun(hostname, port, consoleReader);
            } catch (IOException e) {
                if (running) {
                    System.out.println("Connection to server lost. Attempting to reconnect in 3 seconds...");
                    try { Thread.sleep(3000); } catch (InterruptedException ie) { }
                }
            }
        }
    }

    private static void connectAndRun(String hostname, int port, BufferedReader consoleReader) throws IOException {
        try (Socket socket = new Socket(hostname, port)) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if (sessionToken != null) {
                System.out.println("Attempting to resume session...");
                writer.println("RECONNECT " + sessionToken);
                String response = serverReader.readLine();
                if (response == null || response.startsWith("ERROR")) {
                    System.out.println("Session expired. Please log in again.");
                    sessionToken = null;
                    authenticate(writer, serverReader, consoleReader);
                }
            } else {
                authenticate(writer, serverReader, consoleReader);
            }

            writer.println("JOIN " + currentRoom);

            Thread.ofVirtual().start(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverReader.readLine()) != null) {
                        if (!serverMessage.startsWith("TOKEN")) { 
                            System.out.println(serverMessage);
                        }
                    }
                } catch (IOException e) {
                } finally {
                    try { socket.close(); } catch (IOException e) {} 
                    
                    System.out.println("\n[Server disconnected. Press ENTER to attempt a reconnect...]");
                }
            });

            while (running) {
                String text = consoleReader.readLine();
                
                if (text == null) break;

                if (text.equalsIgnoreCase("bye")) {
                    writer.println("BYE");
                    running = false;
                    break;
                } else if (text.equalsIgnoreCase("/list")) {
                    writer.println("LIST");
                } else if (text.startsWith("/join ")) {
                    currentRoom = text.substring(6).trim();
                    writer.println("JOIN " + currentRoom);
                } else {
                    writer.println("MSG " + text);
                }

                if (writer.checkError()) {
                    throw new IOException("Server disconnected");
                }
            }
        }
    }

    private static void authenticate(PrintWriter writer, BufferedReader serverReader, BufferedReader consoleReader) throws IOException {
        Console console = System.console();
        
        while (sessionToken == null) {
            System.out.println("Do you want to (1) Login or (2) Register?");
            String choice = consoleReader.readLine();
            
            System.out.print("Username: ");
            String user = consoleReader.readLine();
            
            String pass = "";
            if (console != null) {
                char[] passwordArray = console.readPassword("Password: ");
                pass = new String(passwordArray);
            } else {
                System.out.print("Password: ");
                pass = consoleReader.readLine();
            }

            if (choice.equals("1")) {
                writer.println("LOGIN " + user + " " + pass);
            } else {
                writer.println("REGISTER " + user + " " + pass);
                System.out.println(serverReader.readLine()); 
                continue;
            }

            String response = serverReader.readLine();
            if (response != null && response.startsWith("TOKEN")) {
                sessionToken = response.substring(6);
                System.out.println("Logged in successfully! Type '/join <room>' to change rooms or 'bye' to quit.");
            } else {
                System.out.println(response != null ? response : "Server disconnected.");
                if (response == null) throw new IOException("Server dropped connection.");
            }
        }
    }
}