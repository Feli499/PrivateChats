package de.feli490.hytale.privatechats.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Chat {

    private final UUID id;
    private final ChatType chatType;
    private final long created;

    private final Set<PlayerChatRole> playerChatRoles;
    private final Set<PlayerChatRole> unmodifiablePlayerChatRoles;

    private final List<ChatMessage> messages;
    private final List<ChatMessage> unmodifiableMessageList;

    private Chat(UUID id, ChatType chatType, long created) {
        this.id = id;
        this.chatType = chatType;
        this.created = created;

        playerChatRoles = new HashSet<>();
        messages = new ArrayList<>();
        unmodifiableMessageList = Collections.unmodifiableList(messages);
        unmodifiablePlayerChatRoles = Collections.unmodifiableSet(playerChatRoles);
    }

    public void addChatter(UUID playerId, ChatRole role) {
        playerChatRoles.add(new PlayerChatRole(playerId, role));
    }

    public void sendMessage(UUID senderId, String message) {
        messages.add(new ChatMessage(UUID.randomUUID(), senderId, message, System.currentTimeMillis()));
    }

    public void removeChatter(UUID playerId) {
        playerChatRoles.removeIf(playerChatRole -> playerChatRole.getPlayerId()
                                                                 .equals(playerId));
    }

    public String getChatName() {
        return chatType.name() + ": " + playerChatRoles.size();
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

    public static Chat createDirect(UUID player1, UUID player2) {
        Chat chat = createChat(ChatType.DIRECT);
        chat.addChatter(player1, ChatRole.OWNER);
        chat.addChatter(player2, ChatRole.OWNER);
        return chat;
    }

    public static Chat createGroup(UUID player1) {
        Chat chat = createChat(ChatType.GROUP);
        chat.addChatter(player1, ChatRole.OWNER);
        return chat;
    }

    private static Chat createChat(ChatType chatType) {
        return new Chat(UUID.randomUUID(), chatType, System.currentTimeMillis());
    }
}
