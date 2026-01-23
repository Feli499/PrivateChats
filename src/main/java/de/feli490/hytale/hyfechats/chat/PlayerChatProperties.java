package de.feli490.hytale.hyfechats.chat;

import de.feli490.hytale.hyfechats.chat.playerchatproperties.DisplayUnreadProperty;
import java.util.UUID;

public class PlayerChatProperties {

    private final UUID playerId;
    private final long memberSince;

    private ChatRole role;

    private long lastRead;
    private DisplayUnreadProperty displayUnreadProperty;

    public PlayerChatProperties(UUID playerId, long memberSince, ChatRole role, long lastRead,
            DisplayUnreadProperty displayUnreadProperty) {
        this.playerId = playerId;
        this.memberSince = memberSince;

        this.role = role;

        this.lastRead = lastRead;
        this.displayUnreadProperty = displayUnreadProperty;
    }

    public PlayerChatProperties(UUID playerId, ChatRole role) {
        this(playerId, System.currentTimeMillis(), role, System.currentTimeMillis(), DisplayUnreadProperty.ALWAYS);
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

    public long getMemberSince() {
        return memberSince;
    }

    public DisplayUnreadProperty getDisplayUnread() {
        return displayUnreadProperty;
    }

    public void setDisplayUnread(DisplayUnreadProperty displayUnreadProperty) {
        this.displayUnreadProperty = displayUnreadProperty;
    }

    public long getLastRead() {
        return lastRead;
    }

    public void setLastRead(long lastRead) {
        this.lastRead = lastRead;
    }
}
