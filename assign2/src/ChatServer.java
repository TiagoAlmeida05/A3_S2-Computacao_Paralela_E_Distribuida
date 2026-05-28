import java.io.*;
import java.net.*;

public class ChatServer {
    private static final RoomManager roomManager = new RoomManager();
    private static final AuthManager authManager = new AuthManager();

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat Server listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread.ofVirtual().start(() -> handleClient(clientSocket));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String username = null;
            ChatRoom currentRoom = null;

            String command;
            while ((command = reader.readLine()) != null) {
                String[] parts = command.split(" ", 2);
                String action = parts[0].toUpperCase();

                if (username == null) {
                    if (action.equals("REGISTER") && parts.length == 2) {
                        String[] auth = parts[1].split(" ", 2);
                        if (auth.length == 2) {
                            boolean success = authManager.register(auth[0], auth[1]);
                            writer.println(success ? "SUCCESS Registered successfully." : "ERROR Username taken.");
                        }
                    } else if (action.equals("LOGIN") && parts.length == 2) {
                        String[] auth = parts[1].split(" ", 2);
                        if (auth.length == 2) {
                            String token = authManager.login(auth[0], auth[1]);
                            if (token != null) {
                                username = auth[0];
                                writer.println("TOKEN " + token);
                                writer.println("SYSTEM Available rooms: " + roomManager.getAvailableRooms());
                            } else {
                                writer.println("ERROR Invalid credentials.");
                            }
                        }
                    } else if (action.equals("RECONNECT") && parts.length == 2) {
                        username = authManager.getUsernameFromToken(parts[1]);
                        if (username != null) {
                            writer.println("SUCCESS Welcome back, " + username);
                            writer.println("SYSTEM Available rooms: " + roomManager.getAvailableRooms());
                        } else {
                            writer.println("ERROR Invalid or expired token.");
                        }
                    }
                    continue; 
                }

                if (action.equals("JOIN") && parts.length == 2) {
                    if (currentRoom != null) currentRoom.removeClient(writer, username);
                    currentRoom = roomManager.getOrCreateRoom(parts[1]);
                    currentRoom.addClient(writer, username);
                    writer.println("SYSTEM Joined room: " + parts[1]);
                } else if (action.equals("LIST")) {
                    writer.println("SYSTEM Available rooms: " + roomManager.getAvailableRooms());
                } else if (action.equals("MSG") && parts.length == 2 && currentRoom != null) {
                    currentRoom.broadcast(username, parts[1]);
                } else if (action.equals("BYE")) {
                    break;
                }
            }

            if (currentRoom != null && username != null) {
                currentRoom.removeClient(writer, username);
            }

        } catch (IOException ex) {
            System.out.println("Connection dropped.");
        } finally {
            try { socket.close(); } catch (IOException e) { }
        }
    }
}