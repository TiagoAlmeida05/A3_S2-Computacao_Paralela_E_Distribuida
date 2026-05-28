import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class AIChatRoom extends ChatRoom {
    private final String systemPrompt;
    private final List<String> messageHistory = new ArrayList<>();
    private final HttpClient httpClient;

    public AIChatRoom(String name, String systemPrompt) {
        super(name); 
        this.systemPrompt = systemPrompt;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void broadcast(String sender, String message) {
        // Broadcast the message to all users normally
        super.broadcast(sender, message);

        // Safely add the message to the room's context history
        synchronized (messageHistory) {
            messageHistory.add(sender + ": " + message);
        }

        // If the message isn't from the Bot or System, trigger the LLM
        if (!sender.equals("Bot") && !sender.equals("System")) {
            // Spawn a virtual thread so the chat doesn't freeze while waiting for Ollama
            Thread.ofVirtual().start(this::askOllama);
        }
    }

    private void askOllama() {
        try {
            // Build the full context string to send to the AI
            String fullContext;
            synchronized (messageHistory) {
                fullContext = systemPrompt + "\\n\\n" + String.join("\\n", messageHistory) + "\\nBot:";
            }

            // Escape quotes and newlines for the JSON payload
            String escapedContext = fullContext.replace("\"", "\\\"").replace("\n", "\\n");
            
            // Format matches your cURL example
            String jsonPayload = "{" +
                    "\"model\": \"llama3\"," +
                    "\"prompt\": \"" + escapedContext + "\"," +
                    "\"stream\": false" +
                    "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Extract the bot's text from the JSON response
            String botReply = extractResponseValue(response.body());

            if (botReply != null && !botReply.isBlank()) {
                // Broadcast the Bot's response back to the room
                super.broadcast("Bot", botReply);
                
                // Add the Bot's response to the history so it remembers what it said
                synchronized (messageHistory) {
                    messageHistory.add("Bot: " + botReply);
                }
            }

        } catch (Exception e) {
            System.out.println("Ollama API Error: " + e.getMessage());
        }
    }

 
    // method to extract the "response" field from Ollama's JSON.
    private String extractResponseValue(String json) {
        String target = "\"response\":\"";
        int startIndex = json.indexOf(target);
        if (startIndex == -1) return "I encountered an error.";
        
        startIndex += target.length();
        
        // find the end quote, ignoring escaped quotes inside the response
        int endIndex = startIndex;
        while (endIndex < json.length()) {
            if (json.charAt(endIndex) == '"' && json.charAt(endIndex - 1) != '\\') {
                break;
            }
            endIndex++;
        }
        
        // unescape newlines so they print correctly in the chat
        return json.substring(startIndex, endIndex).replace("\\n", "\n");
    }
}