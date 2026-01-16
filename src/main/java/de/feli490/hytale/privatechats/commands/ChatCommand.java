package de.feli490.hytale.privatechats.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import de.feli490.hytale.privatechats.gui.PrivateChatsUI;
import de.feli490.hytale.privatechats.utils.CommandUtils;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ChatCommand extends AbstractAsyncCommand {
    public ChatCommand() {
        super("chat", "Opens the chat menu");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(@NonNullDecl CommandContext commandContext) {
        Player player = CommandUtils.getPlayerFromContext(commandContext);

        return CommandUtils.getPlayerRefFuture(commandContext)
                           .thenAccept(playerRef -> {

                               CustomUIPage customPage = player.getPageManager()
                                                               .getCustomPage();
                               if (customPage != null)
                                   return;

                               Ref<EntityStore> reference = playerRef.getReference();

                               PrivateChatsUI privateChatsUI = new PrivateChatsUI(playerRef, CustomPageLifetime.CanDismiss);

                               player.getPageManager()
                                     .openCustomPage(reference, reference.getStore(), privateChatsUI);
                           });
    }
}
