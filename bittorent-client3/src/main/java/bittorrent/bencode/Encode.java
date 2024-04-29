package bittorrent.bencode;

import bittorrent.bencode.coders.DictionaryCoder;
import bittorrent.bencode.coders.NumericalCoder;
import bittorrent.bencode.coders.ListCoder;
import bittorrent.bencode.coders.StringCoder;

import java.io.OutputStream;

public class Encode {

    OutputStream outputStream;

    public Encode(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public String encode(Object object) {

        if (object instanceof byte[]) {
            // string encoding
            return new StringCoder().encode(object, outputStream);
        }
        if (object instanceof Long) {
            // integer encoding
            return new NumericalCoder().encode(object, outputStream);
        }
        if (object instanceof java.util.List<?>) {
            // list encoding
            return new ListCoder().encode(object, outputStream);
        }
        if (object instanceof java.util.Map<?, ?>) {
            // dictionary encoding
            return new DictionaryCoder().encode(object, outputStream);
        }
        throw new RuntimeException("Unsupported format");
    }
}
