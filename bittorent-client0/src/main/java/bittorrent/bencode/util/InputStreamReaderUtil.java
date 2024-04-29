package bittorrent.bencode.util;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamReaderUtil {
    public static int peek(InputStream inputStream) {
        try {
            inputStream.mark(1);
            int read = inputStream.read();
            inputStream.reset();
            return read;
        } catch (IOException e) {
            return -1;
        }
    }

    public static int poll(InputStream inputStream) {
        try {
            return inputStream.read();
        } catch (IOException e) {
            return -1;
        }
    }

    public static String readUntil(InputStream inputStream, char end, boolean endInclusive) {
        StringBuilder stringBuilder = new StringBuilder();

        int ch;

        while ((ch = poll(inputStream)) != -1) {
            if (ch == end) {
                if (endInclusive) {
                    stringBuilder.append((char) ch);
                }
                break;
            }
            stringBuilder.append((char) ch);
        }

        return stringBuilder.toString();
    }

}
