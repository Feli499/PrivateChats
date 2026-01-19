package de.feli490.hytale.hyfechats.data;

import de.feli490.hytale.hyfechats.chat.Chat;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class MemoryChatDataLoader implements ChatDataLoader {
    @Override
    public Collection<ChatData> loadChats() {
        return List.of();
    }

    @Override
    public void deleteChat(UUID chatId) {

    }

    @Override
    public void saveChat(Chat chat) {

    }

    @Override
    public void saveChats(Collection<Chat> chats) {

    }
}
