package bittorrent.bencode.handler.model.messages;

import java.io.DataOutputStream;
import java.io.IOException;

public class NotImplementedMessage implements Message {
    @Override
    public int length() {
        return 0;
    }

    @Override
    public void sendMessage(DataOutputStream outputStream) throws IOException {

    }

    @Override
    public Object getPayload() {
        return null;
    }

    @Override
    public MessageType getMessageType() {
        return null;
    }
}
