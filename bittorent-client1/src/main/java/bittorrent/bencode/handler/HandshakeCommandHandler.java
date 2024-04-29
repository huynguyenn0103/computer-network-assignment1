package bittorrent.bencode.handler;

import bittorrent.bencode.handler.model.DownloadTorrentPiece;
import bittorrent.bencode.handler.model.TorrentStateModel;
import bittorrent.bencode.util.TorrentUtil;

import java.util.HexFormat;
import java.util.Map;

public class HandshakeCommandHandler implements CommandHandler {
    @Override
    public void handle(String[] args) {
        String filename = args[1];
        String peerAddress = args[2];

        Map<String, Object> decodedMap = TorrentUtil.decodeTorrentFile(filename);
        handleResult(decodedMap, peerAddress);
    }

    private void handleResult(Map<String, Object> resultMap, String peerAddress) {
        Map<String, Object> infoMap = (Map<String, Object>) resultMap.get("info");

        byte[] infoHashRaw = TorrentUtil.getInfoHashRaw(infoMap);

        String[] peerAddressParts = peerAddress.split(":");

        DownloadTorrentPiece handshakeState = new DownloadTorrentPiece(peerAddressParts[0], peerAddressParts[1], TorrentStateModel.builder().infoHashRaw(infoHashRaw).build(), 0);
        handshakeState.sendHandshake();
        System.out.println("Peer ID: " + handshakeState.getPeerId());
        handshakeState.close();

    }

}
