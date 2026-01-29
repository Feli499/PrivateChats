package de.feli490.hytale.hyfechats.data;

import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

public interface ChatDataLoader {
    Collection<ChatData> loadChats() throws IOException;

    void deleteChat(UUID chatId) throws IOException;

    void saveChat(Chat chat) throws IOException;

    void saveMessage(ChatMessage message) throws IOException;

    void savePlayerChatProperties(ChatMessage message) throws IOException;

    void saveChats(Collection<Chat> chats) throws IOException;
}
