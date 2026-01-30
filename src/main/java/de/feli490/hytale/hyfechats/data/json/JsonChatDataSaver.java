package de.feli490.hytale.hyfechats.data.json;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.util.BsonUtil;
import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;
import de.feli490.hytale.hyfechats.data.ChatDataSaver;
import de.feli490.hytale.hyfechats.data.json.dataobjects.JsonChatMetaData;
import de.feli490.hytale.hyfechats.data.json.dataobjects.JsonMessageData;
import de.feli490.hytale.hyfechats.data.json.dataobjects.JsonPlayerChatProperties;
import de.feli490.utils.hytale.utils.FileUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class JsonChatDataSaver implements ChatDataSaver {

    private final HytaleLogger logger;
    private final Path chatFolder;

    public JsonChatDataSaver(HytaleLogger logger, Path path) throws IOException {

        this.logger = logger.getSubLogger("JsonChatDataSaver");
        this.chatFolder = path;

        if (!Files.exists(path))
            Files.createDirectories(path);

        if (!Files.isDirectory(path))
            throw new IllegalArgumentException("Path must be a directory");
    }

    @Override
    public void saveChatMetaData(Chat chat) throws IOException {

        JsonChatFolderContainer chatFolderContainer = getChatFolderContainer(chat);

        Path metaDataFile = chatFolderContainer.getMetaDataFile();
        FileUtils.loadOrCreateEmptyJson(metaDataFile);

        JsonChatMetaData jsonChatMetaData = new JsonChatMetaData(chat);
        BsonUtil.writeSync(metaDataFile, JsonChatMetaData.CODEC, jsonChatMetaData, logger);
    }

    @Override
    public void deleteChat(Chat chat) throws IOException {

        JsonChatFolderContainer chatFolderContainer = getChatFolderContainer(chat);
        FileUtils.deleteDirectoryRecursive(chatFolderContainer.getPath());
    }

    @Override
    public void saveMessage(ChatMessage message) throws IOException {

        Chat chat = message.chat();
        JsonChatFolderContainer chatFolderContainer = getChatFolderContainer(chat);

        Path messagesFile = chatFolderContainer.getMessagesFile();
        FileUtils.loadOrCreate(messagesFile);

        JsonMessageData jsonMessageData = new JsonMessageData(message);
        try (FileWriter fileWriter = new FileWriter(messagesFile.toFile())) {
            String json = jsonMessageData.toJson();
            fileWriter.append(json + "\n");
        }
    }

    @Override
    public void savePlayerChatProperties(PlayerChatProperties playerChatProperties) throws IOException {

        Chat chat = playerChatProperties.getChat();
        JsonChatFolderContainer chatFolderContainer = getChatFolderContainer(chat);

        UUID playerId = playerChatProperties.getPlayerId();
        Path playerChatPropertyFile = chatFolderContainer.getPlayerChatPropertyFile(playerId);
        FileUtils.loadOrCreateEmptyJson(playerChatPropertyFile);

        BsonUtil.writeSync(playerChatPropertyFile,
                           JsonPlayerChatProperties.CODEC,
                           new JsonPlayerChatProperties(playerChatProperties),
                           logger);
    }

    @Override
    public void deletePlayerChatProperties(PlayerChatProperties playerChatProperties) throws IOException {

        Chat chat = playerChatProperties.getChat();
        JsonChatFolderContainer chatFolderContainer = getChatFolderContainer(chat);

        UUID playerId = playerChatProperties.getPlayerId();
        Path playerChatPropertyFile = chatFolderContainer.getPlayerChatPropertyFile(playerId);
        Files.deleteIfExists(playerChatPropertyFile);
    }

    private JsonChatFolderContainer getChatFolderContainer(Chat chat) {
        return JsonChatFolderContainer.of(chatFolder, chat);
    }
}
