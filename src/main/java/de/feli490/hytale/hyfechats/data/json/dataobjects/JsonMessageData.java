package de.feli490.hytale.hyfechats.data.json.dataobjects;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.data.json.JsonChatFolderContainer;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bson.BsonString;

public class JsonMessageData {

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

    private JsonMessageData() {}

    public JsonMessageData(ChatMessage message) {
        this.id = message.id();
        this.senderId = message.senderId();
        this.message = message.message();
        this.timestamp = message.timestamp();
    }

    public String toJson() {
        return CODEC.encode(this, ExtraInfo.THREAD_LOCAL.get())
                    .toString();
    }

    public ChatMessage toChatMessage(Chat chat) {
        return new ChatMessage(chat, id, senderId, message, timestamp);
    }

    public Long getTimestamp() {
        return timestamp;
    }

    private void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    public UUID getSenderId() {
        return senderId;
    }

    private void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getId() {
        return id;
    }

    private void setId(UUID id) {
        this.id = id;
    }

    public static List<JsonMessageData> loadFromChatDirectory(JsonChatFolderContainer chatFolderContainer) throws IOException {

        List<JsonMessageData> messages = new ArrayList<>();
        Path messagesFile = chatFolderContainer.getMessagesFile();
        try (FileReader fileReader = new FileReader(messagesFile.toFile())) {
            List<String> lines = fileReader.readAllLines();
            for (String line : lines) {
                if (line.isBlank())
                    continue;
                JsonMessageData decode = JsonMessageData.CODEC.decode(new BsonString(line));
                messages.add(decode);
            }
        }
        return messages;
    }
}
