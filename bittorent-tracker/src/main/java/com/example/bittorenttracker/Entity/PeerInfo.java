package com.example.bittorenttracker.Entity;

import java.net.InetAddress;

public class PeerInfo {
    public InetAddress address;
    public Integer port;
    public String info_hash;
    public Integer uploaded;
    public Integer downloaded;
    public String peerId;
    public String bitfield;

}
