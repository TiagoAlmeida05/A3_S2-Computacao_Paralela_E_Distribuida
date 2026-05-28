import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RoomManager {
    private final Map<String, ChatRoom> rooms = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ChatRoom getOrCreateRoom(String roomName) {
        lock.writeLock().lock();
        try {
            if (!rooms.containsKey(roomName)) {
                
                // Check if the room is meant to be an AI room
                if (roomName.toLowerCase().startsWith("ai ")|| roomName.equalsIgnoreCase("ai")) {
                    String systemPrompt = "You are a helpful assistant in a group chat. Keep your answers brief and relevant to the conversation.";
                    rooms.put(roomName, new AIChatRoom(roomName, systemPrompt));
                    System.out.println("Created new AI room: " + roomName);
                } else {
                    rooms.put(roomName, new ChatRoom(roomName));
                    System.out.println("Created new room: " + roomName);
                }
            }
            return rooms.get(roomName);
        } finally {
            lock.writeLock().unlock();
        }
    }
}