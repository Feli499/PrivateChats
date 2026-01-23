package de.feli490.hytale.hyfechats;

import com.hypixel.hytale.logger.HytaleLogger;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatFactory;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.ChatRole;
import de.feli490.hytale.hyfechats.chat.ChatType;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;
import de.feli490.hytale.hyfechats.chat.listeners.MemberChangedListener;
import de.feli490.hytale.hyfechats.chat.listeners.ReceivedNewMessageListener;
import de.feli490.hytale.hyfechats.data.ChatData;
import de.feli490.hytale.hyfechats.data.ChatDataLoader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

public class PrivateChatManager {

    private final Set<Chat> chats;
    private final HytaleLogger logger;
    private final ChatFactory chatFactory;
    private final ChatDataLoader chatDataLoader;

    public PrivateChatManager(HytaleLogger logger, ChatFactory chatFactory, ChatDataLoader chatDataLoader) {
        this.logger = logger.getSubLogger("PrivateChatManager");
        this.chatFactory = chatFactory;
        this.chatDataLoader = chatDataLoader;
        chats = new HashSet<>();

        ChatManagerChatListener chatManagerChatListener = new ChatManagerChatListener(this);
        chatFactory.addMemberChangedListenerForCreation(chatManagerChatListener);
        chatFactory.addMessageListenerForCreation(chatManagerChatListener);

        loadChats();
    }

    private void loadChats() {

        try {
            for (ChatData loadChat : chatDataLoader.loadChats()) {
                Chat chat = chatFactory.fromChatData(loadChat);
                chats.add(chat);
            }
        } catch (IOException e) {
            logger.at(Level.SEVERE)
                  .withCause(e)
                  .log("Could not load chats!");
        }
    }

    public Chat getLastChat(UUID playerId) {
        return chats.stream()
                    .filter(chat -> chat.isMember(playerId))
                    .max(Comparator.comparingLong(Chat::getLastUpdate))
                    .orElse(null);
    }

    public Chat createGroupChat(UUID owner, List<UUID> memberUUIDs) {

        Stream<Chat> chatStream = chats.stream()
                                       .filter(chat -> chat.isMember(owner))
                                       .filter(chat -> chat.getChatType() == ChatType.GROUP)
                                       .filter(chat -> chat.getMembers()
                                                           .size() == memberUUIDs.size() + 1) //Add the owner
                                       .filter(chat -> memberUUIDs.stream()
                                                                  .allMatch(chat::isMember));
        Optional<Chat> chat = chatStream.findFirst();
        if (chat.isPresent())
            return chat.get();

        Chat group = chatFactory.createGroup(owner);
        memberUUIDs.forEach(uuid -> group.addChatter(uuid, ChatRole.MEMBER));

        chats.add(group);
        saveChat(group);
        return group;
    }

    public Chat createDirectChat(UUID player1, UUID player2) {

        Optional<Chat> first = chats.stream()
                                    .filter(chat -> chat.isMember(player1) && chat.isMember(player2)
                                            && ChatType.DIRECT.equals(chat.getChatType()))
                                    .findFirst();
        if (first.isPresent())
            return first.get();

        Chat direct = chatFactory.createDirect(player1, player2);

        chats.add(direct);
        saveChat(direct);
        return direct;
    }

    public List<Chat> getSortedChats(UUID playerId) {
        return chats.stream()
                    .filter(chat -> chat.isMember(playerId))
                    .sorted(Comparator.comparingLong(Chat::getLastUpdate)
                                      .reversed())
                    .toList();
    }

    private void saveChat(Chat chat) {
        try {
            chatDataLoader.saveChat(chat);
        } catch (IOException e) {
            logger.at(Level.SEVERE)
                  .withCause(e)
                  .log("Could not save chat: " + chat.getId());
        }
    }

    private record ChatManagerChatListener(PrivateChatManager privateChatManager)
            implements ReceivedNewMessageListener, MemberChangedListener {

        @Override
        public void onMessage(Chat chat, ChatMessage message) {
            privateChatManager.saveChat(chat);
        }

        @Override
        public void onMemberAdded(Chat chat, PlayerChatProperties member) {
            privateChatManager.saveChat(chat);
        }

        @Override
        public void onMemberRemoved(Chat chat, PlayerChatProperties member) {
            privateChatManager.saveChat(chat);
        }

        @Override
        public void onMemberRoleChanged(Chat chat, PlayerChatProperties newRole, ChatRole oldRole) {
            privateChatManager.saveChat(chat);
        }
    }
}
