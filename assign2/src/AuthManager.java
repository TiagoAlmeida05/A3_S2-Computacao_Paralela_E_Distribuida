import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuthManager {
    private final File userFile = new File("users.txt");
    private final File tokenFile = new File("tokens.txt"); 
    private final Map<String, String> activeTokens = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public AuthManager() {
        try {
            if (userFile.createNewFile()) {
                System.out.println("Created new users.txt file for registration.");
            }
            if (tokenFile.createNewFile()) {
                System.out.println("Created new tokens.txt file for sessions.");
            } else {
                loadTokens();
            }
        } catch (IOException e) {
            System.out.println("Error creating files: " + e.getMessage());
        }
    }

    private void loadTokens() {
        try (BufferedReader reader = new BufferedReader(new FileReader(tokenFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    activeTokens.put(parts[0], parts[1]);
                }
            }
            System.out.println("Loaded " + activeTokens.size() + " active sessions from disk.");
        } catch (IOException e) {
            System.out.println("Error loading tokens.");
        }
    }

    public boolean register(String username, String password) {
        lock.writeLock().lock();
        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(username + ":")) return false;
                }
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(userFile, true))) {
                writer.println(username + ":" + password);
            }
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String login(String username, String password) {
        lock.readLock().lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts[0].equals(username) && parts[1].equals(password)) {
                    return generateToken(username);
                }
            }
            return null; 
        } catch (IOException e) {
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    private String generateToken(String username) {
        lock.readLock().unlock();
        lock.writeLock().lock();
        try {
            String token = UUID.randomUUID().toString();
            activeTokens.put(token, username);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(tokenFile, true))) {
                writer.println(token + ":" + username);
            } catch (IOException e) {
                System.out.println("Could not save token to file.");
            }
            
            return token;
        } finally {
            lock.writeLock().unlock();
            lock.readLock().lock();
        }
    }

    public String getUsernameFromToken(String token) {
        lock.readLock().lock();
        try {
            return activeTokens.get(token);
        } finally {
            lock.readLock().unlock();
        }
    }
}