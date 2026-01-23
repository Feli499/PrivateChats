package de.feli490.hytale.hyfechats.chat.listeners;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;
import de.feli490.utils.hytale.message.MessageBuilder;
import de.feli490.utils.hytale.message.MessageBuilderFactory;
import de.feli490.utils.hytale.playerdata.PlayerDataProvider;
import java.util.UUID;

public class SendMessagesToChatReceivedNewMessageListener implements ReceivedNewMessageListener {

    private final MessageBuilderFactory messageBuilderFactory;
    private final PlayerDataProvider playerDataProvider;

    public SendMessagesToChatReceivedNewMessageListener(MessageBuilderFactory messageBuilderFactory,
            PlayerDataProvider playerDataProvider) {
        this.messageBuilderFactory = messageBuilderFactory;
        this.playerDataProvider = playerDataProvider;
    }

    @Override
    public void onMessage(Chat chat, ChatMessage message) {

        UUID senderId = message.senderId();
        Universe universe = Universe.get();

        for (PlayerChatProperties member : chat.getMembers()) {

            UUID playerId = member.getPlayerId();
            PlayerRef player = universe.getPlayer(playerId);
            if (player == null)
                continue;

            MessageBuilder builder = messageBuilderFactory.builder();
            switch (chat.getChatType()) {
            case DIRECT:
                createDefaultPrefix(builder, chat, senderId, playerId);
                break;
            case GROUP:
                buildChatPrefixGroup(builder, chat, senderId, playerId);
                break;
            default:
                throw new IllegalArgumentException("Unknown chat type: " + chat.getChatType());
            }

            builder.second(message.message());
            player.sendMessage(builder.build());
        }
    }

    private void buildChatPrefixGroup(MessageBuilder builder, Chat chat, UUID senderId, UUID playerId) {
        createDefaultPrefix(builder, chat, senderId, playerId);
        if (!playerId.equals(senderId)) {
            builder.main(playerDataProvider.getLastPlayerName(senderId))
                   .main(": ");
        }
    }

    private void createDefaultPrefix(MessageBuilder builder, Chat chat, UUID senderId, UUID playerId) {

        String chatName = chat.getChatName(playerId);
        if (chatName.length() > 10) {
            chatName = chatName.substring(0, 10) + "...";
        }

        builder.main(playerId.equals(senderId) ? "[To " : "[From ")
               .main(chatName)
               .main("] ");
    }
}
