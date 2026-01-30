package de.feli490.hytale.hyfechats.chat.listeners;

import com.hypixel.hytale.logger.HytaleLogger;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;
import de.feli490.hytale.hyfechats.data.ChatDataSaver;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class SaveChatPlayerOpensChatListener implements PlayerOpensChatListener {

    private final HytaleLogger logger;
    private final ChatDataSaver chatDataSaver;

    public SaveChatPlayerOpensChatListener(HytaleLogger logger, ChatDataSaver chatDataSaver) {
        this.logger = logger.getSubLogger("SaveChatPlayerOpensChatListener");
        this.chatDataSaver = chatDataSaver;
    }

    @Override
    public void onChatOpened(Chat chat, UUID playerId) {
        try {
            PlayerChatProperties playerChatProperties = chat.getPlayerChatProperties(playerId);
            chatDataSaver.savePlayerChatProperties(playerChatProperties);
        } catch (IOException e) {
            logger.at(Level.SEVERE)
                  .withCause(e)
                  .log("Could not save chat: " + chat.getId());
        }
    }
}
