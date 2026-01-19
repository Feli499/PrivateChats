package de.feli490.hytale.hyfechats.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import de.feli490.hytale.hyfechats.PrivateChatManager;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.utils.hytale.utils.CommandUtils;
import de.feli490.utils.hytale.utils.MessageUtils;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ResponseCommand extends AbstractAsyncCommand {

    private final PrivateChatManager chatManager;

    public ResponseCommand(PrivateChatManager chatManager) {
        super("response", "Responds into the last private chat you got a message from");

        this.chatManager = chatManager;

        addAliases("r");
        setAllowsExtraArguments(true);

        requirePermission("hyfechats.chat");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(@NonNullDecl CommandContext commandContext) {

        String[] splitMessage = commandContext.getInputString()
                                              .trim()
                                              .split(" ", 2);
        if (splitMessage.length < 2) {
            commandContext.sendMessage(MessageUtils.error("Please enter a message!"));
            return CompletableFuture.completedFuture(null);
        }

        String message = splitMessage[2];

        return CommandUtils.getPlayerRefFuture(commandContext)
                           .thenAccept(playerRef -> {

                               Chat lastChat = chatManager.getLastChat(playerRef.getUuid());
                               if (lastChat == null) {
                                   commandContext.sendMessage(MessageUtils.error("No chat to respond to!"));
                                   return;
                               }

                               lastChat.sendMessage(playerRef.getUuid(), message);
                           });
    }
}
