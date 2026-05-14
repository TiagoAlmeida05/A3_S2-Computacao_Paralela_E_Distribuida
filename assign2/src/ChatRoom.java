import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChatRoom {
    private final String name;
    private final List<PrintWriter> clientWriters = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ChatRoom(String name) {
        this.name = name;
    }

    public void addClient(PrintWriter writer, String username) {
        lock.writeLock().lock();
        try {
            clientWriters.add(writer);
        } finally {
            lock.writeLock().unlock();
        }
        broadcast("System", "[" + username + " enters the room]");
    }

    public void removeClient(PrintWriter writer, String username) {
        lock.writeLock().lock();
        try {
            clientWriters.remove(writer);
        } finally {
            lock.writeLock().unlock();
        }
        broadcast("System", "[" + username + " leaves the room]");
    }

    public void broadcast(String sender, String message) {
        String formattedMessage = sender + ": " + message;
        
        lock.readLock().lock();
        try {
            for (PrintWriter writer : clientWriters) {

                Thread.ofVirtual().start(() -> {
                    synchronized(writer) {
                        writer.println(formattedMessage);
                    }
                });
            }
        } finally {
            lock.readLock().unlock();
        }
    }
}