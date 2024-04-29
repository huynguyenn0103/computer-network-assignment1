package bittorrent.bencode.handler;

import bittorrent.bencode.Decode;
import bittorrent.bencode.Print;
import bittorrent.bencode.handler.model.TorrentStateModel;
import bittorrent.bencode.util.TorrentUtil;
import okhttp3.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

public class PeersCommandHandler implements CommandHandler {
    @Override
    public void handle(String[] args) {
        String filename = args[1];

        handleResult(TorrentUtil.decodeTorrentFile(filename));

    }

    private void handleResult(Object decodedObj) {
        TorrentStateModel torrentStateModel = TorrentUtil.getTorrentInfo((Map<String, Object>) decodedObj, true);
        torrentStateModel.getPeers().forEach(isa -> System.out.println(isa.getAddress().getHostAddress() + ":" + isa.getPort()));
    }

}
