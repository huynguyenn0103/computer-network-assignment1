package bittorrent.bencode.handler;

import bittorrent.bencode.Decode;
import bittorrent.bencode.util.TorrentUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class InfoCommandHandler implements CommandHandler {
    static class BittorentFile{
        public String[] path;
        public int length;
    }
    @Override
    public void handle(String[] args) {
        String filename = args[1];

        File torrentFile = new File(filename);

        if (!torrentFile.exists()) {
            throw new RuntimeException("Torrent file does not exist");
        }

        try {
            FileInputStream fis = new FileInputStream(torrentFile);
            byte[] fileContent = fis.readAllBytes();
            fis.close();

            Object o = new Decode(fileContent).decode();
            handleResult(o);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

    private void handleResult(Object decodedObj) {
        if (decodedObj instanceof Map<?, ?> resultMap) {
            System.out.print("annouce: ");
            System.out.println(new String((byte[]) resultMap.get("announce"), StandardCharsets.ISO_8859_1));
            Map<String, Object> infoMap = (Map<String, Object>) resultMap.get("info");
            handleInfoHash(infoMap);
            handleInfoPieces(infoMap);
        }
    }

    private void handleInfoHash(Map<String, Object> infoMap) {
        System.out.println("info hash (id): " + TorrentUtil.getInfoHashHex(infoMap));
    }

    private void handleInfoPieces(Map<String, Object> infoMap) {
        System.out.println("info: ");
        // name
        byte[] name = (byte[]) infoMap.get("name");
        String str_name = new String(name, StandardCharsets.UTF_8);
        System.out.println("    name: " + str_name);

        // piece length
        System.out.println("    piece length: " + infoMap.get("piece length"));


        byte[] pieces = (byte[]) infoMap.get("pieces");
        // pieces
        System.out.println("    pieces:" + HexFormat.of().formatHex(pieces));
        for (int i = 0; i < pieces.length; i += 20) {
            byte[] piece = new byte[20];
            System.arraycopy(pieces, i, piece, 0, 20);

            System.out.println("        " + "[piece " + i/20 + "]" +  HexFormat.of().formatHex(piece));
        }

        List<Map<String, Object>> arrFiles = (List<Map<String, Object>>) infoMap.get("files");
        for(int i = 0; i < arrFiles.size(); i++){
            Map<String, Object> map = arrFiles.get(i);
            List<byte[]> pathList = (List<byte[]>)map.get("path");
            String dir = "";
            for(int j = 0; j < pathList.size(); j++){
                byte[] by = pathList.get(j);
                String by_string = new String(by, StandardCharsets.UTF_8);
                if(j == pathList.size() - 1){
                    dir += by_string;
                } else {
                    dir += by_string+ "/";
                }

            }
            System.out.println(dir + " - length: " + map.get("length"));
        }
    }

}
