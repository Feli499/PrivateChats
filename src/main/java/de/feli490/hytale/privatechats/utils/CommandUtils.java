package de.feli490.hytale.privatechats.utils;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.exceptions.SenderTypeException;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.util.concurrent.CompletableFuture;

public class CommandUtils {

    private CommandUtils() {}

    public static Player getPlayerFromContext(CommandContext context) throws SenderTypeException {
        return context.senderAs(Player.class);
    }

    public static CompletableFuture<PlayerRef> getPlayerRefFuture(CommandContext context) throws SenderTypeException {
        Player player = getPlayerFromContext(context);
        return PlayerUtils.getPlayerRef(player);
    }

    public static ConsoleSender getConsoleFromContext(CommandContext context) throws SenderTypeException {
        return context.senderAs(ConsoleSender.class);
    }
}
