package de.feli490.hytale.hyfechats.chat.listeners;

import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatRole;
import de.feli490.hytale.hyfechats.chat.PlayerChatRole;

public interface MemberChangedListener {

    default void onMemberAdded(Chat chat, PlayerChatRole member) {}

    default void onMemberRoleChanged(Chat chat, PlayerChatRole newRole, ChatRole oldRole) {}

    default void onMemberRemoved(Chat chat, PlayerChatRole member) {}

}
