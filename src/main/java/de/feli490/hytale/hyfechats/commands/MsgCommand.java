package de.feli490.hytale.hyfechats.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import de.feli490.hytale.hyfechats.PrivateChatManager;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.utils.hytale.playerdata.PlayerDataProvider;
import de.feli490.utils.hytale.utils.CommandUtils;
import de.feli490.utils.hytale.utils.MessageUtils;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class MsgCommand extends AbstractAsyncCommand {

    private final PrivateChatManager chatManager;
    private final PlayerDataProvider playerDataProvider;

    private final RequiredArg<String> playerArgument;

    public MsgCommand(PrivateChatManager chatManager, PlayerDataProvider playerDataProvider) {
        super("msg", "Sends a private message to a player");

        this.chatManager = chatManager;
        this.playerDataProvider = playerDataProvider;

        addAliases("tell", "whisper");
        playerArgument = withRequiredArg("player", "The name of the player to write to", ArgTypes.STRING);

        setAllowsExtraArguments(true);
        requirePermission("hyfechats.chat");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(@NonNullDecl CommandContext commandContext) {

        String playerString = playerArgument.get(commandContext);
        UUID playerUUID = null;
        try {
            playerUUID = UUID.fromString(playerString);
        } catch (Exception e) {
            playerUUID = playerDataProvider.getPlayerUUIDByLastName(playerString);
        }

        if (playerUUID == null) {
            commandContext.sendMessage(MessageUtils.error("Unknown player: " + playerString));
            return CompletableFuture.completedFuture(null);
        }

        String[] splitMessage = commandContext.getInputString()
                                              .trim()
                                              .split(" ", 3);
        if (splitMessage.length < 3) {
            commandContext.sendMessage(MessageUtils.error("Please enter a message!"));
            return CompletableFuture.completedFuture(null);
        }

        String message = splitMessage[2];

        UUID receiverUUID = playerUUID;
        return CommandUtils.getPlayerRefFuture(commandContext)
                           .thenAccept(playerRef -> {

                               UUID senderUUID = playerRef.getUuid();
                               if (senderUUID.equals(receiverUUID)) {
                                   commandContext.sendMessage(MessageUtils.error("You can't message yourself!"));
                                   return;
                               }

                               Chat directChat = chatManager.createDirectChat(senderUUID, receiverUUID);
                               directChat.sendMessage(senderUUID, message);
                           });
    }
}
