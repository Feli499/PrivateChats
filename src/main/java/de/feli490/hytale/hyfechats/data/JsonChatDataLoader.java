package de.feli490.hytale.hyfechats.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.ChatRole;
import de.feli490.hytale.hyfechats.chat.ChatType;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;
import de.feli490.hytale.hyfechats.chat.playerchatproperties.DisplayUnreadProperty;
import java.io.FileReader;
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
import org.bson.BsonString;

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
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory, Files::isDirectory)) {
            for (Path path : paths) {
                ChatData chatData = loadChat(path);
                chats.add(chatData);
            }
        }
        return chats;
    }

    private ChatData loadChat(Path path) throws IOException {

        JsonChatMetaData jsonChatMetaData = RawJsonReader.readSync(getMetaDataFile(path), JsonChatMetaData.CODEC, logger);
        Set<JsonPlayerChatProperties> jsonPlayerChatProperties = JsonPlayerChatProperties.loadFromChatDirectory(path, logger);
        List<JsonMessageData> jsonMessageData = JsonMessageData.loadFromChatDirectory(path);

        return new JsonChatData(jsonChatMetaData, jsonMessageData, jsonPlayerChatProperties);
    }

    static Path getMessagesFile(Path path) {
        return path.resolve("messages.dat");
    }

    static Path getMetaDataFile(Path path) {
        return path.resolve("metadata.json");
    }

    static Path getPlayerChatPropertyFile(Path path, UUID playerId) {
        return getPlayerChatPropertiesDirectory(path).resolve(playerId.toString() + ".json");
    }

    static Path getPlayerChatPropertiesDirectory(Path path) {
        return path.resolve("members");
    }

    private static class JsonChatData implements ChatData {

        private final JsonChatMetaData metaData;
        private final Set<JsonPlayerChatProperties> playerChatProperties;
        private final List<JsonMessageData> messages;

        public JsonChatData(JsonChatMetaData metaData, List<JsonMessageData> messages, Set<JsonPlayerChatProperties> playerChatProperties) {
            this.metaData = metaData;
            this.messages = new ArrayList<>(messages);
            this.playerChatProperties = new HashSet<>(playerChatProperties);
        }

        public JsonChatData(Chat chat) {

            this.metaData = new JsonChatMetaData(chat);

            this.messages = new ArrayList<>();
            chat.getMessages()
                .forEach(message -> messages.add(new JsonMessageData(message)));

            this.playerChatProperties = new HashSet<>();
            chat.getMembers()
                .forEach(member -> playerChatProperties.add(new JsonPlayerChatProperties(member)));
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
            return Set.of();
        }

        @Override
        public List<ChatMessage> getMessages(Chat chat) {
            return List.of();
        }
    }

    public static class JsonChatMetaData {

        public static Codec<JsonChatMetaData> CODEC = BuilderCodec.builder(JsonChatMetaData.class, JsonChatMetaData::new)
                                                              .addField(new KeyedCodec<>("Id", Codec.UUID_STRING),
                                                                        JsonChatMetaData::setId,
                                                                        JsonChatMetaData::getId)
                                                              .addField(new KeyedCodec<>("ChatType", Codec.STRING),
                                                                        JsonChatMetaData::setChatTypeString,
                                                                        JsonChatMetaData::getChatTypeString)
                                                              .addField(new KeyedCodec<>("Created", Codec.LONG),
                                                                        JsonChatMetaData::setCreated,
                                                                        JsonChatMetaData::getCreated)
                                                              .build();

        private UUID id;
        private ChatType chatType;
        private long created;

        private JsonChatMetaData() {}

        public JsonChatMetaData(Chat chat) {
            this.id = chat.getId();
            this.chatType = chat.getChatType();
            this.created = chat.getCreated();
        }

        public UUID getId() {
            return id;
        }

        private void setId(UUID id) {
            this.id = id;
        }

        public long getCreated() {
            return created;
        }

        private void setCreated(long created) {
            this.created = created;
        }

        public String getChatTypeString() {
            return chatType.name();
        }

        private void setChatTypeString(String chatType) {
            this.chatType = ChatType.valueOf(chatType);
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

        private UUID playerId;
        private long memberSince;
        private ChatRole role;

        private long lastRead;
        private DisplayUnreadProperty displayUnreadProperty;

        private JsonPlayerChatProperties() {
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

        private void setPlayerId(UUID playerId) {
            this.playerId = playerId;
        }

        public String getRole() {
            return role.name();
        }

        private void setRole(String role) {
            this.role = ChatRole.valueOf(role);
        }

        public long getMemberSince() {
            return memberSince;
        }

        private void setMemberSince(long memberSince) {
            this.memberSince = memberSince;
        }

        public String getDisplayUnreadPropertyString() {
            return displayUnreadProperty.name();
        }

        public DisplayUnreadProperty getDisplayUnreadProperty() {
            return displayUnreadProperty;
        }

        private void setDisplayUnreadProperty(String displayUnreadPropertyString) {
            this.displayUnreadProperty = DisplayUnreadProperty.valueOf(displayUnreadPropertyString);
        }

        private void setDisplayUnreadProperty(DisplayUnreadProperty displayUnreadProperty) {
            this.displayUnreadProperty = displayUnreadProperty;
        }

        public long getLastRead() {
            return lastRead;
        }

        private void setLastRead(long lastRead) {
            this.lastRead = lastRead;
        }

        public PlayerChatProperties toPlayerChatProperties(Chat chat) {
            return new PlayerChatProperties(chat, playerId, memberSince, role, lastRead, displayUnreadProperty);
        }

        public static Set<JsonPlayerChatProperties> loadFromChatDirectory(Path path, HytaleLogger logger) throws IOException {

            Path playerChatPropertiesDirectory = getPlayerChatPropertiesDirectory(path);
            Set<JsonPlayerChatProperties> jsonPlayerChatPropertySet = new HashSet<>();
            try (DirectoryStream<Path> playerChatPropertyFiles = Files.newDirectoryStream(playerChatPropertiesDirectory, "*.json")) {
                for (Path playerChatPropertyFile : playerChatPropertyFiles) {
                    JsonPlayerChatProperties jsonPlayerChatProperties = RawJsonReader.readSync(playerChatPropertyFile,
                                                                                               JsonPlayerChatProperties.CODEC,
                                                                                               logger);
                    jsonPlayerChatPropertySet.add(jsonPlayerChatProperties);
                }
            }

            return jsonPlayerChatPropertySet;
        }
    }

    public static class JsonMessageData {

        private JsonMessageData() {}

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

        private UUID id;
        private UUID senderId;
        private String message;
        private Long timestamp;

        private void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public JsonMessageData(ChatMessage message) {
            this.id = message.id();
            this.senderId = message.senderId();
            this.message = message.message();
            this.timestamp = message.timestamp();
        }

        public ChatMessage toChatMessage(Chat chat) {
            return new ChatMessage(chat, id, senderId, message, timestamp);
        }

        public Long getTimestamp() {
            return timestamp;
        }

        private void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        private void setSenderId(UUID senderId) {
            this.senderId = senderId;
        }

        public UUID getSenderId() {
            return senderId;
        }

        private void setId(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }

        public static List<JsonMessageData> loadFromChatDirectory(Path path) throws IOException {

            List<JsonMessageData> messages = new ArrayList<>();
            Path messagesFile = JsonChatDataLoader.getMessagesFile(path);
            try (FileReader fileReader = new FileReader(messagesFile.toFile())) {
                List<String> lines = fileReader.readAllLines();
                for (String line : lines) {

                    JsonMessageData decode = JsonMessageData.CODEC.decode(new BsonString(line));
                    messages.add(decode);
                }
            }
        }
    }
}
