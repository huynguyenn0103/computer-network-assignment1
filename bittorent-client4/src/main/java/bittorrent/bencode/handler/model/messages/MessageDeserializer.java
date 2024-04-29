package bittorrent.bencode.handler.model.messages;

import java.io.DataInputStream;
import java.io.IOException;

public interface MessageDeserializer {
    Message deserialize(int payloadSize, DataInputStream dataInputStream) throws IOException;
}
