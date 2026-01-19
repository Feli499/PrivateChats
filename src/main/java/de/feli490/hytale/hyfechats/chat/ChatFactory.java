package de.feli490.hytale.hyfechats.chat;

import de.feli490.hytale.hyfechats.chat.listeners.MemberChangedListener;
import de.feli490.hytale.hyfechats.chat.listeners.ReceivedNewMessageListener;
import de.feli490.hytale.hyfechats.data.ChatData;
import de.feli490.utils.hytale.playerdata.PlayerDataProvider;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatFactory {

    private final PlayerDataProvider playerDataProvider;

    private final Set<ReceivedNewMessageListener> messageListeners = new HashSet<>();
    private final Set<MemberChangedListener> memberListeners = new HashSet<>();

    public ChatFactory(PlayerDataProvider playerDataProvider) {
        this.playerDataProvider = playerDataProvider;
    }

    public void addMessageListenerForCreation(ReceivedNewMessageListener listener) {
        messageListeners.add(listener);
    }

    public void addMemberChangedListenerForCreation(MemberChangedListener listener) {
        memberListeners.add(listener);
    }

    public Chat createDirect(UUID player1, UUID player2) {
        Chat chat = createChat(ChatType.DIRECT);
        chat.addChatter(player1, ChatRole.OWNER);
        chat.addChatter(player2, ChatRole.OWNER);
        return chat;
    }

    public Chat createGroup(UUID player1) {
        Chat chat = createChat(ChatType.GROUP);
        chat.addChatter(player1, ChatRole.OWNER);
        return chat;
    }

    private Chat createChat(ChatType chatType) {
        Chat chat = new Chat(UUID.randomUUID(), chatType, System.currentTimeMillis(), playerDataProvider);
        registerListeners(chat);
        return chat;
    }

    private void registerListeners(Chat chat) {
        memberListeners.forEach(chat::addMemberChangedListener);
        messageListeners.forEach(chat::addNewMessageListener);
    }

    public Chat fromChatData(ChatData loadChat) {
        Chat chat = Chat.fromChatData(loadChat, playerDataProvider);
        registerListeners(chat);
        return chat;
    }
}
