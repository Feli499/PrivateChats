package de.feli490.hytale.privatechats.chat.listeners;

import de.feli490.hytale.privatechats.chat.ChatMessage;

public interface ReceivedNewMessageListener {
    void onMessage(ChatMessage message);
}
