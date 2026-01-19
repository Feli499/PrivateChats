package de.feli490.hytale.privatechats.chat;

import de.feli490.hytale.privatechats.chat.listeners.MemberChangedListener;
import de.feli490.hytale.privatechats.chat.listeners.ReceivedNewMessageListener;
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

    private final Set<PlayerChatRole> playerChatRoles;
    private final Set<PlayerChatRole> unmodifiablePlayerChatRoles;

    private final List<ChatMessage> messages;
    private final List<ChatMessage> unmodifiableMessageList;

    private final Set<ReceivedNewMessageListener> messageListeners = new HashSet<>();
    private final Set<MemberChangedListener> memberChangedListeners = new HashSet<>();

    public Chat(UUID id, ChatType chatType, long created, PlayerDataProvider playerDataProvider) {
        this.id = id;
        this.chatType = chatType;
        this.created = created;
        this.playerDataProvider = playerDataProvider;

        playerChatRoles = new HashSet<>();
        messages = new ArrayList<>();
        unmodifiableMessageList = Collections.unmodifiableList(messages);
        unmodifiablePlayerChatRoles = Collections.unmodifiableSet(playerChatRoles);
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
        PlayerChatRole playerChatRole = new PlayerChatRole(playerId, role);
        playerChatRoles.add(playerChatRole);
        memberChangedListeners.forEach(listener -> listener.onMemberAdded(playerChatRole));
    }

    public void sendMessage(UUID senderId, String message) {

        ChatMessage chatMessage = new ChatMessage(UUID.randomUUID(), senderId, message, System.currentTimeMillis());
        messages.add(chatMessage);
        messageListeners.forEach(listener -> listener.onMessage(chatMessage));
    }

    public void removeChatter(UUID playerId) {

        PlayerChatRole playerChatRole = getPlayerChatRole(playerId);
        playerChatRoles.remove(playerChatRole);
        memberChangedListeners.forEach(listener -> listener.onMemberRemoved(playerChatRole));
    }

    private PlayerChatRole getPlayerChatRole(UUID playerId) {
        return playerChatRoles.stream()
                              .filter(playerChatRole -> playerChatRole.getPlayerId()
                                                                      .equals(playerId))
                              .findFirst()
                              .orElse(null);
    }

    public String getChatName(UUID chatNameFor) {

        if (chatType == ChatType.DIRECT) {
            Optional<PlayerChatRole> first = playerChatRoles.stream()
                                                            .filter(playerChatRole -> !playerChatRole.getPlayerId()
                                                                                                     .equals(chatNameFor))
                                                            .findFirst();
            PlayerChatRole playerChatRole = first.get();
            UUID playerId = playerChatRole.getPlayerId();
            String lastPlayerName = playerDataProvider.getLastPlayerName(playerId);
            if (lastPlayerName == null)
                return "Unknown (" + playerId + ")";
            return lastPlayerName;
        }

        StringBuilder stringBuilder = new StringBuilder(chatType.name());
        for (PlayerChatRole playerChatRole : playerChatRoles) {
            if (playerChatRole.getPlayerId()
                              .equals(chatNameFor))
                continue;

            stringBuilder.append(playerDataProvider.getLastPlayerName(playerChatRole.getPlayerId()))
                         .append(", ");
        }
        stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
        return stringBuilder.toString();
    }

    public ChatRole getRole(UUID playerId) {
        return playerChatRoles.stream()
                              .filter(playerChatRole -> playerChatRole.getPlayerId()
                                                                      .equals(playerId))
                              .findFirst()
                              .map(PlayerChatRole::getRole)
                              .orElse(null);
    }

    public Set<PlayerChatRole> getMembers() {
        return unmodifiablePlayerChatRoles;
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
        return playerChatRoles.stream()
                              .anyMatch(playerChatRole -> playerChatRole.getPlayerId()
                                                                        .equals(uuid));
    }

    public List<ChatMessage> getMessages() {
        return unmodifiableMessageList;
    }

    public UUID getId() {
        return id;
    }
}
