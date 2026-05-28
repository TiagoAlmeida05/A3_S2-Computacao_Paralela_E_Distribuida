import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RoomManager {
    private final Map<String, ChatRoom> rooms = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RoomManager() {
        rooms.put("Lobby", new ChatRoom("Lobby"));
    }

    public ChatRoom getOrCreateRoom(String roomName) {
        lock.writeLock().lock();
        try {
            if (!rooms.containsKey(roomName)) {
                rooms.put(roomName, new ChatRoom(roomName));
                System.out.println("Created new room: " + roomName);
            }
            return rooms.get(roomName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getAvailableRooms() {
        lock.readLock().lock();
        try {
            if (rooms.isEmpty()) return "None";
            return String.join(", ", rooms.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }
}