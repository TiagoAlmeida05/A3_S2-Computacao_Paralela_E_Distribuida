import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuthManager {
    private final File userFile = new File("users.txt");
    private final File tokenFile = new File("tokens.txt"); 
    
    private static final long TOKEN_LIFESPAN_MS = 60 * 60 * 1000;

    private static class TokenData {
        String username;
        long expirationTime;

        TokenData(String username, long expirationTime) {
            this.username = username;
            this.expirationTime = expirationTime;
        }
    }

    private final Map<String, TokenData> activeTokens = new HashMap<>();
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
        long currentTime = System.currentTimeMillis();
        int loaded = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(tokenFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 3);
                if (parts.length == 3) {
                    long expirationTime = Long.parseLong(parts[2]);
                    
                    if (currentTime < expirationTime) {
                        activeTokens.put(parts[0], new TokenData(parts[1], expirationTime));
                        loaded++;
                    }
                }
            }
            System.out.println("Loaded " + loaded + " active sessions from disk (ignored expired ones).");
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading tokens. File might be corrupted or using old format.");
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
            long expirationTime = System.currentTimeMillis() + TOKEN_LIFESPAN_MS;
            
            activeTokens.put(token, new TokenData(username, expirationTime));
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(tokenFile, true))) {
                writer.println(token + ":" + username + ":" + expirationTime);
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
        lock.writeLock().lock(); 
        try {
            TokenData data = activeTokens.get(token);
            if (data == null) {
                return null;
            }
            
            if (System.currentTimeMillis() > data.expirationTime) {
                System.out.println("Token expired for user: " + data.username);
                activeTokens.remove(token);
                return null;
            }
            
            return data.username;
        } finally {
            lock.writeLock().unlock();
        }
    }
}