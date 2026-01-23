package de.feli490.hytale.hyfechats.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.BsonUtil;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.ChatRole;
import de.feli490.hytale.hyfechats.chat.ChatType;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;
import de.feli490.hytale.hyfechats.chat.playerchatproperties.DisplayUnreadProperty;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class JsonChatDataLoader implements ChatDataLoader {

    private final Path directory;
    private final HytaleLogger logger;

    public JsonChatDataLoader(HytaleLogger logger, Path path) throws IOException {

        this.logger = logger.getSubLogger("JsonChatWriter");
        directory = path;
        
        if (!Files.exists(path))
            Files.createDirectories(path);

        if (!Files.isDirectory(path))
            throw new IllegalArgumentException("Path must be a directory");

    }

    @Override
    public Collection<ChatData> loadChats() throws IOException {

        Set<ChatData> chats = new HashSet<>();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory, "*.json")) {
            for (Path path : paths) {
                JsonChatData jsonChatData = RawJsonReader.readSync(path, JsonChatData.CODEC, logger);
                if (jsonChatData.needsToBeResaved())
                    saveChat(jsonChatData);
                chats.add(jsonChatData);
            }
        }
        return chats;
    }

    @Override
    public void deleteChat(UUID chatId) throws IOException {
        Files.deleteIfExists(directory.resolve(chatId.toString() + ".json"));
    }

    @Override
    public void saveChat(Chat chat) throws IOException {

        JsonChatData jsonChatData = JsonChatData.fromChat(chat);
        saveChat(jsonChatData);
    }

    private void saveChat(JsonChatData jsonChatData) throws IOException {
        BsonUtil.writeSync(directory.resolve(jsonChatData.getId()
                                                         .toString() + ".json"), JsonChatData.CODEC, jsonChatData, logger);
    }

    @Override
    public void saveChats(Collection<Chat> chats) throws IOException {
        for (Chat chat : chats) {
            saveChat(chat);
        }
    }

    public static class JsonChatData implements ChatData {

        public static Codec<JsonChatData> CODEC = BuilderCodec.builder(JsonChatDataLoader.JsonChatData.class,
                                                                       JsonChatDataLoader.JsonChatData::new)
                                                              .addField(new KeyedCodec<>("Id", Codec.UUID_STRING),
                                                                        JsonChatData::setId,
                                                                        JsonChatData::getId)
                                                              .addField(new KeyedCodec<>("ChatType", Codec.STRING),
                                                                        JsonChatData::setChatTypeString,
                                                                        JsonChatData::getChatTypeString)
                                                              .addField(new KeyedCodec<>("Created", Codec.LONG),
                                                                        JsonChatData::setCreated,
                                                                        JsonChatData::getCreated)
                                                              .addField(new KeyedCodec<>("Messages", JsonMessageData.ARRAY_CODEC),
                                                                        JsonChatData::setJsonMessageData,
                                                                        JsonChatData::getJsonMessageData)
                                                              .addField(new KeyedCodec<>("PlayerChatRoles",
                                                                                         JsonPlayerChatProperties.ARRAY_CODEC),
                                                                        JsonChatData::setJsonChatRoleData,
                                                                        _ -> new JsonPlayerChatProperties[0])
                                                              .addField(new KeyedCodec<>("PlayerChatProperties",
                                                                                         JsonPlayerChatProperties.ARRAY_CODEC),
                                                                        JsonChatData::setJsonPlayerChatProperties,
                                                                        JsonChatData::getJsonPlayerChatProperties)
                                                              .build();

        private UUID id;
        private ChatType chatType;
        private long created;

        private List<ChatMessage> messages;
        private boolean needsToBeResaved = false;
        private Set<PlayerChatProperties> playerChatProperties = new HashSet<>();

        public JsonChatData() {}

        public boolean needsToBeResaved() {
            return needsToBeResaved;
        }

        @Override
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        @Override
        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        @Override
        public ChatType getChatType() {
            return chatType;
        }

        public String getChatTypeString() {
            return chatType.name();
        }

        public void setChatTypeString(String chatType) {
            this.chatType = ChatType.valueOf(chatType);
        }

        public void setJsonChatRoleData(JsonPlayerChatProperties[] jsonPlayerChatPropertiesArray) {

            if (jsonPlayerChatPropertiesArray == null || jsonPlayerChatPropertiesArray.length == 0)
                return;

            Set<PlayerChatProperties> playerChatProperties = new HashSet<>(jsonPlayerChatPropertiesArray.length);
            for (JsonPlayerChatProperties jsonPlayerChatProperties : jsonPlayerChatPropertiesArray) {
                playerChatProperties.add(jsonPlayerChatProperties.toPlayerChatProperties());
            }
            this.playerChatProperties.addAll(playerChatProperties);
            needsToBeResaved = true;
        }

        public JsonPlayerChatProperties[] getJsonPlayerChatProperties() {

            JsonPlayerChatProperties[] jsonPlayerChatRoleData = new JsonPlayerChatProperties[playerChatProperties.size()];
            int i = 0;
            for (PlayerChatProperties playerChatProperties : this.playerChatProperties) {
                jsonPlayerChatRoleData[i++] = new JsonPlayerChatProperties(playerChatProperties);
            }
            return jsonPlayerChatRoleData;
        }

        public void setJsonPlayerChatProperties(JsonPlayerChatProperties[] JsonPlayerChatProperties) {

            Set<PlayerChatProperties> playerChatProperties = new HashSet<>(JsonPlayerChatProperties.length);
            for (JsonPlayerChatProperties jsonPlayerChatProperties : JsonPlayerChatProperties) {
                playerChatProperties.add(jsonPlayerChatProperties.toPlayerChatProperties());
            }
            this.playerChatProperties.addAll(playerChatProperties);
        }

        @Override
        public Set<PlayerChatProperties> getPlayerChatProperties() {
            return playerChatProperties;
        }

        public void setPlayerChatProperties(Set<PlayerChatProperties> playerChatProperties) {
            this.playerChatProperties = new HashSet<>(playerChatProperties);
        }

        public JsonMessageData[] getJsonMessageData() {

            JsonMessageData[] jsonMessageData = new JsonMessageData[messages.size()];
            for (int i = 0; i < messages.size(); i++) {
                ChatMessage message = messages.get(i);
                jsonMessageData[i] = new JsonMessageData(message);
            }
            return jsonMessageData;
        }

        public void setJsonMessageData(JsonMessageData[] messages) {

            ArrayList<ChatMessage> messagesList = new ArrayList<>(messages.length);
            for (JsonMessageData message : messages) {
                messagesList.add(message.toChatMessage());
            }
            this.messages = messagesList;
        }

        @Override
        public List<ChatMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<ChatMessage> messages) {
            this.messages = List.copyOf(messages);
        }

        public static JsonChatData fromChat(Chat chat) {

            JsonChatData jsonChatData = new JsonChatData();

            jsonChatData.setId(chat.getId());
            jsonChatData.setChatTypeString(chat.getChatType()
                                               .name());
            jsonChatData.setCreated(chat.getCreated());
            jsonChatData.setMessages(chat.getMessages());
            jsonChatData.setPlayerChatProperties(chat.getMembers());

            return jsonChatData;
        }
    }

    public static class JsonPlayerChatProperties {

        public static Codec<JsonPlayerChatProperties> CODEC = BuilderCodec.builder(JsonPlayerChatProperties.class,
                                                                                   JsonPlayerChatProperties::new)
                                                                          .addField(new KeyedCodec<>("PlayerId", Codec.UUID_STRING),
                                                                                    JsonPlayerChatProperties::setPlayerId,
                                                                                    JsonPlayerChatProperties::getPlayerId)
                                                                          .addField(new KeyedCodec<>("MemberSince", Codec.LONG),
                                                                                    JsonPlayerChatProperties::setMemberSince,
                                                                                    JsonPlayerChatProperties::getMemberSince)
                                                                          .addField(new KeyedCodec<>("Role", Codec.STRING),
                                                                                    JsonPlayerChatProperties::setRole,
                                                                                    JsonPlayerChatProperties::getRole)
                                                                          .addField(new KeyedCodec<>("LastRead", Codec.LONG),
                                                                                    JsonPlayerChatProperties::setLastRead,
                                                                                    JsonPlayerChatProperties::getLastRead)
                                                                          .addField(new KeyedCodec<>("DisplayUnread", Codec.STRING),
                                                                                    JsonPlayerChatProperties::setDisplayUnreadProperty,
                                                                                    JsonPlayerChatProperties::getDisplayUnreadPropertyString)
                                                                          .build();
        public static ArrayCodec<JsonPlayerChatProperties> ARRAY_CODEC = new ArrayCodec<>(CODEC,
                                                                                          JsonPlayerChatProperties[]::new,
                                                                                          JsonPlayerChatProperties::new);
        private UUID playerId;
        private long memberSince;
        private ChatRole role;

        private long lastRead;
        private DisplayUnreadProperty displayUnreadProperty;

        public JsonPlayerChatProperties() {
            lastRead = System.currentTimeMillis();
            displayUnreadProperty = DisplayUnreadProperty.ALWAYS;
        }

        public JsonPlayerChatProperties(PlayerChatProperties playerChatProperties) {
            this.playerId = playerChatProperties.getPlayerId();
            this.role = playerChatProperties.getRole();
            this.memberSince = playerChatProperties.getMemberSince();
            this.lastRead = playerChatProperties.getLastRead();
            this.displayUnreadProperty = playerChatProperties.getDisplayUnread();
        }

        public UUID getPlayerId() {
            return playerId;
        }

        public void setPlayerId(UUID playerId) {
            this.playerId = playerId;
        }

        public String getRole() {
            return role.name();
        }

        public void setRole(String role) {
            this.role = ChatRole.valueOf(role);
        }

        public long getMemberSince() {
            return memberSince;
        }

        public void setMemberSince(long memberSince) {
            this.memberSince = memberSince;
        }

        public String getDisplayUnreadPropertyString() {
            return displayUnreadProperty.name();
        }

        public DisplayUnreadProperty getDisplayUnreadProperty() {
            return displayUnreadProperty;
        }

        public void setDisplayUnreadProperty(String displayUnreadPropertyString) {
            this.displayUnreadProperty = DisplayUnreadProperty.valueOf(displayUnreadPropertyString);
        }

        public void setDisplayUnreadProperty(DisplayUnreadProperty displayUnreadProperty) {
            this.displayUnreadProperty = displayUnreadProperty;
        }

        public long getLastRead() {
            return lastRead;
        }

        public void setLastRead(long lastRead) {
            this.lastRead = lastRead;
        }

        public PlayerChatProperties toPlayerChatProperties() {
            return new PlayerChatProperties(playerId, memberSince, role, lastRead, displayUnreadProperty);
        }
    }

    public static class JsonMessageData {

        public static Codec<JsonMessageData> CODEC = BuilderCodec.builder(JsonMessageData.class, JsonMessageData::new)
                                                                 .addField(new KeyedCodec<>("Id", Codec.UUID_STRING),
                                                                           JsonMessageData::setId,
                                                                           JsonMessageData::getId)
                                                                 .addField(new KeyedCodec<>("SenderId", Codec.UUID_STRING),
                                                                           JsonMessageData::setSenderId,
                                                                           JsonMessageData::getSenderId)
                                                                 .addField(new KeyedCodec<>("Message", Codec.STRING),
                                                                           JsonMessageData::setMessage,
                                                                           JsonMessageData::getMessage)
                                                                 .addField(new KeyedCodec<>("Timestamp", Codec.LONG),
                                                                           JsonMessageData::setTimestamp,
                                                                           JsonMessageData::getTimestamp)
                                                                 .build();

        public static ArrayCodec<JsonMessageData> ARRAY_CODEC = new ArrayCodec<>(CODEC, JsonMessageData[]::new, JsonMessageData::new);
        private UUID id;
        private UUID senderId;
        private String message;
        private Long timestamp;

        public JsonMessageData() {}

        public JsonMessageData(ChatMessage message) {
            this.id = message.id();
            this.senderId = message.senderId();
            this.message = message.message();
            this.timestamp = message.timestamp();
        }

        public ChatMessage toChatMessage() {
            return new ChatMessage(id, senderId, message, timestamp);
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public UUID getSenderId() {
            return senderId;
        }

        public void setSenderId(UUID senderId) {
            this.senderId = senderId;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }
    }
}
