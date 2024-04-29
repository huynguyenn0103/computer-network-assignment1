package bittorrent.bencode.handler.model;

import bittorrent.bencode.entity.TrackerResponse;
import com.google.gson.Gson;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Builder
@Setter
@Getter
public class TorrentStateModel {
    Map<String, Object> torrentMap;
    Map<String, Object> infoMap;
    String trackerUrl;
    byte[] infoHashRaw;
    String urlEncodedHash;
    List<InetSocketAddress> peers;
    private List<byte[]> pieces;
    public boolean updatePeers(int pieceIdx){
        String host = "http://localhost:8080/tracker";
        Request request = new Request.Builder()
                .get()
                .url(
                        HttpUrl.parse(host)
                                .newBuilder()
                                .addQueryParameter("pieceRequiredIndex", Integer.toString(pieceIdx))
                                .build()
                )
                .build();
        OkHttpClient client = new OkHttpClient();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            ResponseBody body = response.body();
            Gson gson = new Gson();
            TrackerResponse[] array = gson.fromJson(body.string(), TrackerResponse[].class);
            List<InetSocketAddress> socketAddresses = new LinkedList<>();
            for(int i = 0; i < array.length; i++){
                socketAddresses.add(new InetSocketAddress(InetAddress.getByName(array[i].ip), array[i].port));
            }
            this.peers = socketAddresses;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response.isSuccessful();

    }

    public int getNumPieces() {
        if (pieces != null) return this.pieces.size();
        this.pieces = new ArrayList<>();
        byte[] pieces = (byte[]) infoMap.get("pieces");
        for (int i = 0; i < pieces.length; i += 20) {
            byte[] piece = new byte[20];
            System.arraycopy(pieces, i, piece, 0, 20);
            this.pieces.add(piece);
        }
        return this.pieces.size();
    }

    public Long getTotalLength() {
        Long total = 0l;
        List<Map<String, Object>> arrFiles = (List<Map<String, Object>>) infoMap.get("files");
        for(int i = 0; i < arrFiles.size(); i++){
            Map<String, Object> map = arrFiles.get(i);
            total = total + (Long) map.get("length");
        }
        return total;
    }

    public byte[] getPieceHash(int pieceIdx) {
        if (pieces == null) getNumPieces();
        return this.pieces.get(pieceIdx);
    }

}
