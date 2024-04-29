package bittorrent.bencode.handler;

import bittorrent.bencode.handler.model.DownloadTorrentPiece;
import bittorrent.bencode.handler.model.TorrentStateModel;
import bittorrent.bencode.util.TorrentUtil;
import com.sun.tools.javac.Main;
import lombok.SneakyThrows;
import okhttp3.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DownloadCommandHandler implements CommandHandler {
    static List<String> fileNames = new ArrayList<>();
    static List<Long> lengths = new ArrayList<>();
    Map<Integer, byte[]> pieceManager;
    Set<Integer> portUsed = new HashSet<>() ;

    public DownloadCommandHandler(){}
    public DownloadCommandHandler(Map<Integer, byte[]> pieceManager){
        this.pieceManager = pieceManager;
    }
    private void updateStatus(String bitfield){
        Integer port = 2003;
        String address = "127.0.0.1";
//        String bitfield = "1111";
        String host = "http://localhost:8080/tracker/status";
        String json = "{\"address\": \"" + address + "\", \"port\": " + port + ", \"bitfield\": \"" + bitfield + "\"}";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.Builder()
                .post(requestBody) // Set the request method as POST
                .url(host)
                .build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
//            System.out.println(response.isSuccessful());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean uploadRequest(int port){
        String address = "127.0.0.1";
        String host = "http://localhost:8080/tracker/updateUpload";
        String json = "{\"port\": " + port + "}";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.Builder()
                .post(requestBody)
                .url(host)
                .build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            return response.isSuccessful();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean downloadRequest(int port){
        String address = "127.0.0.1";
        String host = "http://localhost:8080/tracker/updateDownload";
        String json = "{\"port\": " + port + "}";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.Builder()
                .post(requestBody)
                .url(host)
                .build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            return response.isSuccessful();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleInfo(Map<String, Object> resultMap){
        Map<String, Object> infoMap = (Map<String, Object>) resultMap.get("info");
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
            fileNames.add(dir);
            lengths.add((Long) map.get("length"));
            System.out.println(dir + " - length: " + map.get("length"));
        }
    }
    @Override
    public void handle(String[] args) {
        String outputFilename = args[2];
        String torrentFileName = args[3];


        handleResult(TorrentUtil.decodeTorrentFile(torrentFileName), outputFilename);
        downloadRequest(2003);
        for(Integer item: portUsed){
            uploadRequest(item);
        }
        portUsed.clear();
        System.out.println("Downloaded " + torrentFileName + " to " + outputFilename);
    }



    @SneakyThrows
    private void handleResult(Map<String, Object> resultMap, String outputFileName) {
        TorrentStateModel torrentStateModel = TorrentUtil.getTorrentInfo(resultMap, true);
        handleInfo(resultMap);
        Map<Integer, DownloadTorrentPiece> pieceIdxToThread = new HashMap<>();
        int updateStatusCounter = 10;
        for (int pieceIdx = 0; pieceIdx < torrentStateModel.getNumPieces(); pieceIdx++) {
            torrentStateModel.updatePeers(pieceIdx);
            Random random = new Random();
            int randomNumber = random.nextInt(torrentStateModel.getPeers().size());
            InetSocketAddress peer = torrentStateModel.getPeers().get(randomNumber);
            portUsed.add(peer.getPort());
            DownloadTorrentPiece t = new DownloadTorrentPiece(peer.getAddress().getHostAddress(), String.valueOf(peer.getPort()), torrentStateModel, pieceIdx, pieceManager);
            pieceIdxToThread.put(pieceIdx, t);
            t.sendHandshake();
            t.getPiece();
//            t.join();

//            System.out.println("-----------" + pieceIdx);
            updateStatusCounter--;
            if(updateStatusCounter == 0){
                String bitfield = "";
                for(int i = 0; i < torrentStateModel.getNumPieces(); i++){
                    if(pieceIdxToThread.containsKey(i)){
                        bitfield += "1";
                    } else {
                        bitfield += "0";
                    }
                }
                updateStatus(bitfield);
                updateStatusCounter = 10;
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(t.getPieceBytes(), 0, t.getPieceBytes().length);

//            Thread.sleep(100);
        }

        // all pieces combined. write to a file
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for(int i = 0; i < pieceIdxToThread.size(); i++){
            byteArrayOutputStream.write(pieceIdxToThread.get(i).getPieceBytes(), 0, pieceIdxToThread.get(i).getPieceBytes().length);
        }
        byte[] concatenatedBytes = byteArrayOutputStream.toByteArray();
        int offset = 0;
        for (int i = 0; i < fileNames.size(); i++) {
            File outputFile = new File("output" + File.separator + fileNames.get(i));
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(concatenatedBytes, offset,(int)(long)lengths.get(i));
            offset += lengths.get(i);
//            fos.flush();
            fos.close();
        }


    }

}
