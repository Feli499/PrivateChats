package de.feli490.hytale.hyfechats;

import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import de.feli490.hytale.hyfechats.chat.ChatFactory;
import de.feli490.hytale.hyfechats.commands.ChatCommand;
import de.feli490.hytale.hyfechats.data.ChatDataLoader;
import de.feli490.hytale.hyfechats.data.JsonChatDataLoader;
import de.feli490.hytale.hyfechats.data.MemoryChatDataLoader;
import de.feli490.utils.hytale.PlayerDataProviderInstance;
import de.feli490.utils.hytale.playerdata.PlayerDataProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class HyFeChatsPlugin extends JavaPlugin {

    private PrivateChatManager privateChatManager;
    private PlayerDataProvider playerDataProvider;

    public HyFeChatsPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {

        try {
            createDataFolder();
        } catch (IOException e) {
            getLogger().at(Level.SEVERE)
                       .withCause(e)
                       .log("Could not create data folder! Stopping Plugin...");
            return;
        }

        ChatDataLoader chatDataLoader;
        try {
            chatDataLoader = new JsonChatDataLoader(getLogger(), getDataDirectory().resolve("chats"));
        } catch (Exception e) {
            chatDataLoader = new MemoryChatDataLoader();
            getLogger().at(Level.SEVERE)
                       .withCause(e)
                       .log("Could not load chats! Using MemoryChatDataLoader instead...");
        }

        playerDataProvider = PlayerDataProviderInstance.get();
        ChatFactory chatFactory = new ChatFactory(playerDataProvider);
        privateChatManager = new PrivateChatManager(getLogger(), chatFactory, chatDataLoader);

        getLogger().at(Level.INFO).log("Successfuly loaded the Plugin!");
    }

    @Override
    protected void start() {

        setupCommands();

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

    private void setupCommands() {
        CommandManager commandManager = CommandManager.get();
        commandManager.registerSystemCommand(new ChatCommand(privateChatManager, playerDataProvider));
    }
}
