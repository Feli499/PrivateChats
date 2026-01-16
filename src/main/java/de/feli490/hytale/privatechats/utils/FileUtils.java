package de.feli490.hytale.privatechats.utils;

import java.io.FileWriter;
import java.nio.file.Path;

public class FileUtils {

    private FileUtils() {}

    public static Path loadOrCreateEmptyJson(Path filePath) {
        var file = filePath.toFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
                var writer = new FileWriter(file);
                writer.write("{}");
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file.toPath();
    }
}
