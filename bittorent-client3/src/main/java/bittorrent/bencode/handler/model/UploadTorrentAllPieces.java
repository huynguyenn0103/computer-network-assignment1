package bittorrent.bencode.handler.model;

import bittorrent.bencode.util.TorrentUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//@Builder
@Setter
@Getter
public class UploadTorrentAllPieces{
    TorrentStateModel torrentStateModel;
    Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    static Map<Integer, byte[]> pieceManager;

    static List<byte[]> listOfPieces;
    static List<String> fileNames = new ArrayList<>();
    static List<Long> lengths = new ArrayList<>();

    public UploadTorrentAllPieces(TorrentStateModel torrentStateModel,Socket clientSocket, Map<Integer, byte[]> pieceManager) {
        this.torrentStateModel = torrentStateModel;
        this.clientSocket = clientSocket;
        this.pieceManager = pieceManager;
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
        }
    }
    public static void FolderToBytes(String folderPath){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            File folder = new File(folderPath);
            File[] files = folder.listFiles();
            for(int i = 0; i < files.length; i++){
                File file = new File("");
                for(int j = 0; j < files.length; j++){
                    if(fileNames.get(i).compareTo(files[j].getName()) == 0){
                        file = files[j];
                        break;
                    }
                }
                System.out.println(file.getName());
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    buffer = new byte[4096];
                }
                fis.close();
            }
            byte[] concatenatedBytes = byteArrayOutputStream.toByteArray();


            FileOutputStream fos = new FileOutputStream("concated_file");
            fos.write(concatenatedBytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<byte[]> bytesToPieces() throws IOException {
        List<byte[]> listsOfPiece = new ArrayList<>();
        String concatFile = "concated_file";
        FileInputStream fis = new FileInputStream(concatFile);
        byte[] buffer = new byte[16384];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(buffer, 0, bytesRead);
            listsOfPiece.add(buffer);
            buffer = new byte[16384];
        }
        fis.close();
        return listsOfPiece;
    }

    public static List<byte[]> setupPieces(){
        List<byte[]> listsOfPiece = new ArrayList<>();
        for(int i = 0; i < pieceManager.size(); i++){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(pieceManager.get(i), 0, pieceManager.get(i).length);
            listsOfPiece.add(byteArrayOutputStream.toByteArray());
        }
        return listsOfPiece;
    }

    private int readIntFromStream(int numBytes, DataInputStream inputStream) throws IOException {
        byte[] bytes = new byte[numBytes];
        inputStream.read(bytes);
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xFF);
        }
        return value;

    }
    public void receiveHandshake(){
        try {
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());

            byte[] handshakeRequest = new byte[68];
            while (true) {
                Thread.sleep(10);
                if(inputStream.available() == 68){
                    inputStream.read(handshakeRequest);
                    byte[] rawPeerId = new byte[20];
                    System.arraycopy(handshakeRequest, 48, rawPeerId, 0, 20);
                    String peerId = new String(rawPeerId, StandardCharsets.UTF_8);
                    System.out.println("[Upload] => Requested peer id: " + peerId);

                    outputStream.write(19);
                    outputStream.write("BitTorrent protocol".getBytes());
                    outputStream.write(new byte[8]);
                    outputStream.write(torrentStateModel.infoHashRaw);
                    outputStream.write("00000000000000000003".getBytes());
                    outputStream.flush();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private boolean sendBitfield_5() throws IOException, InterruptedException {
        Thread.sleep(10);
        String str = "";
        for(int i = 0; i < torrentStateModel.getNumPieces(); i++){
            if(i < pieceManager.size()){
                str = str + "1";
            } else {
                str = str + "0";
            }
        }
        outputStream.writeInt(torrentStateModel.getNumPieces() + 1);
        outputStream.write(5);
        outputStream.write(str.getBytes());
        outputStream.flush();
        System.out.println("[Upload] => Bitfield finish");
        return true;
    }

    private boolean receiveInterest_2() throws IOException, InterruptedException {
        Thread.sleep(10);
        int interest_len = readIntFromStream(4, inputStream);
        int interest_msg = readIntFromStream(1, inputStream);
//        System.out.println("[Upload] => Interest finish - " + "len: " + interest_len + " - msg_id: " + interest_msg);
        System.out.println("[Upload] => Interest finish");
        return true;
    }

    private boolean sendUnchoke_1() throws IOException, InterruptedException {
        Thread.sleep(10);
        outputStream.writeInt(1);
        outputStream.write(1);
        outputStream.flush();
        System.out.println("[Upload] => Unchoke finish");
        return true;
    }

    private void receive_send_piece_6() throws InterruptedException, IOException {
        int count = 0;
        while(true){
            Thread.sleep(1);
            int block_len = readIntFromStream(4, inputStream);
            int block_message = readIntFromStream(1, inputStream);
            int block_index = readIntFromStream(4, inputStream);
            int block_begin = readIntFromStream(4, inputStream);
            int block_length = readIntFromStream(4, inputStream);
//            System.out.println(block_len + " - " + block_message + " - " + block_index + " - " + block_begin + " - " + block_length );


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(listOfPieces.get(count + 4 * block_index) , 0, listOfPieces.get(count + 4 * block_index).length);
            outputStream.writeInt(1 + listOfPieces.get(count + 4 * block_index).length + 8);
            outputStream.write(7);
//            System.out.println("        Block " + (count + 4 * block_index) + " finished with len block: " + listOfPieces.get(count + 4 * block_index).length);
            outputStream.writeInt(block_index);
            outputStream.writeInt(block_begin);
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.flush();
            byteArrayOutputStream.close();
            Thread.sleep(1);
            if(listOfPieces.get(count + 4 * block_index).length != 16384 || ++count == 4){
                System.out.println("[Upload] => Piece " + block_index + " exchange finish");
                break;
            }
        }
    }

    public void sendPiece(){

        try {
            handleInfo(torrentStateModel.getTorrentMap());
            // Create file concated
//            FolderToBytes("output");

            // file concated -> listOfPieces
//            listOfPieces = bytesToPieces();
            listOfPieces = setupPieces();
//            System.out.println("**" + listOfPieces.size());

            // Handshake
            receiveHandshake();

            // Bitfield response
            sendBitfield_5();

            // Interest request
            receiveInterest_2();

            // Unchoke response
            sendUnchoke_1();

            // Block request
            receive_send_piece_6();
            System.out.println();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
