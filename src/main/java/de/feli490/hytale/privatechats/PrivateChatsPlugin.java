package de.feli490.hytale.privatechats;

import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import de.feli490.hytale.privatechats.commands.ChatCommand;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PrivateChatsPlugin extends JavaPlugin {

    private PrivateChatManager privateChatManager;

    public PrivateChatsPlugin(@NonNullDecl JavaPluginInit init) {
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

        privateChatManager = new PrivateChatManager();

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
        commandManager.registerSystemCommand(new ChatCommand(privateChatManager));
    }
}
