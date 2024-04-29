package bittorrent.bencode.handler.model;

import bittorrent.bencode.handler.model.messages.*;
import bittorrent.bencode.util.HashUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Getter
public class DownloadTorrentPiece extends Thread {
    String peerAddress;
    int peerPort;
    byte[] rawPeerId; // useless
    String peerId;     // useless
    TorrentStateModel torrentStateModel;

    Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private final int pieceIdx;
    private byte[] pieceBytes;
    Map<Integer, byte[]> pieceManager;


    public DownloadTorrentPiece(String peerAddress, String peerPort, TorrentStateModel torrentStateModel, int pieceIdx) {
        this.peerAddress = peerAddress;
        this.peerPort = Integer.parseInt(peerPort);
        this.torrentStateModel = torrentStateModel;
        this.pieceIdx = pieceIdx;
    }

    public DownloadTorrentPiece(String peerAddress, String peerPort, TorrentStateModel torrentStateModel, int pieceIdx, Map<Integer, byte[]> pieceManager) {
        this.peerAddress = peerAddress;
        this.peerPort = Integer.parseInt(peerPort);
        this.torrentStateModel = torrentStateModel;
        this.pieceIdx = pieceIdx;
        this.pieceManager = pieceManager;
    }

    @SneakyThrows
    @Override
    public void run() {
        sendHandshake();
        getPiece();
    }

    public void sendHandshake(){
        try {
            clientSocket = new Socket(peerAddress, peerPort);
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            outputStream.write(19);
            outputStream.write("BitTorrent protocol".getBytes());
            outputStream.write(new byte[8]);
            outputStream.write(torrentStateModel.infoHashRaw);
            outputStream.write("00000000000000000001".getBytes());
            outputStream.flush();
            inputStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            while (true){
                if(inputStream.available() == 68){
                    byte[] handshakeResponse = new byte[68];
                    inputStream.read(handshakeResponse);
                    rawPeerId = new byte[20];
                    System.arraycopy(handshakeResponse, 48, rawPeerId, 0, 20);
                    peerId = new String(rawPeerId, StandardCharsets.UTF_8);
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean receiveBitField_5() throws InterruptedException {
        Thread.sleep(1);
        final var bitfieldMessage = readMessage();
        if (!(bitfieldMessage instanceof BitfieldMessage)) {
            System.out.println("[Download] => Can not receive bitfield");
            return false;
        } else {
            System.out.println("[Download] => Bitfield status: " + bitfieldMessage.getPayload());
            if(((String) bitfieldMessage.getPayload()).charAt(pieceIdx) == '1'){
                return true;
            }
            System.out.println("[Download] => Piece" + pieceIdx +  " needed does not exists");
            return false;
        }
    }

    private boolean sendInterested_2() throws InterruptedException {
        Thread.sleep(1);
        sendMessage(new InterestedMessage());
        while (true) {
            Message message = readMessage();
            if (message instanceof UnchokeMessage) {
                System.out.println("[Download] => Peek is unchoked");
                return true;
            }
            System.err.println("[Download] => Peer is choked");
            Thread.sleep(1000);
        }
    }

    private byte[] sendPieceRequest_6() throws InterruptedException {
        // Define piece length
        Long pieceLength = (Long) torrentStateModel.getInfoMap().get("piece length");
        if (pieceIdx == torrentStateModel.getNumPieces() - 1) {
            pieceLength = torrentStateModel.getTotalLength() % pieceLength;
        }

        // send ceil(pieceLength/16384) Request messages
        double blockLength = 16384;
        int totalNumBlocks = (int) Math.ceil(pieceLength / blockLength);
        for (int blockIdx = 0; blockIdx < totalNumBlocks; blockIdx++) {
            int currBlockLength = (int) blockLength;
            if (blockIdx == totalNumBlocks - 1) {
                currBlockLength = (int) (pieceLength % 16384);
                if (currBlockLength == 0) currBlockLength = (int) blockLength;
            }
            sendMessage(new RequestMessage(pieceIdx, (int) (blockIdx * blockLength), currBlockLength));
        }
        Thread.sleep(1);
        // receive piece messages
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        for (int blockIdx = 0; blockIdx < totalNumBlocks; blockIdx++) {
            Message message = readMessage();
            if (!(message instanceof PieceMessage pieceMessage)) {
                blockIdx--;
                continue;
            }

            byteArrayOutputStream.write(pieceMessage.getBlock(), 0, pieceMessage.getBlock().length);
            pieceManager.put(pieceIdx* 4 + blockIdx, pieceMessage.getBlock());
//            System.arraycopy(pieceMessage.getBlock(), 0, pieceBytes, pieceMessage.getBegin(), pieceMessage.getBlock().length);
        }
        System.out.println("[Download] => Received piece successfully");
        return byteArrayOutputStream.toByteArray();
    }
    public byte[] getPiece() {
        try {
            System.out.println("[Download] Piece index: " + pieceIdx);
//            clientSocket = new Socket(peerAddress, peerPort);
//            outputStream = new DataOutputStream(clientSocket.getOutputStream());
//            inputStream = new DataInputStream(clientSocket.getInputStream());

            boolean isBitfieldSuccess = receiveBitField_5();
            boolean isInterestedSuccess = sendInterested_2();
            byte[] pieceBytes = sendPieceRequest_6();

            // compare with piece hash value
            byte[] receivedPieceHash = HashUtil.sha1(pieceBytes);
//            System.out.println(receivedPieceHash);
            byte[] originalPieceHash = torrentStateModel.getPieceHash(pieceIdx);
//            System.out.println(originalPieceHash);
            if (!Arrays.equals(receivedPieceHash, originalPieceHash) && pieceIdx != torrentStateModel.getNumPieces()-1) {
                throw new RuntimeException("[Download] => Piece hash does not match. Data integrity compromised.");
            } else {
                System.out.println("[Download] => Piece " + pieceIdx + " downloaded and check hashed successfully from peer " + peerAddress + ":" + peerPort);
            }
            System.out.println();

            this.pieceBytes = pieceBytes;
            return pieceBytes;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void getAndSavePiece(String outputFileName) {
        byte[] pieceBytes = getPiece();
        writeToFile(outputFileName, pieceBytes);
        System.out.println("Piece " + pieceIdx + " downloaded to " + outputFileName);

    }

    @SneakyThrows
    private void writeToFile(String outputFileName, byte[] pieceBytes) {
        FileOutputStream fos = new FileOutputStream(outputFileName);
        fos.write(pieceBytes);
        fos.close();
    }

    @SneakyThrows
    private Message readMessage() {
        int payloadSize = getMessageLength() - 1;
        int messageId = getMessageId();

        MessageType messageType = MessageType.valueOf(messageId);
        return messageType.getDeserializer().deserialize(payloadSize, inputStream);
    }

    @SneakyThrows
    private void sendMessage(Message message) {
        message.sendMessage(outputStream);
    }



    @SneakyThrows
    private int getMessageLength() {
        int value = readIntFromStream(4);
//        System.out.println("Message length: " + value);
        return value;
    }

    @SneakyThrows
    private int getMessageId() {
        int value = readIntFromStream(1);
//        System.out.println("Message id: " + value);
        return value;
    }

    @SneakyThrows
    private int readIntFromStream(int numBytes) {
        byte[] bytes = new byte[numBytes];
        inputStream.read(bytes);
        Thread.sleep(1);
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xFF);
        }
        return value;

    }

}
