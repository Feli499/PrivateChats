package de.feli490.hytale.hyfechats.chat.listeners;

import de.feli490.hytale.hyfechats.chat.Chat;
import java.util.UUID;

public interface PlayerOpensChatListener {
    void onChatOpened(Chat chat, UUID playerId);
}
