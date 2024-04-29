package bittorrent.bencode.handler.model.messages;

import lombok.SneakyThrows;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Message {
    int length();
    void sendMessage(DataOutputStream outputStream) throws IOException;
    Object getPayload();

    MessageType getMessageType();
}
