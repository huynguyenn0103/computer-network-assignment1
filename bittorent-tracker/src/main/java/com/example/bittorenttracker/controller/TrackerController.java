package com.example.bittorenttracker.controller;

import com.example.bittorenttracker.Entity.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tracker")
@CrossOrigin(origins = "*")
public class TrackerController {
//    static Map<Integer, String> map = new HashMap<>();
    static Map<Integer, PeerInfo> store = new HashMap<>();
    static {
        String str = "";
        for(int i = 0; i < 230; i++){
            str += "1";
        }
        PeerInfo peerInfo = new PeerInfo();
        try {
            peerInfo.address = InetAddress.getLocalHost();
            peerInfo.port = 2003;
            peerInfo.info_hash = "";
            peerInfo.uploaded = 0;
            peerInfo.downloaded = 1;
            peerInfo.peerId = "00000000000000000000";
            peerInfo.bitfield = str;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }


        store.put(2003, peerInfo);
    }
    private void printStore(){
        System.out.println("============================================");

        System.out.println("Store status: ");
        for (Integer key: store.keySet()){
            System.out.println("Port: " + store.get(key).port);
            System.out.println("[" + store.get(key).address.getHostAddress() + ":" + store.get(key).port + "] Info_hash: " + store.get(key).info_hash);
//            System.out.println(store.get(key).info_hash.compareTo("��=l�\u0013P�F�\u001A�WD>\u000B�!�"));
            System.out.println("[" + store.get(key).address.getHostAddress() + ":" + store.get(key).port + "] Uploaded: " + store.get(key).uploaded);
            System.out.println("[" + store.get(key).address.getHostAddress() + ":" + store.get(key).port + "] Downloaded: " + store.get(key).downloaded);
            System.out.println("[" + store.get(key).address.getHostAddress() + ":" + store.get(key).port + "] Peer id: " + store.get(key).peerId);
            System.out.println("[" + store.get(key).address.getHostAddress() + ":" + store.get(key).port + "] Bitfield: " + store.get(key).bitfield);
            System.out.println();
        }
        System.out.println("============================================");
        System.out.println();
    }
    @GetMapping("/info")
    public ResponseEntity<?> getAllInfo(){
        List<InfoResponse> list = new ArrayList<>();
        int i = 0;
        for (PeerInfo peer: store.values()) {
            list.add(new InfoResponse(i, peer.address.getHostAddress(), peer.bitfield, peer.port, peer.peerId, peer.downloaded, peer.uploaded));
            i++;
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/started")
    public boolean getStarted(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String port,
            @RequestParam(required = false) String info_hash,
            @RequestParam(required = false) String uploaded,
            @RequestParam(required = false) String downloaded,
            @RequestParam(required = false) String compact,
            @RequestParam(required = false) String peer_id) throws UnknownHostException {
        PeerInfo peerInfo = new PeerInfo();
        peerInfo.address = InetAddress.getLocalHost();
        peerInfo.port = Integer.parseInt(port);
        peerInfo.info_hash = info_hash;
        peerInfo.uploaded = Integer.parseInt(uploaded);
        peerInfo.downloaded = Integer.parseInt(downloaded);
        peerInfo.peerId = peer_id;
        store.put(peerInfo.port, peerInfo);
//        System.out.println("Started" + port);
        return true;
    }

    @GetMapping("")
    public List<TrackerResponse> getAllPeers(
            @RequestParam(required = false) String pieceRequiredIndex
            ) throws UnknownHostException {
        List<TrackerResponse> results = new ArrayList<>();
        if(pieceRequiredIndex != null){
            for(Integer key: store.keySet()){
                if(store.get(key).bitfield != null && store.get(key).bitfield.length() > Integer.parseInt(pieceRequiredIndex)){
                    if(store.get(key).bitfield.charAt(Integer.parseInt(pieceRequiredIndex)) == '1'){
                        TrackerResponse trackerResponse = new TrackerResponse();
                        trackerResponse.ip = store.get(key).address.getHostAddress();
                        trackerResponse.port = store.get(key).port;
                        trackerResponse.peerId = store.get(key).peerId;
                        results.add(trackerResponse);

                    }
                }
            }
        }
//        System.out.println(results.size() + "!!!!" + store.size());
//        System.out.println("Getall");
        printStore();
        return results;
    }

    @PostMapping ("/status")
    boolean updateStatus(@RequestBody RequestStatus requestStatus){
        PeerInfo peerInfo = store.get(requestStatus.port);
        peerInfo.bitfield = requestStatus.bitfield;
        store.put(requestStatus.port, peerInfo);
//        System.out.println("Port updated: " + requestStatus.port + ", bitfield: " + requestStatus.bitfield);
//        System.out.println("Status");
        printStore();
        return true;
    }

    @PostMapping ("/updateDownload")
    boolean updateDownload(@RequestBody Download requestLoad){
        PeerInfo peerInfoDownload = store.get(requestLoad.port);
        peerInfoDownload.downloaded += 1;
        store.put(requestLoad.port, peerInfoDownload);
//        System.out.println("[" + store.get(requestLoad.port).address.getHostAddress() + ":" + store.get(requestLoad.port).port + "] Download updated: " + store.get(requestLoad.port).downloaded);
        System.out.println("Upload");
        printStore();
        return true;
    }

    @PostMapping ("/updateUpload")
    boolean updateUpload(@RequestBody Upload requestLoad){
        PeerInfo peerInfoUpload = store.get(requestLoad.port);
        peerInfoUpload.uploaded += 1;
        store.put(requestLoad.port, peerInfoUpload);
//        System.out.println("[" + store.get(requestLoad.port).address.getHostAddress() + ":" + store.get(requestLoad.port).port + "] Upload updated: " + store.get(requestLoad.port).uploaded);
        System.out.println("Download");
        printStore();
        return true;
    }

    @PostMapping("/close")
    boolean deactivatePeer(@RequestBody Deactivate deactivate){
        store.remove(deactivate.port);
        return true;
    }
}
