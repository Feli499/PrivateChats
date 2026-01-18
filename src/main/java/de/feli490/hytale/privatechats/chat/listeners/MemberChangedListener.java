package de.feli490.hytale.privatechats.chat.listeners;

import de.feli490.hytale.privatechats.chat.ChatRole;
import de.feli490.hytale.privatechats.chat.PlayerChatRole;

public interface MemberChangedListener {

    default void onMemberAdded(PlayerChatRole member) {}

    default void onMemberRoleChanged(PlayerChatRole newRole, ChatRole oldRole) {}

    default void onMemberRemoved(PlayerChatRole member) {}

}
