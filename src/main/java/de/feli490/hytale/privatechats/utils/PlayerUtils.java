package de.feli490.hytale.privatechats.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;

public class PlayerUtils {

    private PlayerUtils() {}

    public static CompletableFuture<PlayerRef> getPlayerRef(Player player) {

        Ref<EntityStore> reference = player.getReference();
        if (reference == null || !reference.isValid()) {
            return null;
        }

        Store<EntityStore> store = reference.getStore();
        World world = player.getWorld();

        return CompletableFuture.supplyAsync(() -> store.getComponent(reference, PlayerRef.getComponentType()), world);
    }

    public static Player getPlayer(PlayerRef playerRef) {
        Ref<EntityStore> reference = playerRef.getReference();
        return getPlayer(reference, reference.getStore());
    }

    public static Player getPlayer(Ref<EntityStore> ref, Store<EntityStore> store) {
        return store.getComponent(ref, Player.getComponentType());
    }

    public static PlayerRef getPlayerRef(Ref<EntityStore> ref, Store<EntityStore> store) {
        return store.getComponent(ref, PlayerRef.getComponentType());
    }
}
