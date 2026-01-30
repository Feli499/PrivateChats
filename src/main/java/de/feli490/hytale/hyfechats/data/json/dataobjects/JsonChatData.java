package de.feli490.hytale.hyfechats.data.json.dataobjects;

import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.ChatType;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;
import de.feli490.hytale.hyfechats.data.ChatData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class JsonChatData implements ChatData {

    private final JsonChatMetaData metaData;
    private final Set<JsonPlayerChatProperties> jsonPlayerChatProperties;
    private final List<JsonMessageData> jsonMessageData;

    public JsonChatData(JsonChatMetaData metaData, List<JsonMessageData> jsonMessageData,
            Set<JsonPlayerChatProperties> jsonPlayerChatProperties) {
        this.metaData = metaData;
        this.jsonMessageData = new ArrayList<>(jsonMessageData);
        this.jsonPlayerChatProperties = new HashSet<>(jsonPlayerChatProperties);
    }

    public JsonChatData(Chat chat) {

        this.metaData = new JsonChatMetaData(chat);

        this.jsonMessageData = new ArrayList<>();
        chat.getMessages()
            .forEach(message -> jsonMessageData.add(new JsonMessageData(message)));

        this.jsonPlayerChatProperties = new HashSet<>();
        chat.getMembers()
            .forEach(member -> jsonPlayerChatProperties.add(new JsonPlayerChatProperties(member)));
    }

    @Override
    public UUID getId() {
        return metaData.getId();
    }

    @Override
    public long getCreated() {
        return metaData.getCreated();
    }

    @Override
    public ChatType getChatType() {
        return ChatType.valueOf(metaData.getChatTypeString());
    }

    @Override
    public Set<PlayerChatProperties> getPlayerChatProperties(Chat chat) {

        HashSet<PlayerChatProperties> playerChatProperties = new HashSet<>(jsonPlayerChatProperties.size());
        for (JsonPlayerChatProperties jsonPlayerChatProperty : jsonPlayerChatProperties) {
            playerChatProperties.add(jsonPlayerChatProperty.toPlayerChatProperties(chat));
        }
        return playerChatProperties;
    }

    @Override
    public List<ChatMessage> getMessages(Chat chat) {
        ArrayList<ChatMessage> chatMessages = new ArrayList<>(jsonMessageData.size());
        for (JsonMessageData jsonMessageDatum : jsonMessageData) {
            chatMessages.add(jsonMessageDatum.toChatMessage(chat));
        }
        return chatMessages;
    }
}
