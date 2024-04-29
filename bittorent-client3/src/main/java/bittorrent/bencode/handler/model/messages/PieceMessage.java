package bittorrent.bencode.handler.model.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//@AllArgsConstructor
@Getter
public class PieceMessage implements Message {

    int index;
    int begin;
    byte[] block;
    public PieceMessage (int index, int begin, byte[] block){
        this.block = new byte[16384];
        this.index = index;
        this.begin = begin;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(block, 0, block.length);
        this.block = byteArrayOutputStream.toByteArray();
    }

    @Override
    public int length() {
        return block.length + 1 + 8; // +1 for message id, +4 each for index, begin
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
        return MessageType.PIECE_MESSAGE;
    }
}
