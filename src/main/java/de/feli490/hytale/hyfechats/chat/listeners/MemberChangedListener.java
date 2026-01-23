package de.feli490.hytale.hyfechats.chat.listeners;

import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatRole;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;

public interface MemberChangedListener {

    default void onMemberAdded(Chat chat, PlayerChatProperties member) {}

    default void onMemberRoleChanged(Chat chat, PlayerChatProperties newRole, ChatRole oldRole) {}

    default void onMemberRemoved(Chat chat, PlayerChatProperties member) {}

}
