package de.feli490.hytale.privatechats.utils;

import com.hypixel.hytale.server.core.Message;
import java.awt.Color;

public class MessageUtils {

    public static final Color MAIN_COLOR = Color.GREEN;
    public static final Color SECONDARY_COLOR = Color.WHITE;

    private MessageUtils() {}

    public static Message main(String message) {
        return format(message, MAIN_COLOR);
    }

    public static Message secondary(String message) {
        return format(message, SECONDARY_COLOR);
    }

    public static Message error(String message) {
        return format(message, Color.RED);
    }

    public static Message format(String message, Color color) {
        return Message.raw(message)
                      .color(color);
    }

    public static Message format(String message, String color) {
        return Message.raw(message)
                      .color(color);
    }

    public static Message main(double value) {
        return format(value, MAIN_COLOR);
    }

    public static Message secondary(double value) {
        return format(value, SECONDARY_COLOR);
    }

    public static Message format(double value, Color color) {
        return format(format(value), color);
    }

    public static Message format(double value, String color) {
        return format(format(value), color);
    }

    public static Message main(int value) {
        return format(value, MAIN_COLOR);
    }

    public static Message secondary(int value) {
        return format(value, SECONDARY_COLOR);
    }

    public static Message format(int value, Color color) {
        return format(String.format("%d", value), color);
    }

    public static Message format(int value, String color) {
        return format(String.format("%d", value), color);
    }

    public static String format(double value) {
        return String.format("%.2f", value);
    }
}
