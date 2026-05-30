# Concurrent Chat Server with AI Integration

This project is a high-performance, fault-tolerant distributed chat server built in Java SE 21+. It features custom thread-safe concurrency, token-based session resumption, and local AI (LLM) integration using Ollama—all implemented without external dependencies.

## Project Structure
- `ChatServer.java`: The main server entry point handling virtual threads and routing.
- `ChatClient.java`: The client application featuring auto-reconnection and blocking I/O.
- `AuthManager.java`: Handles registration, secure login, and persistent, time-expiring session tokens.
- `RoomManager.java`: Manages room creation, tracking, and dynamic AI-room routing.
- `ChatRoom.java`: Handles standard room broadcasting logic with non-blocking scatter-gather virtual threads.
- `AIChatRoom.java`: Extends `ChatRoom` to provide an asynchronous interface to a local LLM.

## Prerequisites
- **Java SE 21+**: Required to utilize Virtual Threads.
- **Ollama**: Required for AI functionality (Download at ollama.com).

## Setup & Running Instructions

### 1. Start the AI Engine (Ollama)
Ensure you have Ollama installed. Open a terminal (or Command Prompt) and run the following command to start the Llama 3 model:
> ollama run llama3

(Note: Keep this terminal open and running in the background to use AI chat features).

### 2. Compile the Project
Open a new terminal in the project directory where all your .java files are saved and compile them:
> javac *.java

### 3. Run the Server
Start the server on your desired port (e.g., 8080):
> java ChatServer 8080

### 4. Run the Client(s)
Open a new terminal window for each client you wish to simulate and connect to the server:
> java ChatClient localhost 8080

## Usage & Commands
- **Authentication**: Follow the on-screen prompts to register a new user (2) or login (1). Passwords are automatically masked in standard terminals.
- **Chat Commands**:
  - `/list`: Display all available chat rooms on the server.
  - `/join <room_name>`: Join an existing room or dynamically create a new one.
  - **AI Rooms**: If you name a room starting with "ai" (e.g., `/join AI Study`), the server will automatically spawn an AI bot in the room to answer your queries.
  - `bye`: Disconnect and close the client.
- **Messaging**: To chat, simply type your message and press ENTER.

## Fault Tolerance Features
- **Auto-Recovery**: If the server crashes or your network drops, the client will automatically detect the disconnection and attempt to reconnect every 3 seconds.
- **Seamless Resumption**: Upon successful reconnection, the client uses a secure, persistent UUID token to resume your session and instantly rejoin your previous chat room without forcing you to log in again.