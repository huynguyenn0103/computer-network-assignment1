package bittorrent.bencode.handler.model.messages;

import java.io.DataOutputStream;
import java.io.IOException;

public class InterestedMessage implements Message {

    @Override
    public int length() {
        return 1; // +1 for message id
    }

    @Override
    public void sendMessage(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(length());
        outputStream.write(getMessageType().getMessageId());
    }

    @Override
    public Object getPayload() {
        return null;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.INTERESTED_MESSAGE;
    }
}
