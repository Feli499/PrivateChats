package de.feli490.hytale.hyfechats;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.PlayerChatRole;
import de.feli490.hytale.hyfechats.chat.listeners.ReceivedNewMessageListener;
import de.feli490.utils.hytale.playerdata.PlayerDataProvider;
import java.awt.Color;
import java.util.UUID;

public class SendMessagesToChatReceivedNewMessageListener implements ReceivedNewMessageListener {

    private final PlayerDataProvider playerDataProvider;

    public SendMessagesToChatReceivedNewMessageListener(
            PlayerDataProvider playerDataProvider) {this.playerDataProvider = playerDataProvider;}

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
            String prefixString = (playerId.equals(senderId) ? "[To " : "[From ") + chat.getChatName(playerId) + "] ";
            Message prefix = Message.raw(prefixString)
                                    .color(Color.GREEN);
            Message join = Message.join(prefix,
                                        Message.raw(chatMessage.message())
                                               .color(Color.WHITE));
            sendMessage(playerId, join);
        }
    }

    private void sendMessageForGroupChat(Chat chat, ChatMessage chatMessage) {

        UUID senderId = chatMessage.senderId();

        for (PlayerChatRole member : chat.getMembers()) {

            UUID playerId = member.getPlayerId();
            String chatName = chat.getChatName(playerId);
            if (chatName.length() > 10) {
                chatName = chatName.substring(0, 10) + "...";
            }

            String prefixString = (playerId.equals(senderId) ? "[To " : "[From ") + chatName + "] ";
            Message prefix = Message.raw(prefixString)
                                    .color(Color.GREEN);
            Message join = Message.join(prefix,
                                        Message.raw(chatMessage.message())
                                               .color(Color.WHITE));
            sendMessage(playerId, join);
        }
    }

    private void sendMessage(UUID receiverId, Message message) {
        PlayerRef player = Universe.get()
                                   .getPlayer(receiverId);

        if (player == null)
            return;

        player.sendMessage(message);
    }
}
