package de.feli490.hytale.privatechats.chat;

import java.util.UUID;

public class PlayerChatRole {

    private final UUID playerId;
    private ChatRole role;

    public PlayerChatRole(UUID playerId, ChatRole role) {
        this.playerId = playerId;
        this.role = role;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public ChatRole getRole() {
        return role;
    }

    public void setRole(ChatRole role) {
        this.role = role;
    }
}
