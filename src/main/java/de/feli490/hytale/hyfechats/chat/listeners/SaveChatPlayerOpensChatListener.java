package de.feli490.hytale.hyfechats.chat.listeners;

import com.hypixel.hytale.logger.HytaleLogger;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.data.ChatDataLoader;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class SaveChatPlayerOpensChatListener implements PlayerOpensChatListener {

    private final HytaleLogger logger;
    private final ChatDataLoader chatDataLoader;

    public SaveChatPlayerOpensChatListener(HytaleLogger logger, ChatDataLoader chatDataLoader) {
        this.logger = logger.getSubLogger("SaveChatPlayerOpensChatListener");
        this.chatDataLoader = chatDataLoader;
    }

    @Override
    public void onChatOpened(Chat chat, UUID playerId) {
        try {
            chatDataLoader.saveChat(chat);
        } catch (IOException e) {
            logger.at(Level.SEVERE)
                  .withCause(e)
                  .log("Could not save chat: " + chat.getId());
        }
    }
}
