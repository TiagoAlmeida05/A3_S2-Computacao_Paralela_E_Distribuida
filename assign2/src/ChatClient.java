import java.net.*;
import java.io.*;
import java.util.Scanner;

public class ChatClient {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ChatClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            
            // Spawn a virtual thread dedicated to reading incoming messages from the server
            Thread.ofVirtual().start(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = reader.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });

            // Main thread dedicated to reading user input and sending it
            Scanner consoleScanner = new Scanner(System.in);
            while (true) {
                String text = consoleScanner.nextLine();
                writer.println(text);
                
                if (text.equalsIgnoreCase("bye")) {
                    break;
                }
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}