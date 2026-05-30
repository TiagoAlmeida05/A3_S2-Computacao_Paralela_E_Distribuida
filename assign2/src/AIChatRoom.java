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
        super.broadcast(sender, message);

        synchronized (messageHistory) {
            messageHistory.add(sender + ": " + message);
        }

        if (!sender.equals("Bot") && !sender.equals("System")) {
            Thread.ofVirtual().start(this::askOllama);
        }
    }

    private void askOllama() {
        try {
            String fullContext;
            synchronized (messageHistory) {
                fullContext = systemPrompt + "\\n\\n" + String.join("\\n", messageHistory) + "\\nBot:";
            }

            String escapedContext = fullContext.replace("\"", "\\\"").replace("\n", "\\n");
            
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

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String botReply = extractResponseValue(response.body());

            if (botReply != null && !botReply.isBlank()) {
                super.broadcast("Bot", botReply);
                
                synchronized (messageHistory) {
                    messageHistory.add("Bot: " + botReply);
                }
            }

        } catch (Exception e) {
            System.out.println("Ollama API Error: " + e.getMessage());
        }
    }

 
    private String extractResponseValue(String json) {
        String target = "\"response\":\"";
        int startIndex = json.indexOf(target);
        if (startIndex == -1) return "I encountered an error.";
        
        startIndex += target.length();
        
        int endIndex = startIndex;
        while (endIndex < json.length()) {
            if (json.charAt(endIndex) == '"' && json.charAt(endIndex - 1) != '\\') {
                break;
            }
            endIndex++;
        }
        
        return json.substring(startIndex, endIndex).replace("\\n", "\n");
    }
}