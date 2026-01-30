package de.feli490.hytale.hyfechats.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SingleChatFileJsonChatDataLoader implements ChatDataLoader {

    private final Path directory;
    private final HytaleLogger logger;

    public SingleChatFileJsonChatDataLoader(HytaleLogger logger, Path path) throws IOException {

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
                chats.add(jsonChatData);
            }
        }
        return chats;
    }

    public void deleteFiles() throws IOException {

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory, "*.json")) {
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }

    public static class JsonChatData implements ChatData {

        public static Codec<JsonChatData> CODEC = BuilderCodec.builder(SingleChatFileJsonChatDataLoader.JsonChatData.class,
                                                                       SingleChatFileJsonChatDataLoader.JsonChatData::new)
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
        private final Set<JsonPlayerChatProperties> playerChatProperties = new HashSet<>();
        private UUID id;
        private ChatType chatType;
        private long created;
        private List<JsonMessageData> messages;

        public JsonChatData() {}

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

            this.playerChatProperties.addAll(List.of(jsonPlayerChatPropertiesArray));
        }

        public JsonPlayerChatProperties[] getJsonPlayerChatProperties() {
            return this.playerChatProperties.toArray(new JsonPlayerChatProperties[0]);
        }

        public void setJsonPlayerChatProperties(JsonPlayerChatProperties[] JsonPlayerChatProperties) {
            this.playerChatProperties.addAll(List.of(JsonPlayerChatProperties));
        }

        @Override
        public Set<PlayerChatProperties> getPlayerChatProperties(Chat chat) {

            Set<PlayerChatProperties> playerChatProperties = new HashSet<>();
            for (JsonPlayerChatProperties jsonPlayerChatProperties : getJsonPlayerChatProperties()) {
                playerChatProperties.add(jsonPlayerChatProperties.toPlayerChatProperties(chat));
            }
            return playerChatProperties;
        }

        public JsonMessageData[] getJsonMessageData() {
            return messages.toArray(new JsonMessageData[0]);
        }

        public void setJsonMessageData(JsonMessageData[] messages) {
            this.messages = Arrays.stream(messages)
                                  .toList();
        }

        @Override
        public List<ChatMessage> getMessages(Chat chat) {

            ArrayList<ChatMessage> chatMessages = new ArrayList<>(messages.size());
            for (JsonMessageData message : messages) {
                chatMessages.add(message.toChatMessage(chat));
            }
            return chatMessages;
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

        public void setDisplayUnreadProperty(String displayUnreadPropertyString) {
            this.displayUnreadProperty = DisplayUnreadProperty.valueOf(displayUnreadPropertyString);
        }

        public long getLastRead() {
            return lastRead;
        }

        public void setLastRead(long lastRead) {
            this.lastRead = lastRead;
        }

        public PlayerChatProperties toPlayerChatProperties(Chat chat) {
            return new PlayerChatProperties(chat, playerId, memberSince, role, lastRead, displayUnreadProperty);
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

        public ChatMessage toChatMessage(Chat chat) {
            return new ChatMessage(chat, id, senderId, message, timestamp);
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
