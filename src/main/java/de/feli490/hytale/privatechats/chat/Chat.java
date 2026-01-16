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

    private final Set<PlayerChatRole> playerChatRoles;
    private final List<ChatMessage> messages;
    private final List<ChatMessage> unmodifiableMessageList;

    private Chat(UUID id, ChatType chatType) {
        this.id = id;
        this.chatType = chatType;

        playerChatRoles = new HashSet<>();
        messages = new ArrayList<>();
        unmodifiableMessageList = Collections.unmodifiableList(messages);
    }

    public void addChatter(UUID playerId, ChatRole role) {
        playerChatRoles.add(new PlayerChatRole(playerId, role));
    }

    public void sendMessage(UUID senderId, String message) {
        messages.add(new ChatMessage(UUID.randomUUID(), senderId, message));
    }

    public void removeChatter(UUID playerId) {
        playerChatRoles.removeIf(playerChatRole -> playerChatRole.getPlayerId()
                                                                 .equals(playerId));
    }

    public List<ChatMessage> getMessages() {
        return unmodifiableMessageList;
    }

    public static Chat createDirect(UUID player1, UUID player2) {
        Chat chat = new Chat(UUID.randomUUID(), ChatType.DIRECT);
        chat.addChatter(player1, ChatRole.OWNER);
        chat.addChatter(player2, ChatRole.OWNER);
        return chat;
    }

    public static Chat createGroup(UUID player1) {
        Chat chat = new Chat(UUID.randomUUID(), ChatType.GROUP);
        chat.addChatter(player1, ChatRole.OWNER);
        return chat;
    }
}
