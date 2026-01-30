package de.feli490.hytale.hyfechats.chat;

import java.util.UUID;

public record ChatMessage(Chat chat, UUID id, UUID senderId, String message, Long timestamp) {

    @Override
    public String toString() {
        return "ChatMessage{" + "chat=" + chat.getId() + ", id=" + id + ", senderId=" + senderId + ", message='" + message + '\''
                + ", timestamp=" + timestamp + '}';
    }
}
