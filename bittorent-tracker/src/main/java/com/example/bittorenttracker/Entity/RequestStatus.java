package com.example.bittorenttracker.Entity;

public class RequestStatus {
    public String address;
    public int port;
    public String bitfield;
    public RequestStatus(){}
    public RequestStatus(String address, int port, String bitfield){
        this.address = address;
        this.port = port;
        this.bitfield = bitfield;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBitfield() {
        return bitfield;
    }

    public void setBitfield(String bitfield) {
        this.bitfield = bitfield;
    }
}
