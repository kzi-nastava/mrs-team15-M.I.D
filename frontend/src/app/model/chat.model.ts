export interface Chat {
  id: number;
  user: string;
  messages?: Message[]; // Optional for list view
}

export interface Message {
  content: string;
  userSender: boolean;
  timestamp: string;
}

export interface WebSocketMessage {
  type?: 'message' | 'existing_messages';
  data?: Message | Message[];
  error?: string;
}

export interface SendMessageRequest {
  content: string;
}
