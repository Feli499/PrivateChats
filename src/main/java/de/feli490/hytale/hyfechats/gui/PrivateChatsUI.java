package de.feli490.hytale.hyfechats.gui;

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
import de.feli490.hytale.hyfechats.PrivateChatManager;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.listeners.ReceivedNewMessageListener;
import de.feli490.utils.hytale.message.MessageBuilderFactory;
import de.feli490.utils.hytale.playerdata.PlayerDataProvider;
import de.feli490.utils.hytale.utils.PlayerUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PrivateChatsUI extends InteractiveCustomUIPage<PrivateChatsUI.PrivateChatData> implements ReceivedNewMessageListener {

    private List<Chat> chats;
    private final MessageBuilderFactory messageBuilderFactory;
    private final PlayerDataProvider playerDataProvider;
    private final PrivateChatManager chatManager;
    private Chat currentChat;

    public PrivateChatsUI(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime,
            MessageBuilderFactory messageBuilderFactory, PrivateChatManager chatManager,
            PlayerDataProvider playerDataProvider) {
        super(playerRef, lifetime, PrivateChatData.CODEC);
        this.messageBuilderFactory = messageBuilderFactory;
        this.playerDataProvider = playerDataProvider;
        this.chatManager = chatManager;
        this.chats = Collections.emptyList();

        currentChat = null;
    }

    @Override
    public void onDismiss(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store) {
        if (currentChat != null)
            currentChat.removeNewMessageListener(this);
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder,
            @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {

        uiCommandBuilder.append("ChatsUi/PrivateChats.ui");

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                                       "#CreateChat",
                                       EventData.of(PrivateChatData.KEY_CREATE_CHAT_ACTION, Boolean.TRUE.toString()),
                                       false);

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
        uiCommandBuilder.append("#ChatView", "ChatsUi/ChatView.ui");

        String prefix = "#ChatView[0] ";
        uiCommandBuilder.set(prefix + "#ChatName.Text", currentChat.getChatName(playerRef.getUuid()));

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

        List<ChatMessage> messages = new ArrayList<>(currentChat.getMessages());

        messages.sort((message1, message2) -> Long.compare(message2.timestamp(), message1.timestamp()));

        for (int i = 0; i < messages.size(); i++) {
            ChatMessage chatMessage = messages.get(i);
            uiCommandBuilder.append(selector, "ChatsUi/ChatMessage.ui");

            UUID playerUUID = chatMessage.senderId();
            String lastPlayerName = playerDataProvider.getLastPlayerName(playerUUID);
            if (lastPlayerName == null)
                lastPlayerName = "Unknown (" + playerUUID + ")";

            uiCommandBuilder.set(selector + "[" + i + "] #DisplayName.Text", lastPlayerName + ": ");
            uiCommandBuilder.set(selector + "[" + i + "] #Message.Text", chatMessage.message());
            uiCommandBuilder.set(selector + "[" + i + "].TooltipTextSpans",
                                 messageBuilderFactory.timestamp(chatMessage.timestamp(), Color.WHITE));
        }

        sendUpdate(uiCommandBuilder, false);
    }

    private void createMessage(String message) {
        currentChat.sendMessage(playerRef.getUuid(), message);

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set("#ChatView[0] #NewMessage.Value", "");
        sendUpdate(uiCommandBuilder, false);
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
        else if (data.createChatButtonPressed != null) {
            CreateChatUI page = new CreateChatUI(playerRef,
                                                 lifetime,
                                                 chatManager,
                                                 playerDataProvider,
                                                 () -> new PrivateChatsUI(playerRef,
                                                                          lifetime,
                                                                          messageBuilderFactory,
                                                                          chatManager,
                                                                          playerDataProvider));
            PlayerUtils.getPlayer(playerRef)
                       .getPageManager()
                       .openCustomPage(ref, store, page);
            sendUpdate();
        }
    }

    private void updateChatList(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {

        for (Chat chat : chats) {
            chat.removeNewMessageListener(this);
        }

        chats = chatManager.getSortedChats(playerRef.getUuid());

        for (Chat chat : chats) {
            chat.addNewMessageListener(this);
        }

        uiCommandBuilder.clear("#ChatPreviewItem");

        for (int i = 0; i < chats.size(); i++) {
            Chat chat = chats.get(i);

            String previewText = "No messages yet.";

            ChatMessage lastMessage = chat.getLastMessage();
            if (lastMessage != null)
                previewText = lastMessage.message();

            uiCommandBuilder.append("#ChatPreviewItem", "ChatsUi/ChatListItem.ui");
            uiCommandBuilder.set("#ChatPreviewItem[" + i + "] #ChatName.Text", chat.getChatName(playerRef.getUuid()));
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

    @Override
    public void onMessage(Chat chat, ChatMessage message) {
        if (currentChat == chat)
            rewriteMessages();

        reloadChats();
    }

    public static class PrivateChatData {

        static final String KEY_CREATE_CHAT_ACTION = "CreateChatButtonAction";
        static final String KEY_DISPLAY_CHAT_BUTTON_ACTION = "DisplayChatButtonAction";
        static final String KEY_MESSAGE_TEXT = "@MessageText";

        public static final BuilderCodec<PrivateChatData> CODEC = BuilderCodec.builder(PrivateChatData.class, PrivateChatData::new)
                                                                              .addField(new KeyedCodec<>(KEY_DISPLAY_CHAT_BUTTON_ACTION,
                                                                                                         Codec.UUID_STRING),
                                                                                        PrivateChatData::setDisplayChat,
                                                                                        PrivateChatData::getDisplayChat)
                                                                              .addField(new KeyedCodec<>(KEY_CREATE_CHAT_ACTION,
                                                                                                         Codec.STRING),
                                                                                        PrivateChatData::setCreateChatButtonPressed,
                                                                                        PrivateChatData::getCreateChatButtonPressed)
                                                                              .addField(new KeyedCodec<>(KEY_MESSAGE_TEXT, Codec.STRING),
                                                                                        PrivateChatData::setWrittenMessage,
                                                                                        PrivateChatData::getWrittenMessage)
                                                                              .build();

        private UUID displayChat;
        private String createChatButtonPressed;
        private String writtenMessage;

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

        public String getCreateChatButtonPressed() {
            return createChatButtonPressed;
        }

        public void setCreateChatButtonPressed(String createChatButtonPressed) {
            this.createChatButtonPressed = createChatButtonPressed;
        }
    }
}
