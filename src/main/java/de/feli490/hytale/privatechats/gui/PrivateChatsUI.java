package de.feli490.hytale.privatechats.gui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.feli490.hytale.privatechats.PrivateChatManager;
import de.feli490.hytale.privatechats.chat.Chat;
import de.feli490.hytale.privatechats.chat.ChatMessage;
import java.util.List;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PrivateChatsUI extends CustomUIPage {

    private final PrivateChatManager chatManager;

    public PrivateChatsUI(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime, PrivateChatManager chatManager) {
        super(playerRef, lifetime);

        this.chatManager = chatManager;
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder,
            @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {

        uiCommandBuilder.append("PrivateChats.ui");

        uiCommandBuilder.clear("#ChatPreviewItem");

        uiCommandBuilder.appendInline("#ChatPreviewItem", "Group { LayoutMode: Top; }");
        List<Chat> sortedChats = chatManager.getSortedChats(playerRef.getUuid());
        for (int i = 0; i < sortedChats.size(); i++) {
            Chat chat = sortedChats.get(i);

            String previewText = "No messages yet.";

            ChatMessage lastMessage = chat.getLastMessage();
            if (lastMessage != null)
                previewText = lastMessage.message();

            uiCommandBuilder.append("#ChatPreviewItem", "ChatListItem.ui");
            //            uiCommandBuilder.set("#ChatPreviewItem[" + i + "] #ChatName.Text", chat.getChatName());
            //            uiCommandBuilder.set("#ChatPreviewItem[" + i + "] #MessagePreview.Text", previewText);
        }

        sendUpdate(uiCommandBuilder, false);
    }
}
