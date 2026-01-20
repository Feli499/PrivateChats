package de.feli490.hytale.hyfechats.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.feli490.hytale.hyfechats.PrivateChatManager;
import de.feli490.utils.core.common.tuple.Pair;
import de.feli490.utils.hytale.playerdata.PlayerDataProvider;
import de.feli490.utils.hytale.utils.PlayerUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class CreateChatUI extends InteractiveCustomUIPage<CreateChatUI.CreateChatData> {

    private final PrivateChatManager chatManager;
    private final PlayerDataProvider playerDataProvider;
    private Supplier<CustomUIPage> returnPageSupplier;

    private List<UUID> currentDisplayedUUIDs;

    private final List<UUID> selectedPlayerUUIDs;
    private String playerSearchQuery = "";

    public CreateChatUI(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime, PrivateChatManager chatManager,
            PlayerDataProvider playerDataProvider, Supplier<CustomUIPage> returnPageSupplier) {
        super(playerRef, lifetime, CreateChatData.CODEC);
        this.chatManager = chatManager;
        this.playerDataProvider = playerDataProvider;
        this.returnPageSupplier = returnPageSupplier;
        currentDisplayedUUIDs = Collections.emptyList();

        selectedPlayerUUIDs = new ArrayList<>();
    }

    public CreateChatUI(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime, PrivateChatManager chatManager,
            PlayerDataProvider playerDataProvider) {
        this(playerRef, lifetime, chatManager, playerDataProvider, null);
    }

    @Override
    public void onDismiss(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl Store<EntityStore> store) {
        Player player = PlayerUtils.getPlayer(playerRef);
        CompletableFuture.delayedExecutor(1, TimeUnit.MILLISECONDS, player.getWorld())
                         .execute(this::goToPreviousPage);
    }

    @Override
    public void build(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl UICommandBuilder uiCommandBuilder,
            @NonNullDecl UIEventBuilder uiEventBuilder, @NonNullDecl Store<EntityStore> store) {

        uiCommandBuilder.append("CreateChat/CreateChat.ui");
        updatePlayerList(uiCommandBuilder, uiEventBuilder);

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                                       "#SearchPlayerName",
                                       EventData.of(CreateChatData.KEY_SEARCH_PLAYER_TEXT, "#SearchPlayerName.Value"),
                                       false);

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                                       "#CreateChatButton",
                                       EventData.of(CreateChatData.KEY_CREATE_CHAT_BUTTON_ACTION, "true"),
                                       false);
    }

    private void updatePlayerList(UICommandBuilder uiCommandBuilder, UIEventBuilder uiEventBuilder) {

        uiCommandBuilder.clear("#PlayerItems");

        List<Pair<UUID, String>> filteredUUIDs = new ArrayList<>();

        playerDataProvider.getAllKnownPlayerUUIDs()
                          .forEach(uuid -> {
                              String playerName = playerDataProvider.getLastPlayerName(uuid);
                              filteredUUIDs.add(new Pair<>(uuid, playerName));
                          });

        if (!playerSearchQuery.isEmpty())
            filteredUUIDs.removeIf(pair -> !pair.getSecond()
                                                .toLowerCase()
                                                .contains(playerSearchQuery));
        filteredUUIDs.removeIf(pair -> pair.getFirst()
                                           .equals(playerRef.getUuid()));

        filteredUUIDs.sort(Comparator.comparing(Pair::getSecond));
        currentDisplayedUUIDs = filteredUUIDs.stream()
                                             .map(Pair::getFirst)
                                             .toList();

        int column = 0;
        int row = 0;
        for (Pair<UUID, String> pair : filteredUUIDs) {
            UUID currentUUID = pair.getFirst();

            if (column == 0) {
                uiCommandBuilder.appendInline("#PlayerItems", "Group { LayoutMode: Left; Anchor: (Bottom: 0); }");
            }

            uiCommandBuilder.append("#PlayerItems[" + row + "]", "CreateChat/PlayerListItem.ui");
            uiCommandBuilder.set("#PlayerItems[" + row + "][" + column + "] #IsSelected.Value", selectedPlayerUUIDs.contains(currentUUID));
            uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                                           "#PlayerItems[" + row + "][" + column + "] #PlayerListItemButton",
                                           EventData.of(CreateChatData.KEY_PLAYER_EDIT_UUID, currentUUID.toString()),
                                           false);

            uiCommandBuilder.set("#PlayerItems[" + row + "][" + column + "] #PlayerName.Text", pair.getSecond());

            ++column;
            if (column >= 3) {
                column = 0;
                ++row;
            }
        }
    }

    public void updatePlayerList(String playerSearchQuery) {

        this.playerSearchQuery = playerSearchQuery;

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
            updatePlayerList(data.getPlayerSearchQuery());
        } else if (data.getPlayerEditUUID() != null) {
            toggleSelectedPlayer(data.getPlayerEditUUID());
        } else if (data.getCreateChatButtonPressed() != null) {
            createChat();
        }
    }

    private void goToPreviousPage() {
        if (returnPageSupplier == null)
            return;
        CustomUIPage customUIPage = returnPageSupplier.get();
        returnPageSupplier = null;

        Ref<EntityStore> reference = playerRef.getReference();
        PlayerUtils.getPlayer(playerRef)
                   .getPageManager()
                   .openCustomPage(reference, reference.getStore(), customUIPage);
        sendUpdate();
    }

    private void createChat() {
        if (selectedPlayerUUIDs.size() == 1) {
            chatManager.createDirectChat(playerRef.getUuid(), selectedPlayerUUIDs.get(0));
        } else if (selectedPlayerUUIDs.size() > 1) {
            chatManager.createGroupChat(playerRef.getUuid(), selectedPlayerUUIDs);
        }

        goToPreviousPage();
    }

    public void toggleSelectedPlayer(UUID playerUUID) {

        if (selectedPlayerUUIDs.contains(playerUUID)) {
            selectedPlayerUUIDs.remove(playerUUID);
        } else {
            selectedPlayerUUIDs.add(playerUUID);
        }

        int index = currentDisplayedUUIDs.indexOf(playerUUID);
        int row = index / 2;
        int column = index % 2;

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        uiCommandBuilder.set("#PlayerItems[" + row + "][" + column + "] #IsSelected.Value", selectedPlayerUUIDs.contains(playerUUID));

        sendUpdate(uiCommandBuilder, false);
    }

    public static class CreateChatData {

        static final String KEY_CREATE_CHAT_BUTTON_ACTION = "CreateChatButton";
        static final String KEY_PLAYER_EDIT_UUID = "PlayerEditUUID";
        static final String KEY_SEARCH_PLAYER_TEXT = "@SearchPlayerName";

        public static final BuilderCodec<CreateChatData> CODEC = BuilderCodec.builder(CreateChatData.class, CreateChatData::new)
                                                                             .addField(new KeyedCodec<>(KEY_CREATE_CHAT_BUTTON_ACTION,
                                                                                                        Codec.STRING),
                                                                                       CreateChatData::setCreateChatButtonPressed,
                                                                                       CreateChatData::getCreateChatButtonPressed)
                                                                             .addField(new KeyedCodec<>(KEY_PLAYER_EDIT_UUID,
                                                                                                        Codec.UUID_STRING),
                                                                                       CreateChatData::setPlayerEditUUID,
                                                                                       CreateChatData::getPlayerEditUUID)
                                                                             .addField(new KeyedCodec<>(KEY_SEARCH_PLAYER_TEXT,
                                                                                                        Codec.STRING),
                                                                                       CreateChatData::setPlayerSearchQuery,
                                                                                       CreateChatData::getPlayerSearchQuery)
                                                                             .build();
        private String createChatButtonPressed;
        private String playerSearchQuery;

        private UUID playerEditUUID;

        public CreateChatData() {}

        public String getCreateChatButtonPressed() {
            return createChatButtonPressed;
        }

        public void setCreateChatButtonPressed(String createChatButtonPressed) {
            this.createChatButtonPressed = createChatButtonPressed;
        }

        public String getPlayerSearchQuery() {
            return playerSearchQuery;
        }

        public void setPlayerSearchQuery(String playerSearchQuery) {
            this.playerSearchQuery = playerSearchQuery.toLowerCase();
        }

        public UUID getPlayerEditUUID() {
            return playerEditUUID;
        }

        public void setPlayerEditUUID(UUID playerEditUUID) {
            this.playerEditUUID = playerEditUUID;
        }
    }
}
