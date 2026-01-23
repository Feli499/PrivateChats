package de.feli490.hytale.hyfechats;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.PlayerChatRole;
import de.feli490.hytale.hyfechats.chat.listeners.ReceivedNewMessageListener;
import de.feli490.utils.hytale.message.MessageBuilder;
import de.feli490.utils.hytale.message.MessageBuilderFactory;
import java.util.UUID;

public class SendMessagesToChatReceivedNewMessageListener implements ReceivedNewMessageListener {

    private final MessageBuilderFactory messageBuilderFactory;

    public SendMessagesToChatReceivedNewMessageListener(MessageBuilderFactory messageBuilderFactory) {
        this.messageBuilderFactory = messageBuilderFactory;
    }

    @Override
    public void onMessage(Chat chat, ChatMessage message) {

        UUID senderId = message.senderId();
        switch (chat.getChatType()) {
            case DIRECT:
                sendMessageForDirectChat(chat, message);
                break;
            case GROUP:
                sendMessageForGroupChat(chat, message);
                break;
            default:
                throw new IllegalArgumentException("Unknown chat type: " + chat.getChatType());
        }
    }

    private void sendMessageForDirectChat(Chat chat, ChatMessage chatMessage) {

        UUID senderId = chatMessage.senderId();
        for (PlayerChatRole member : chat.getMembers()) {
            UUID playerId = member.getPlayerId();

            PlayerRef player = Universe.get()
                                       .getPlayer(playerId);
            if (player == null)
                continue;

            MessageBuilder builder = messageBuilderFactory.builder()
                                                          .main(playerId.equals(senderId) ? "[To " : "[From ")
                                                          .main(chat.getChatName(playerId))
                                                          .main("] ")
                                                          .second(chatMessage.message());
            player.sendMessage(builder.build());
        }
    }

    private void sendMessageForGroupChat(Chat chat, ChatMessage chatMessage) {

        UUID senderId = chatMessage.senderId();

        for (PlayerChatRole member : chat.getMembers()) {
            UUID playerId = member.getPlayerId();

            PlayerRef player = Universe.get()
                                       .getPlayer(playerId);
            if (player == null)
                continue;

            String chatName = chat.getChatName(playerId);
            if (chatName.length() > 10) {
                chatName = chatName.substring(0, 10) + "...";
            }

            MessageBuilder builder = messageBuilderFactory.builder()
                                                          .main(playerId.equals(senderId) ? "[To " : "[From ")
                                                          .main(chatName)
                                                          .main("] ")
                                                          .second(chatMessage.message());
            player.sendMessage(builder.build());
        }
    }
}
