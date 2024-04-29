package com.example.bittorenttracker.Entity;

public class InfoResponse {
    private int id;
    private String address;
    private String bitfield;
    private int port;
    private String peerId;
    private int downloaded;
    private int uploaded;

    public InfoResponse(int id, String address, String bitfield, int port, String peerId, int downloaded, int uploaded) {
        this.address = address;
        this.bitfield = bitfield;
        this.port = port;
        this.peerId = peerId;
        this.downloaded = downloaded;
        this.uploaded = uploaded;
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBitfield() {
        return bitfield;
    }

    public void setBitfield(String bitfield) {
        this.bitfield = bitfield;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public int getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }

    public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
