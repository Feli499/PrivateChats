package de.feli490.hytale.hyfechats.data;

import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;
import java.io.IOException;

public interface ChatDataSaver {

    void saveChatMetaData(Chat chat) throws IOException;

    void deleteChat(Chat chat) throws IOException;

    void saveMessage(ChatMessage message) throws IOException;

    void savePlayerChatProperties(PlayerChatProperties playerChatProperties) throws IOException;

    void deletePlayerChatProperties(PlayerChatProperties playerChatProperties) throws IOException;
}
