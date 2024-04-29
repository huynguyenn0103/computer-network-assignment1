package bittorrent.bencode.handler;

import bittorrent.bencode.handler.model.DownloadTorrentPiece;
import bittorrent.bencode.handler.model.TorrentStateModel;
import bittorrent.bencode.util.TorrentUtil;

import java.net.InetSocketAddress;
import java.util.HexFormat;
import java.util.Map;

public class DownloadPieceCommandHandler implements CommandHandler {
    @Override
    public void handle(String[] args) {
        String outputFilename = args[2];
        String filename = args[3];
        int pieceNum = Integer.parseInt(args[4]);

        handleResult(TorrentUtil.decodeTorrentFile(filename), outputFilename, pieceNum);

    }

    private void handleResult(Map<String, Object> resultMap, String outputFileName, int pieceNum) {
        TorrentStateModel torrentStateModel = TorrentUtil.getTorrentInfo(resultMap, true);

        InetSocketAddress peer = torrentStateModel.getPeers().get(0);

        downloadAndSavePieceFromPeer(peer, pieceNum, torrentStateModel, outputFileName);
    }

    public void downloadAndSavePieceFromPeer(InetSocketAddress peer, int pieceNum, TorrentStateModel torrentStateModel, String outputFileName) {
        DownloadTorrentPiece downloadTorrent = new DownloadTorrentPiece(peer.getAddress().getHostAddress(), String.valueOf(peer.getPort()), torrentStateModel, pieceNum);
        downloadTorrent.sendHandshake();

        downloadTorrent.getAndSavePiece(outputFileName);
    }

}
