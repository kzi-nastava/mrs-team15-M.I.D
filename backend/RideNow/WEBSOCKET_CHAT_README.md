# WebSocket Chat Implementation

This project now includes a WebSocket-based chat system that allows real-time messaging between users and administrators.

## Overview

The chat system consists of:
- **REST API endpoints** for basic chat operations (get chats, get chat by user, etc.)
- **WebSocket connection** for real-time messaging

## REST API Endpoints

### Admin Endpoints
- `GET /api/chats/` - Get all chats (Admin only)
- `GET /api/chats/{id}` - Get chat by ID (Admin only)
- `POST /api/chats/message/{id}` - Send message via REST (Admin/User/Driver)

### User Endpoints
- `GET /api/chats/user` - Get user's chat (creates if doesn't exist) (User/Driver only)
- `POST /api/chats/message/{id}` - Send message via REST (Admin/User/Driver)

## WebSocket Connection

### Connection URL
```
ws://localhost:8080/api/chat/websocket/{chatId}?token={jwtToken}
```

### Parameters
- `{chatId}` - The ID of the chat to connect to
- `{jwtToken}` - Valid JWT authentication token

### Authentication & Authorization
- Users can only connect to their own chat
- Admins can connect to any chat
- Invalid tokens or unauthorized access will result in connection closure

### Message Format

#### Sending Messages
Send a JSON message through the WebSocket:
```json
{
  "content": "Your message text here"
}
```

#### Receiving Messages
You'll receive different types of messages:

**New Message:**
```json
{
  "type": "message",
  "data": {
    "content": "Message content",
    "userSender": true,
    "timestamp": "2026-01-28T10:30:00"
  }
}
```

**Existing Messages (sent upon connection):**
```json
{
  "type": "existing_messages",
  "data": [
    {
      "content": "Previous message 1",
      "userSender": false,
      "timestamp": "2026-01-28T09:30:00"
    },
    {
      "content": "Previous message 2", 
      "userSender": true,
      "timestamp": "2026-01-28T10:00:00"
    }
  ]
}
```

**Error Messages:**
```json
{
  "error": "Error description"
}
```

### Message Fields
- `content` - The text content of the message
- `userSender` - Boolean indicating if message was sent by a user (true) or admin (false)
- `timestamp` - When the message was created

## Usage Flow

### For Users/Drivers:
1. Get your chat ID by calling `GET /api/chats/user`
2. Connect to WebSocket using the chat ID and your JWT token
3. Send and receive messages in real-time

### For Admins:
1. Get all chats by calling `GET /api/chats/`
2. Choose a specific chat and connect using its ID
3. Send and receive messages in real-time

## Frontend Integration Example

```javascript
// Get chat ID first (for users)
const chatResponse = await fetch('/api/chats/user', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const chat = await chatResponse.json();

// Connect to WebSocket
const ws = new WebSocket(`ws://localhost:8080/api/chat/websocket/${chat.id}?token=${token}`);

// Handle incoming messages
ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  
  if (message.type === 'message') {
    // Display new message
    displayMessage(message.data);
  } else if (message.type === 'existing_messages') {
    // Display chat history
    displayMessageHistory(message.data);
  } else if (message.error) {
    // Handle error
    console.error('WebSocket error:', message.error);
  }
};

// Send a message
const sendMessage = (content) => {
  ws.send(JSON.stringify({ content }));
};
```

## Features

- **Real-time messaging** - Messages appear immediately for all connected users
- **Message history** - Previous messages are loaded when connecting
- **Access control** - Users can only access their own chats, admins can access any chat
- **Error handling** - Proper error messages for authentication and other failures
- **Connection management** - Automatic cleanup when connections are closed
- **Broadcasting** - Messages are sent to all connected clients in the same chat

## Technical Details

- WebSocket handler: `ChatWebSocketHandler.java`
- Service layer: `ChatService.java` (contains both REST and WebSocket methods)
- Configuration: `WebSocketConfig.java`
- Models: `Chat.java`, `Message.java`
- Controllers: `ChatController.java` (REST endpoints)

The system maintains backward compatibility with existing REST API endpoints while adding real-time capabilities through WebSockets.
