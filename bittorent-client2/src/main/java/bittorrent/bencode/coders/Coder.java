package bittorrent.bencode.coders;


import java.io.InputStream;
import java.io.OutputStream;

public abstract class Coder {

    public abstract Object decode(InputStream inputStream);

    public abstract String encode(Object object, OutputStream outputStream);

    public abstract void print(Object object);

}
