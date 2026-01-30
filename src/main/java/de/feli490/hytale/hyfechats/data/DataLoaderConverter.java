package de.feli490.hytale.hyfechats.data;

import de.feli490.hytale.hyfechats.chat.Chat;
import de.feli490.hytale.hyfechats.chat.ChatFactory;
import de.feli490.hytale.hyfechats.chat.ChatMessage;
import de.feli490.hytale.hyfechats.chat.PlayerChatProperties;
import de.feli490.utils.hytale.playerdata.EmptyPlayerDataProvider;
import java.io.IOException;
import java.util.Collection;

public class DataLoaderConverter {

    private final Deletable deletable;
    private final ChatDataLoader loaderToLoadFrom;
    private final ChatDataSaver saverToSaveTo;

    private DataLoaderConverter(ChatDataLoader loaderToLoadFrom, ChatDataSaver saverToSaveTo) {

        if (!(loaderToLoadFrom instanceof Deletable deletable))
            throw new IllegalArgumentException("Loader must be deletable");

        this.deletable = deletable;
        this.loaderToLoadFrom = loaderToLoadFrom;
        this.saverToSaveTo = saverToSaveTo;
    }

    public void convertIfDataExists() throws IOException {
        Collection<ChatData> chatData = loaderToLoadFrom.loadChats();
        if (chatData.isEmpty())
            return;

        ChatFactory chatFactory = new ChatFactory(new EmptyPlayerDataProvider());
        for (ChatData chatDatum : chatData) {
            Chat chat = chatFactory.fromChatData(chatDatum);
            saverToSaveTo.saveChatMetaData(chat);
            for (PlayerChatProperties member : chat.getMembers()) {
                saverToSaveTo.savePlayerChatProperties(member);
            }
            for (ChatMessage message : chat.getMessages()) {
                saverToSaveTo.saveMessage(message);
            }
        }
        deletable.deleteAllChats();
    }

    public static void convert(ChatDataLoader loaderToLoadFrom, ChatDataSaver saverToSaveTo) throws IOException {
        DataLoaderConverter dataLoaderConverter = new DataLoaderConverter(loaderToLoadFrom, saverToSaveTo);
        dataLoaderConverter.convertIfDataExists();
    }
}
