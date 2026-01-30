package de.feli490.hytale.hyfechats.data.json;

import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.logger.HytaleLogger;
import de.feli490.hytale.hyfechats.data.ChatData;
import de.feli490.hytale.hyfechats.data.ChatDataLoader;
import de.feli490.hytale.hyfechats.data.json.dataobjects.JsonChatData;
import de.feli490.hytale.hyfechats.data.json.dataobjects.JsonChatMetaData;
import de.feli490.hytale.hyfechats.data.json.dataobjects.JsonMessageData;
import de.feli490.hytale.hyfechats.data.json.dataobjects.JsonPlayerChatProperties;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonChatDataLoader implements ChatDataLoader {

    private final Path directory;
    private final HytaleLogger logger;

    public JsonChatDataLoader(HytaleLogger logger, Path path) throws IOException {

        this.logger = logger.getSubLogger("JsonChatDataLoader");
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

        JsonChatFolderContainer jsonChatFolderContainer = new JsonChatFolderContainer(path);

        JsonChatMetaData jsonChatMetaData = RawJsonReader.readSync(jsonChatFolderContainer.getMetaDataFile(),
                                                                   JsonChatMetaData.CODEC,
                                                                   logger);
        Set<JsonPlayerChatProperties> jsonPlayerChatProperties = JsonPlayerChatProperties.loadFromChatDirectory(jsonChatFolderContainer,
                                                                                                                logger);
        List<JsonMessageData> jsonMessageData = JsonMessageData.loadFromChatDirectory(jsonChatFolderContainer);

        return new JsonChatData(jsonChatMetaData, jsonMessageData, jsonPlayerChatProperties);
    }
}
