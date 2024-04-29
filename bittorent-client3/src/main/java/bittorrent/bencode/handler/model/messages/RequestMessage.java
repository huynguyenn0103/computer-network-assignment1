package bittorrent.bencode.handler.model.messages;

import java.io.DataOutputStream;
import java.io.IOException;

public class RequestMessage implements Message {

    int index;
    int begin;
    int length;

    public RequestMessage(int index, int begin, int length) {
        this.index = index;
        this.begin = begin;
        this.length = length;
    }

    @Override
    public int length() {
        return 13; // +1 for message id, +4 each for index, begin, length
    }

    @Override
    public void sendMessage(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(length());
        outputStream.write(getMessageType().getMessageId());
        outputStream.writeInt(index);
        outputStream.writeInt(begin);
        outputStream.writeInt(length);
    }

    @Override
    public Object getPayload() {
        return null;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.REQUEST_MESSAGE;
    }
}
