package de.feli490.hytale.privatechats.chat;

import java.util.UUID;

public record ChatMessage(UUID id, UUID senderId, String message) {

}
