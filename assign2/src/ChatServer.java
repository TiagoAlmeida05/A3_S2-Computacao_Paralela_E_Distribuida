import java.io.*;
import java.net.*;

public class ChatServer {
    // Centralized manager for all rooms
    private static final RoomManager roomManager = new RoomManager();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ChatServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread.ofVirtual().start(() -> handleClient(clientSocket));
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true)
        ) {
            writer.println("Enter your username:");
            String username = reader.readLine();

            writer.println("Enter room name to join:");
            String roomName = reader.readLine();

            ChatRoom room = roomManager.getOrCreateRoom(roomName);
            writer.println("Room: " + roomName);
            room.addClient(writer, username);

            String text;
            while ((text = reader.readLine()) != null) {
                if (text.equalsIgnoreCase("bye")) {
                    break;
                }
                room.broadcast(username, text);
            }

            room.removeClient(writer, username);

        } catch (IOException ex) {
            System.out.println("Client handler exception: " + ex.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}