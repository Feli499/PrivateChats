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
import de.feli490.hytale.privatechats.utils.MessageUtils;
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
        uiCommandBuilder.clear("#ChatView");
        uiCommandBuilder.append("#ChatView", "ChatView.ui");

        String prefix = "#ChatView[0] ";
        uiCommandBuilder.set(prefix + "#ChatName.Text", currentChat.getChatName());

        UIEventBuilder uiEventBuilder = new UIEventBuilder();
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                                       prefix + "#SendMessageButton",
                                       EventData.of(PrivateChatData.KEY_MESSAGE_TEXT, prefix + "#NewMessage.Value"),
                                       false);

        sendUpdate(uiCommandBuilder, uiEventBuilder, false);

        rewriteMessages();
    }

    public void rewriteMessages() {

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();

        String selector = "#ChatView[0] #MessageItem";
        uiCommandBuilder.clear(selector);

        List<ChatMessage> messages = currentChat.getMessages();

        for (int i = 0; i < messages.size(); i++) {
            ChatMessage chatMessage = messages.get(i);
            uiCommandBuilder.append(selector, "ChatMessage.ui");

            uiCommandBuilder.set(selector + "[" + i + "] #DisplayName.Text", chatMessage.id() + ": ");
            uiCommandBuilder.set(selector + "[" + i + "] #Message.Text", chatMessage.message());
            uiCommandBuilder.set(selector + "[" + i + "].TooltipTextSpans", MessageUtils.formatTimestamp(chatMessage.timestamp()));
        }

        sendUpdate(uiCommandBuilder, false);
    }

    private void createMessage(String message) {
        currentChat.sendMessage(playerRef.getUuid(), message);
        rewriteMessages();
    }

    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store,
            @NonNullDecl PrivateChatData data) {
        super.handleDataEvent(ref, store, data);

        if (data.writtenMessage != null) {
            createMessage(data.writtenMessage);
        } else if (data.displayChat != null//
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
                                           EventData.of(PrivateChatData.KEY_DISPLAY_CHAT_BUTTON_ACTION,
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
        static final String KEY_MESSAGE_TEXT = "@MessageText";

        public static final BuilderCodec<PrivateChatData> CODEC = BuilderCodec.builder(PrivateChatData.class, PrivateChatData::new)
                                                                              .addField(new KeyedCodec<>(KEY_DISPLAY_CHAT_BUTTON_ACTION,
                                                                                                         Codec.UUID_STRING),
                                                                                        PrivateChatData::setDisplayChat,
                                                                                        PrivateChatData::getDisplayChat)
                                                                              .addField(new KeyedCodec<>(KEY_MESSAGE_TEXT, Codec.STRING),
                                                                                        PrivateChatData::setWrittenMessage,
                                                                                        PrivateChatData::getWrittenMessage)
                                                                              .build();

        private UUID displayChat;
        private String writtenMessage;
        private String sendMessage;

        public PrivateChatData() {}

        public UUID getDisplayChat() {
            return displayChat;
        }

        public void setDisplayChat(UUID displayChat) {
            this.displayChat = displayChat;
        }

        public String getWrittenMessage() {
            return writtenMessage;
        }

        public void setWrittenMessage(String writtenMessage) {
            this.writtenMessage = writtenMessage;
        }
    }
}
