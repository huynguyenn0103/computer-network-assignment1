package bittorrent.bencode.util;

import bittorrent.bencode.Decode;
import bittorrent.bencode.Encode;
import bittorrent.bencode.handler.model.TorrentStateModel;
import com.google.gson.Gson;
import okhttp3.*;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TorrentUtil {
    public static String getInfoHashHex(Map<String, Object> infoMap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Encode(baos).encode(infoMap);
        return HashUtil.shaHashHex(baos);
    }

    public static byte[] getInfoHashRaw(Map<String, Object> infoMap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Encode(baos).encode(infoMap);
        return HashUtil.shaHashRaw(baos);
    }

    public static Map<String, Object> decodeTorrentFile(String filename) {
        File torrentFile = new File(filename);

        if (!torrentFile.exists()) {
            throw new RuntimeException("Torrent file does not exist");
        }

        try {
            FileInputStream fis = new FileInputStream(torrentFile);
            byte[] fileContent = fis.readAllBytes();
            fis.close();

            return (Map<String, Object>) new Decode(fileContent).decode();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static TorrentStateModel getTorrentInfo(Map<String, Object> resultMap, boolean isConnectedTracker) {

        @SuppressWarnings("unchecked")
        Map<String, Object> infoMap = (Map<String, Object>) resultMap.get("info");

//        String host = new String((byte[]) resultMap.get("announce"), StandardCharsets.ISO_8859_1);
        String host = "http://localhost:8080/tracker/started";
        byte[] infoHashRaw = TorrentUtil.getInfoHashRaw(infoMap);
        String urlEncodedHash = URLEncoder.encode(new String(infoHashRaw, StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1);

        TorrentStateModel torrentStateModel = TorrentStateModel
                .builder()
                .torrentMap(resultMap)
                .infoHashRaw(infoHashRaw)
                .infoMap(infoMap)
                .trackerUrl(host)
                .urlEncodedHash(urlEncodedHash)
                .build();
        if(isConnectedTracker){
            Request request = new Request.Builder()
                    .get()
                    .url(
                            HttpUrl.parse(host)
                                    .newBuilder()
                                    .addEncodedQueryParameter("info_hash", urlEncodedHash)
                                    .addQueryParameter("peer_id", "00000000000000000003")
                                    .addQueryParameter("port", "2006")
                                    .addQueryParameter("uploaded", "0")
                                    .addQueryParameter("downloaded", "0")
                                    .addQueryParameter("compact", "0")
                                    .build()
                    )
                    .build();

            try {
                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                ResponseBody body = response.body();
//            InputStream inputStream = new ByteArrayInputStream(body.byteStream().readAllBytes());
//
//            @SuppressWarnings("unchecked")
//            Map<String, Object> decodedMap = (Map<String, Object>) new Decode(inputStream).decode();
//
//            byte[] peers = (byte[]) decodedMap.get("peers");
//
//            List<InetSocketAddress> socketAddresses = new LinkedList<>();
//
//            for (int i = 0; i < peers.length; i += 6) {
//                byte[] peer = new byte[4];
//                byte[] portBytes = new byte[2];
//                System.arraycopy(peers, i, peer, 0, 4);
//                System.arraycopy(peers, i + 4, portBytes, 0, 2);
//                int port = ((portBytes[0] & 0xFF) << 8) | (portBytes[1] & 0xFF);
//                InetAddress ia = InetAddress.getByAddress(peer);
//                InetSocketAddress isa = new InetSocketAddress(ia, port);
//                socketAddresses.add(isa);
//            }
//            Gson gson = new Gson();
//            Integer[] array = gson.fromJson(body.string(), Integer[].class);
//            List<InetSocketAddress> socketAddresses = new LinkedList<>();
//            for(int i = 0; i < array.length; i++){
//                socketAddresses.add(new InetSocketAddress(InetAddress.getLocalHost(), array[i]));
//            }
//            torrentStateModel.setPeers(socketAddresses);
                return torrentStateModel;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return torrentStateModel;
        }
    }


}
