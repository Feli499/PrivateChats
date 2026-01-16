package de.feli490.hytale.privatechats;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import de.feli490.hytale.privatechats.chat.Chat;
import de.feli490.hytale.privatechats.chat.ChatRole;
import de.feli490.hytale.privatechats.chat.PlayerChatRole;
import de.feli490.hytale.privatechats.utils.PlayerUtils;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DebugChatFactory {

    private static final List<String> DEBUG_MESSAGES = List.of("Ist wie podcast im hintergrund",
                                                               "Sind super büro geräusche",
                                                               "wer das liest ist doof",
                                                               "pipimann",
                                                               "Ich bin ein geiles chick",
                                                               "ich habe hunger auf döner",
                                                               "fünf fliegende felis finden fünfzig füllhörner fingerfood");
    private final PrivateChatManager chatManager;
    private final Random random;

    public DebugChatFactory(EventRegistry eventRegistry, PrivateChatManager chatManager) {
        this.chatManager = chatManager;
        random = new Random();

        eventRegistry.registerGlobal(PlayerReadyEvent.class, playerReadyEvent -> {
            Player player = playerReadyEvent.getPlayer();
            PlayerUtils.getPlayerRef(player)
                       .thenAccept(playerRef -> addDebugChatsForPlayer(playerRef.getUuid()));
        });
    }

    public void addDebugChatsForPlayer(UUID playerUuid) {

        int chatAmount = random.nextInt(100) + 10;
        for (int i = 0; i < chatAmount; i++) {
            Chat randomDebugChat = createRandomDebugChat(playerUuid);
            createDebugMessages(randomDebugChat);
        }
    }

    private void createDebugMessages(Chat randomDebugChat) {

        if (random.nextInt(5) == 0) {
            return;
        }

        int messages = random.nextInt(10) + 1;
        for (int i = 0; i < messages; i++) {
            createChatMessage(randomDebugChat);
        }
    }

    private Chat createRandomDebugChat(UUID playerUuid) {

        if (random.nextInt(3) == 0) {
            return createGroupChat(playerUuid);
        }

        return createDirectChat(playerUuid);
    }

    private Chat createDirectChat(UUID player) {
        return chatManager.createDirectChat(player, UUID.randomUUID());
    }

    private Chat createGroupChat(UUID player) {
        Chat groupChat = chatManager.createGroupChat(player);

        int chatter = random.nextInt(5);
        for (int i = 0; i < chatter; i++) {
            groupChat.addChatter(UUID.randomUUID(), random.nextBoolean() ? ChatRole.MEMBER : ChatRole.ADMIN);
        }

        return groupChat;
    }

    private void createChatMessage(Chat chat) {
        List<PlayerChatRole> list = chat.getMembers()
                                        .stream()
                                        .toList();

        PlayerChatRole playerChatRole = list.get(random.nextInt(list.size()));
        chat.sendMessage(playerChatRole.getPlayerId(), getRandomDebugMessage());
    }

    private String getRandomDebugMessage() {
        return DEBUG_MESSAGES.get(random.nextInt(DEBUG_MESSAGES.size()));
    }
}
