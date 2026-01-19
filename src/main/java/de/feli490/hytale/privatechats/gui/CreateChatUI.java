package de.feli490.hytale.privatechats.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.feli490.hytale.privatechats.PrivateChatManager;
import de.feli490.utils.hytale.playerdata.PlayerDataProvider;
import de.feli490.utils.hytale.utils.PlayerUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class CreateChatUI extends InteractiveCustomUIPage<CreateChatUI.CreateChatData> {

    private final PrivateChatManager chatManager;
    private final PlayerDataProvider playerDataProvider;
    private final Supplier<CustomUIPage> returnPageSupplier;

    private final List<UUID> selectedPlayerUUIDs;
    private String playerSearchQuery = "";

    public CreateChatUI(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime, PrivateChatManager chatManager,
            PlayerDataProvider playerDataProvider, Supplier<CustomUIPage> returnPageSupplier) {
        super(playerRef, lifetime, CreateChatData.CODEC);
        this.chatManager = chatManager;
        this.playerDataProvider = playerDataProvider;
        this.returnPageSupplier = returnPageSupplier;

        selectedPlayerUUIDs = new ArrayList<>();
        selectedPlayerUUIDs.add(playerRef.getUuid());
    }

    public CreateChatUI(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime, PrivateChatManager chatManager,
            PlayerDataProvider playerDataProvider) {
        this(playerRef, lifetime, chatManager, playerDataProvider, null);
    }

    @Override
    public void onDismiss(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store) {
        if (returnPageSupplier != null) {
            Player player = PlayerUtils.getPlayer(playerRef);
            CompletableFuture.delayedExecutor(1, TimeUnit.MILLISECONDS, player.getWorld())
                             .execute(() -> player.getPageManager()
                                                  .openCustomPage(ref, store, returnPageSupplier.get()));
        }
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder,
            @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {

        uiCommandBuilder.append("CreateChat/CreateChat.ui");
        updatePlayerList(uiCommandBuilder, uiEventBuilder);
    }

    private void updatePlayerList(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {

        uiCommandBuilder.clear("#PlayerItems");

        int column = 0;
        int row = 0;
        for (UUID currentUUID : playerDataProvider.getAllKnownPlayerUUIDs()) {
            if (currentUUID.equals(playerRef.getUuid()))
                continue;

            if (column == 0) {
                uiCommandBuilder.appendInline("#PlayerItems", "Group { LayoutMode: Left; Anchor: (Bottom: 0); }");
            }

            uiCommandBuilder.append("#PlayerItems[" + row + "]", "CreateChat/PlayerListItem.ui");
            uiCommandBuilder.set("#PlayerItems[" + row + "][" + column + "] #IsSelected.Value", selectedPlayerUUIDs.contains(currentUUID));
            uiCommandBuilder.set("#PlayerItems[" + row + "][" + column + "] #PlayerName.Text",
                                 playerDataProvider.getLastPlayerName(currentUUID));

            ++column;
            if (column >= 3) {
                column = 0;
                ++row;
            }
        }
    }

    public void updatePlayerList() {
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        updatePlayerList(uiCommandBuilder, uiEventBuilder);

        sendUpdate(uiCommandBuilder, uiEventBuilder, false);
    }

    @Override
    public void handleDataEvent(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CreateChatData data) {
        super.handleDataEvent(ref, store, data);

        if (data.getPlayerSearchQuery() != null) {
            playerSearchQuery = data.getPlayerSearchQuery();
        }
    }

    public static class CreateChatData {

        static final String KEY_DISPLAY_CHAT_BUTTON_ACTION = "CreateChatButton";
        static final String KEY_PLAYER_EDIT_UUID = "PlayerEditUUID";
        static final String KEY_PLAYER_EDIT_UUID_ACTION = "PlayerEditUUIDAction";
        static final String KEY_SEARCH_PLAYER_TEXT = "@SearchPlayerName";

        public static final BuilderCodec<CreateChatData> CODEC = BuilderCodec.builder(CreateChatData.class, CreateChatData::new)
                                                                             .addField(new KeyedCodec<>(KEY_DISPLAY_CHAT_BUTTON_ACTION,
                                                                                                        Codec.BOOLEAN),
                                                                                       CreateChatData::setCreateChatButtonPressed,
                                                                                       CreateChatData::getCreateChatButtonPressed)
                                                                             .addField(new KeyedCodec<>(KEY_PLAYER_EDIT_UUID,
                                                                                                        Codec.UUID_STRING),
                                                                                       CreateChatData::setPlayerEditUUID,
                                                                                       CreateChatData::getPlayerEditUUID)
                                                                             .addField(new KeyedCodec<>(KEY_PLAYER_EDIT_UUID_ACTION,
                                                                                                        Codec.STRING),
                                                                                       CreateChatData::setPlayerUUIDAction,
                                                                                       CreateChatData::getPlayerUUIDAction)
                                                                             .addField(new KeyedCodec<>(KEY_SEARCH_PLAYER_TEXT,
                                                                                                        Codec.STRING),
                                                                                       CreateChatData::setPlayerSearchQuery,
                                                                                       CreateChatData::getPlayerSearchQuery)
                                                                             .build();
        private boolean createChatButtonPressed;
        private String playerSearchQuery;

        private UUID playerEditUUID;
        private String playerUUIDAction;

        public CreateChatData() {}

        public boolean getCreateChatButtonPressed() {
            return createChatButtonPressed;
        }

        public void setCreateChatButtonPressed(boolean displayChat) {
            this.createChatButtonPressed = displayChat;
        }

        public String getPlayerSearchQuery() {
            return playerSearchQuery;
        }

        public void setPlayerSearchQuery(String playerSearchQuery) {
            this.playerSearchQuery = playerSearchQuery;
        }

        public String getPlayerUUIDAction() {
            return playerUUIDAction;
        }

        public void setPlayerUUIDAction(String playerUUIDAction) {
            this.playerUUIDAction = playerUUIDAction;
        }

        public UUID getPlayerEditUUID() {
            return playerEditUUID;
        }

        public void setPlayerEditUUID(UUID playerEditUUID) {
            this.playerEditUUID = playerEditUUID;
        }
    }
}
