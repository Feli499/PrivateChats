package de.feli490.hytale.hyfechats.data;

import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class MemoryChatDataLoader implements ChatDataLoader, ChatDataSaver {
    @Override
    public Collection<ChatData> loadChats() {
        return List.of();
    }

    @Override
    public void deleteChat(Chat chat) throws IOException {

    }

    @Override
    public void saveMessage(ChatMessage message) throws IOException {

    }

    @Override
    public void savePlayerChatProperties(PlayerChatProperties playerChatProperties) throws IOException {

    }

    @Override
    public void saveChatMetaData(Chat chat) throws IOException {

    }

    @Override
    public void deletePlayerChatProperties(PlayerChatProperties playerChatProperties) throws IOException {

    }
}
