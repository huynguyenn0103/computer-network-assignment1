package bittorrent.bencode.coders;

import bittorrent.bencode.Decode;
import bittorrent.bencode.Encode;
import bittorrent.bencode.Print;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static bittorrent.bencode.util.InputStreamReaderUtil.peek;
import static bittorrent.bencode.util.InputStreamReaderUtil.poll;

public class DictionaryCoder extends Coder {
    private static final Gson gson = new Gson();

    @Override
    public Object decode(InputStream inputStream) {

        poll(inputStream); // to read first 'd'

        Map<String, Object> resultMap = new TreeMap<>();

//        result = DecodedResult.builder().result(resultMap).coder(this).build();

        boolean isKey = true;
        String key = "";

        Decode decode = new Decode(inputStream);

        while (peek(inputStream) != 'e') {
            Object o = decode.decode();
            if (isKey) {
                key = new String((byte[]) o, StandardCharsets.ISO_8859_1);
                isKey = false;
            } else {
                resultMap.put(key, o);
                isKey = true;
            }

        }
        poll(inputStream); // to read last 'e'

        return resultMap;
    }

    @Override
    public String encode(Object object, OutputStream outputStream) {
        if (object instanceof Map<?, ?> map) {
            try {
                outputStream.write("d".getBytes());
                TreeMap<?, ?> resultMap = new TreeMap<>(map);
                StringCoder stringCoder = new StringCoder();
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<?, ?> me : resultMap.entrySet()) {
                    stringBuilder.append(stringCoder.encode(me.getKey(), outputStream));
                    stringBuilder.append(new Encode(outputStream).encode(me.getValue()));
                }
                outputStream.write("e".getBytes());
                return "d" + stringBuilder + "e";
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
        throw new UnsupportedOperationException("Unsupported type " + object.getClass());
    }

    @Override
    public void print(Object object) {
        if (object instanceof Map<?, ?> resultMap) {
            System.out.print("{");
            int i = 0;
            for (Map.Entry<?, ?> me : resultMap.entrySet()) {
                if (i++ > 0) {
                    System.out.print(",");
                }
                System.out.print(gson.toJson(me.getKey()) + ":");
                new Print().print(me.getValue());
            }
            System.out.print("}");
            return;
        }
        throw new UnsupportedOperationException("Unsupported type " + object.getClass());
    }
}
