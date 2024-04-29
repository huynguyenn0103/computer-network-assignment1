package bittorrent.bencode.coders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import static bittorrent.bencode.util.InputStreamReaderUtil.poll;
import static bittorrent.bencode.util.InputStreamReaderUtil.readUntil;

public class NumericalCoder extends Coder {

    @Override
    public Object decode(InputStream inputStream) {
        poll(inputStream); // to read the first 'i'
        String integerString = readUntil(inputStream, 'e', false);
        return Long.parseLong(integerString);
    }

    @Override
    public String encode(Object object, OutputStream outputStream) {
        if (object instanceof Long number) {
            try {
                String encoded = "i" + number + "e";
                outputStream.write(encoded.getBytes());
                return encoded;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new UnsupportedOperationException("Unsupported type " + object.getClass());
    }

    @Override
    public void print(Object object) {
        System.out.print(object);
    }
}
