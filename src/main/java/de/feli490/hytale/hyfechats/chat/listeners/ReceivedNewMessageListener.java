package de.feli490.hytale.hyfechats.chat.listeners;

import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;

public interface ReceivedNewMessageListener {
    void onMessage(Chat chat, ChatMessage message);
}
