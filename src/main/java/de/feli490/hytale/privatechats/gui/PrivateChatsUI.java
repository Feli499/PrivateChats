package de.feli490.hytale.privatechats.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.feli490.hytale.privatechats.PrivateChatManager;
import de.feli490.hytale.privatechats.chat.Chat;
import de.feli490.hytale.privatechats.chat.ChatMessage;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PrivateChatsUI extends InteractiveCustomUIPage<PrivateChatsUI.PrivateChatData> {

    private final List<Chat> chats;
    private Chat currentChat;

    public PrivateChatsUI(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime, PrivateChatManager chatManager) {
        super(playerRef, lifetime, PrivateChatData.CODEC);

        chats = chatManager.getSortedChats(playerRef.getUuid());
        currentChat = null;
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder,
            @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {

        uiCommandBuilder.append("PrivateChats.ui");
        updateChatList(uiCommandBuilder, uiEventBuilder);
    }

    private Chat getChat(UUID chatId) {
        return chats.stream()
                    .filter(chat -> chat.getId()
                                        .equals(chatId))
                    .findFirst()
                    .orElse(null);
    }

    public void setSelectedChat(Chat chat) {
        currentChat = chat;

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set("#ChatLabel.Text", chat.getChatName() + " (" + chat.getId() + ")");
        sendUpdate(uiCommandBuilder, false);
    }

    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store,
            @NonNullDecl PrivateChatData data) {
        super.handleDataEvent(ref, store, data);

        if (data.displayChat != null//
                && (Objects.isNull(currentChat) || !Objects.equals(currentChat.getId(), data.displayChat)))
            setSelectedChat(getChat(data.displayChat));
    }

    private void updateChatList(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {
        uiCommandBuilder.clear("#ChatPreviewItem");

        for (int i = 0; i < chats.size(); i++) {
            Chat chat = chats.get(i);

            String previewText = "No messages yet.";

            ChatMessage lastMessage = chat.getLastMessage();
            if (lastMessage != null)
                previewText = lastMessage.message();

            uiCommandBuilder.append("#ChatPreviewItem", "ChatListItem.ui");
            uiCommandBuilder.set("#ChatPreviewItem[" + i + "] #ChatName.Text", chat.getChatName());
            uiCommandBuilder.set("#ChatPreviewItem[" + i + "] #MessagePreview.Text", previewText);
            uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                                           "#ChatPreviewItem[" + i + "] #OpenChatButton",
                                           EventData.of("DisplayChatButtonAction",
                                                        chat.getId()
                                                            .toString()),
                                           false);
        }
    }

    private void reloadChats() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        updateChatList(uiCommandBuilder, uiEventBuilder);

        sendUpdate(uiCommandBuilder, uiEventBuilder, false);
    }

    public static class PrivateChatData {

        static final String KEY_DISPLAY_CHAT_BUTTON_ACTION = "DisplayChatButtonAction";

        public static final BuilderCodec<PrivateChatData> CODEC = BuilderCodec.builder(PrivateChatData.class, PrivateChatData::new)
                                                                              .addField(new KeyedCodec<>(KEY_DISPLAY_CHAT_BUTTON_ACTION,
                                                                                                         Codec.UUID_STRING),
                                                                                        PrivateChatData::setDisplayChat,
                                                                                        PrivateChatData::getDisplayChat)
                                                                              .build();

        private UUID displayChat;

        public PrivateChatData() {}

        public UUID getDisplayChat() {
            return displayChat;
        }

        public void setDisplayChat(UUID displayChat) {
            this.displayChat = displayChat;
        }
    }
}
