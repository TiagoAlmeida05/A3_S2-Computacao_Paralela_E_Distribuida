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
                rooms.put(roomName, new ChatRoom(roomName));
                System.out.println("Created new room: " + roomName);
            }
            return rooms.get(roomName);
        } finally {
            lock.writeLock().unlock();
        }
    }
}