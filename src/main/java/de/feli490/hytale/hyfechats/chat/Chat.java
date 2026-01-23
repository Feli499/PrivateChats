package de.feli490.hytale.hyfechats.chat;

import de.feli490.hytale.hyfechats.chat.listeners.MemberChangedListener;
import de.feli490.hytale.hyfechats.chat.listeners.ReceivedNewMessageListener;
import de.feli490.hytale.hyfechats.data.ChatData;
import de.feli490.utils.hytale.playerdata.PlayerDataProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class Chat {

    private final UUID id;
    private final ChatType chatType;
    private final long created;
    private final PlayerDataProvider playerDataProvider;

    private final Set<PlayerChatProperties> playerChatProperties;
    private final Set<PlayerChatProperties> unmodifiablePlayerChatProperties;

    private final List<ChatMessage> messages;
    private final List<ChatMessage> unmodifiableMessageList;

    private final Set<ReceivedNewMessageListener> messageListeners = new HashSet<>();
    private final Set<MemberChangedListener> memberChangedListeners = new HashSet<>();

    public Chat(UUID id, ChatType chatType, long created, PlayerDataProvider playerDataProvider) {
        this.id = id;
        this.chatType = chatType;
        this.created = created;
        this.playerDataProvider = playerDataProvider;

        playerChatProperties = new HashSet<>();
        messages = new ArrayList<>();
        unmodifiableMessageList = Collections.unmodifiableList(messages);
        unmodifiablePlayerChatProperties = Collections.unmodifiableSet(playerChatProperties);
    }

    public void addNewMessageListener(ReceivedNewMessageListener listener) {
        messageListeners.add(listener);
    }

    public boolean isNewMessageListenerRegistered(ReceivedNewMessageListener listener) {
        return messageListeners.contains(listener);
    }

    public void removeNewMessageListener(ReceivedNewMessageListener listener) {
        messageListeners.remove(listener);
    }

    public void addMemberChangedListener(MemberChangedListener listener) {
        memberChangedListeners.add(listener);
    }

    public boolean isMemberChangedListenerRegistered(MemberChangedListener listener) {
        return memberChangedListeners.contains(listener);
    }

    public void removeMemberChangedListener(MemberChangedListener listener) {
        memberChangedListeners.add(listener);
    }

    public void addChatter(UUID playerId, ChatRole role) {
        PlayerChatProperties playerChatProperties = new PlayerChatProperties(playerId, role);
        this.playerChatProperties.add(playerChatProperties);
        memberChangedListeners.forEach(listener -> listener.onMemberAdded(this, playerChatProperties));
    }

    public void sendMessage(UUID senderId, String message) {

        ChatMessage chatMessage = new ChatMessage(UUID.randomUUID(), senderId, message, System.currentTimeMillis());
        messages.add(chatMessage);
        List.copyOf(messageListeners)
            .forEach(listener -> listener.onMessage(this, chatMessage));
    }

    public void removeChatter(UUID playerId) {

        PlayerChatProperties playerChatProperties = getPlayerChatRole(playerId);
        this.playerChatProperties.remove(playerChatProperties);
        memberChangedListeners.forEach(listener -> listener.onMemberRemoved(this, playerChatProperties));
    }

    private PlayerChatProperties getPlayerChatRole(UUID playerId) {
        return playerChatProperties.stream()
                                   .filter(playerChatRole -> playerChatRole.getPlayerId()
                                                                      .equals(playerId))
                                   .findFirst()
                                   .orElse(null);
    }

    public String getChatName(UUID chatNameFor) {

        if (chatType == ChatType.DIRECT) {
            Optional<PlayerChatProperties> first = this.playerChatProperties.stream()
                                                                            .filter(playerChatRole -> !playerChatRole.getPlayerId()
                                                                                                     .equals(chatNameFor))
                                                                            .findFirst();
            PlayerChatProperties playerChatProperties = first.get();
            UUID playerId = playerChatProperties.getPlayerId();
            String lastPlayerName = playerDataProvider.getLastPlayerName(playerId);
            if (lastPlayerName == null)
                return "Unknown (" + playerId + ")";
            return lastPlayerName;
        }

        StringBuilder stringBuilder = new StringBuilder(chatType.name()
                                                                .charAt(0) + ": ");
        for (PlayerChatProperties playerChatProperties : this.playerChatProperties) {
            if (playerChatProperties.getPlayerId()
                                    .equals(chatNameFor))
                continue;

            stringBuilder.append(playerDataProvider.getLastPlayerName(playerChatProperties.getPlayerId()))
                         .append(", ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        return stringBuilder.toString();
    }

    public ChatRole getRole(UUID playerId) {
        return playerChatProperties.stream()
                                   .filter(playerChatRole -> playerChatRole.getPlayerId()
                                                                      .equals(playerId))
                                   .findFirst()
                                   .map(PlayerChatProperties::getRole)
                                   .orElse(null);
    }

    public Set<PlayerChatProperties> getMembers() {
        return unmodifiablePlayerChatProperties;
    }

    public long getCreated() {
        return created;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public long getLastUpdate() {

        ChatMessage lastMessage = getLastMessage();
        if (lastMessage != null)
            return lastMessage.timestamp();

        return created;
    }

    public ChatMessage getLastMessage() {
        if (messages.isEmpty())
            return null;
        return messages.getLast();
    }

    public boolean isMember(UUID uuid) {
        return playerChatProperties.stream()
                                   .anyMatch(playerChatRole -> playerChatRole.getPlayerId()
                                                                        .equals(uuid));
    }

    public List<ChatMessage> getMessages() {
        return unmodifiableMessageList;
    }

    public UUID getId() {
        return id;
    }

    public static Chat fromChatData(ChatData chatData, PlayerDataProvider playerDataProvider) {

        Chat chat = new Chat(chatData.getId(), chatData.getChatType(), chatData.getCreated(), playerDataProvider);
        chat.messages.addAll(chatData.getMessages());
        chat.playerChatProperties.addAll(chatData.getPlayerChatProperties());
        return chat;
    }
}
