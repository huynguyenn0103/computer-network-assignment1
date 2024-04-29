import bittorrent.bencode.handler.*;
import bittorrent.bencode.handler.model.TorrentStateModel;
import bittorrent.bencode.handler.model.UploadTorrentAllPieces;
import bittorrent.bencode.util.TorrentUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
// import com.dampcake.bittorrent.bencode.Bencode; - available if you need it!

public class Main {
    private static final int UPLOAD_PORT = 2003;
    public static Map<Integer, byte[]> pieceManager = new HashMap<>();


    static class ServerManager {
        private ExecutorService downloadExecutor;
        private ExecutorService uploadExecutor;
        String[] args;


        public ServerManager() {
            downloadExecutor = Executors.newFixedThreadPool(1); // Adjust the number of threads as needed
            uploadExecutor = Executors.newFixedThreadPool(2500); // Adjust the number of threads as needed
        }
        public ServerManager(String[] args) {
            downloadExecutor = Executors.newFixedThreadPool(1); // Adjust the number of threads as needed
            uploadExecutor = Executors.newFixedThreadPool(2500); // Adjust the number of threads as needed
            this.args = args;
        }

        public void startServers() {
            startDownloadServer();
            startUploadServer();
        }

        private void startDownloadServer() {
            Runnable downloadServer = () -> {
                    downloadExecutor.submit(new DownloadHandler(args));
            };
            Thread downloadThread = new Thread(downloadServer);
            downloadThread.start();
        }

        private void startUploadServer() {
            Runnable uploadServer = () -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(UPLOAD_PORT);
                    while (true) {

                        Socket clientSocket = serverSocket.accept();
                        uploadExecutor.submit(new UploadHandler(clientSocket));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Thread uploadThread = new Thread(uploadServer);
            uploadThread.start();
        }
    }
    static class DownloadHandler implements Runnable {
        private String[] args;

        DownloadHandler(String[] args){
            this.args = args;
        }

        @Override
        public void run() {
            // Handle download request
//            synchronized (Main.class) {
//                Main.x += 1;
//                System.out.println("Handle download " + x);
//            }
            String command = args[0];
            if(command.compareTo("") != 0){
                if ("decode".equals(command)) {
                    new DecodeCommandHandler().handle(args);
                } else if ("info".equals(command)) {
                    new InfoCommandHandler().handle(args);
                } else if ("peers".equals(command)) {
                    new PeersCommandHandler().handle(args);
                } else if ("handshake".equals(command)) {
                    new HandshakeCommandHandler().handle(args);
                } else if ("download_piece".equals(command)) {
                    new DownloadPieceCommandHandler().handle(args);
                } else if ("download".equals(command)) {
                    new DownloadCommandHandler(pieceManager).handle(args);
//                    System.out.println(pieceManager.size());
                } else {
                    System.out.println("Unknown command: " + command);
                }
            }
        }
    }

    static class UploadHandler implements Runnable {
        private Socket clientSocket;
        private TorrentStateModel torrentStateModel;
        UploadHandler(){}

        UploadHandler(Socket socket) {
            this.clientSocket = socket;
            this.torrentStateModel = TorrentUtil.getTorrentInfo(TorrentUtil.decodeTorrentFile("Hoso.torrent"), false);
        }


        @Override
        public void run() {
            // Handle upload request
//            synchronized (Main.class) {
//                Main.x += 1;
//                System.out.println("Handle upload " + x);
//            }
            UploadTorrentAllPieces upload = new UploadTorrentAllPieces(torrentStateModel, clientSocket, pieceManager);
            upload.sendPiece();
        }
    }
    public static void main(String[] args) throws Exception {
        ServerManager serverManager = new ServerManager(args);
        serverManager.startServers();
    }

}
