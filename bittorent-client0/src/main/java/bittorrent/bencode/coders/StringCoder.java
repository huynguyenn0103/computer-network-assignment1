package bittorrent.bencode.coders;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static bittorrent.bencode.util.InputStreamReaderUtil.readUntil;

public class StringCoder extends Coder {

    private static final Gson gson = new Gson();

    @Override
    public Object decode(InputStream inputStream) {

        String lengthString = readUntil(inputStream, ':', false);

        int length = Integer.parseInt(lengthString);
        try {
            return inputStream.readNBytes(length);
//            new String(, StandardCharsets.ISO_8859_1);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public String encode(Object object, OutputStream outputStream) {
        if (object instanceof String str) {
            try {
                String encoded = str.length() + ":" + str;
                outputStream.write(encoded.getBytes());
                return encoded;
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
        if (object instanceof byte[] bytes) {
            try {
                byte[] length = String.valueOf(bytes.length).getBytes();
                outputStream.write(length);
                outputStream.write(":".getBytes());
                outputStream.write(bytes);
                return ((byte[]) object).length + ":" + new String((byte[]) object, StandardCharsets.ISO_8859_1);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
        throw new UnsupportedOperationException("Unsupported Object Type: " + object.getClass());
    }

    @Override
    public void print(Object object) {
        System.out.print(gson.toJson(new String((byte[]) object, StandardCharsets.ISO_8859_1)));
    }
}
