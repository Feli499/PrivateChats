package de.feli490.hytale.hyfechats;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import de.feli490.hytale.hyfechats.chat.ChatFactory;
import de.feli490.hytale.hyfechats.chat.listeners.SaveChatPlayerOpensChatListener;
import de.feli490.hytale.hyfechats.chat.listeners.SendMessagesToChatReceivedNewMessageListener;
import de.feli490.hytale.hyfechats.commands.ChatCommand;
import de.feli490.hytale.hyfechats.commands.MsgCommand;
import de.feli490.hytale.hyfechats.commands.ResponseCommand;
import de.feli490.hytale.hyfechats.data.ChatDataLoader;
import de.feli490.hytale.hyfechats.data.ChatDataSaver;
import de.feli490.hytale.hyfechats.data.DataLoaderConverter;
import de.feli490.hytale.hyfechats.data.MemoryChatDataLoader;
import de.feli490.hytale.hyfechats.data.json.JsonChatDataLoader;
import de.feli490.hytale.hyfechats.data.json.JsonChatDataSaver;
import de.feli490.hytale.hyfechats.data.json.singlefile.SingleChatFileJsonChatDataLoader;
import de.feli490.hytale.hyfechats.events.PlayerUnreadChatsOnJoinEvent;
import de.feli490.utils.hytale.message.MessageBuilderFactory;
import de.feli490.utils.hytale.playerdata.PlayerDataProvider;
import de.feli490.utils.hytale.playerdata.PlayerDataProviderService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HyFeChatsPlugin extends JavaPlugin {

    private PrivateChatManager privateChatManager;
    private PlayerDataProvider playerDataProvider;
    private MessageBuilderFactory messageBuilderFactory;

    public HyFeChatsPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {

        messageBuilderFactory = new MessageBuilderFactory("#1492ff", "#ffffff");
        try {
            createDataFolder();
        } catch (IOException e) {
            getLogger().at(Level.SEVERE)
                       .withCause(e)
                       .log("Could not create data folder! Stopping Plugin...");
            return;
        }

        ChatDataLoader chatDataLoader;
        ChatDataSaver chatDataSaver;
        try {
            Path chatsFolder = getDataDirectory().resolve("chats");
            chatDataLoader = new JsonChatDataLoader(getLogger(), chatsFolder);
            chatDataSaver = new JsonChatDataSaver(getLogger(), chatsFolder);

            SingleChatFileJsonChatDataLoader singleChatFileJsonChatDataLoader = new SingleChatFileJsonChatDataLoader(getLogger(),
                                                                                                                     chatsFolder);
            DataLoaderConverter.convert(singleChatFileJsonChatDataLoader, chatDataSaver);

        } catch (Exception e) {
            MemoryChatDataLoader memoryChatDataLoader = new MemoryChatDataLoader();
            chatDataLoader = memoryChatDataLoader;
            chatDataSaver = memoryChatDataLoader;
            getLogger().at(Level.SEVERE)
                       .withCause(e)
                       .log("Could not load chats! Using MemoryChatDataLoader instead...");
        }

        playerDataProvider = PlayerDataProviderService.get();
        ChatFactory chatFactory = new ChatFactory(playerDataProvider);
        chatFactory.addPlayerOpensChatListenerForCreation(new SaveChatPlayerOpensChatListener(getLogger(), chatDataSaver));
        chatFactory.addMessageListenerForCreation(new SendMessagesToChatReceivedNewMessageListener(messageBuilderFactory,
                                                                                                   playerDataProvider));

        privateChatManager = new PrivateChatManager(getLogger(), chatFactory, chatDataLoader, chatDataSaver);

        getLogger().at(Level.INFO).log("Successfuly loaded the Plugin!");
    }

    @Override
    protected void start() {

        setupCommands();
        setupEvents();

        getLogger().at(Level.INFO).log("Successfuly started the Plugin!");
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("Successfuly stopped the Plugin!");
    }

    private void createDataFolder() throws IOException {

        Path dataDirectory = getDataDirectory();
        if (!Files.exists(dataDirectory)) {
            Files.createDirectory(dataDirectory);
        }
    }

    private void setupEvents() {
        EventRegistry eventRegistry = getEventRegistry();
        eventRegistry.registerGlobal(PlayerReadyEvent.class, new PlayerUnreadChatsOnJoinEvent(messageBuilderFactory, privateChatManager));
    }

    private void setupCommands() {
        CommandManager commandManager = CommandManager.get();
        commandManager.registerSystemCommand(new ChatCommand(messageBuilderFactory, privateChatManager, playerDataProvider));
        commandManager.registerSystemCommand(new ResponseCommand(messageBuilderFactory, privateChatManager));
        commandManager.registerSystemCommand(new MsgCommand(messageBuilderFactory, privateChatManager, playerDataProvider));
    }
}
