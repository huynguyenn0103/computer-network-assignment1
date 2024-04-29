package bittorrent.bencode.handler;

import bittorrent.bencode.Decode;
import bittorrent.bencode.Print;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public class DecodeCommandHandler implements CommandHandler {
    private static final Gson gson = new Gson();

    @Override
    public void handle(String[] args) {
        try {
            String bencodedValue = args[1];
            Object o = new Decode(bencodedValue).decode();
            new Print().print(o);
            System.out.println();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
