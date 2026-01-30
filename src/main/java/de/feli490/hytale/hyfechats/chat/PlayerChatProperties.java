package de.feli490.hytale.hyfechats.chat;

import de.feli490.hytale.hyfechats.chat.playerchatproperties.DisplayUnreadProperty;
import java.util.UUID;

public class PlayerChatProperties {

    private final Chat chat;

    private final UUID playerId;
    private final long memberSince;

    private ChatRole role;

    private long lastRead;
    private DisplayUnreadProperty displayUnreadProperty;

    public PlayerChatProperties(Chat chat, UUID playerId, long memberSince, ChatRole role, long lastRead,
            DisplayUnreadProperty displayUnreadProperty) {
        this.chat = chat;
        this.playerId = playerId;
        this.memberSince = memberSince;

        this.role = role;

        this.lastRead = lastRead;
        this.displayUnreadProperty = displayUnreadProperty;
    }

    public PlayerChatProperties(Chat chat, UUID playerId, ChatRole role) {
        this(chat, playerId, System.currentTimeMillis(), role, System.currentTimeMillis(), DisplayUnreadProperty.ALWAYS);
    }

    public Chat getChat() {
        return chat;
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

    @Override
    public String toString() {
        return "PlayerChatProperties{" + "chat=" + chat.getId() + ", playerId=" + playerId + ", memberSince=" + memberSince + ", role="
                + role + ", lastRead=" + lastRead + ", displayUnreadProperty=" + displayUnreadProperty + '}';
    }
}
