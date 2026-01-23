package de.feli490.hytale.hyfechats.chat;

import java.util.UUID;

public class PlayerChatProperties {

    private final UUID playerId;
    private final long memberSince;

    private ChatRole role;
    private long updated;

    public PlayerChatProperties(UUID playerId, ChatRole role, long memberSince, long updated) {
        this.playerId = playerId;
        this.role = role;
        this.memberSince = memberSince;
        this.updated = updated;
    }

    public PlayerChatProperties(UUID playerId, ChatRole role) {
        this(playerId, role, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public ChatRole getRole() {
        return role;
    }

    public void setRole(ChatRole role) {
        this.role = role;
        this.updated = System.currentTimeMillis();
    }

    public long getMemberSince() {
        return memberSince;
    }

    public long getUpdated() {
        return updated;
    }
}
