package bittorrent.bencode;

import bittorrent.bencode.coders.DictionaryCoder;
import bittorrent.bencode.coders.ListCoder;
import bittorrent.bencode.coders.NumericalCoder;
import bittorrent.bencode.coders.StringCoder;

public class Print {

    public void print(Object object) {


        if (object instanceof byte[]) {
            // string encoding
            new StringCoder().print(object);
            return;
        }
        if (object instanceof Long) {
            // integer encoding
            new NumericalCoder().print(object);
            return;
        }
        if (object instanceof java.util.List<?>) {
            // list encoding
            new ListCoder().print(object);
            return;
        }
        if (object instanceof java.util.Map<?, ?>) {
            // dictionary encoding
            new DictionaryCoder().print(object);
            return;
        }
        throw new RuntimeException("Unsupported format");
    }
}
