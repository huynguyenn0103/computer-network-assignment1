package bittorrent.bencode.coders;

import bittorrent.bencode.Decode;
import bittorrent.bencode.Encode;
import bittorrent.bencode.Print;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import static bittorrent.bencode.util.InputStreamReaderUtil.peek;
import static bittorrent.bencode.util.InputStreamReaderUtil.poll;

public class ListCoder extends Coder {

    @Override
    public Object decode(InputStream inputStream) {

        poll(inputStream); // to read first 'l'

        List<Object> resultList = new LinkedList<>();
        Decode decode = new Decode(inputStream);
        while (peek(inputStream) != 'e') {
            resultList.add(decode.decode());
        }
        poll(inputStream); // to read last 'e'
        return resultList;
    }

    @Override
    public String encode(Object object, OutputStream outputStream) {
        if (object instanceof List<?> list) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                outputStream.write("l".getBytes());
                for (Object result : list) {
                    stringBuilder.append(new Encode(outputStream).encode(result));
                }
                outputStream.write("e".getBytes());
                return "l" + stringBuilder + "e";
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new UnsupportedOperationException("Unsupported type " + object.getClass());
    }

    @Override
    public void print(Object object) {
        if (object instanceof List<?> list) {

            int i = 0;
            System.out.print("[");
            for (Object result : list) {
                if (i++ > 0) {
                    System.out.print(",");
                }
                new Print().print(result);
            }
            System.out.print("]");
            return;
        }
        throw new UnsupportedOperationException("Unsupported type " + object.getClass());
    }
}
