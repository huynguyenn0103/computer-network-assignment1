package bittorrent.bencode.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashUtil {
    public static String shaHashHex(ByteArrayOutputStream outputStream) {
        try {
            byte[] digest = shaHash(outputStream);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] shaHashRaw(ByteArrayOutputStream outputStream) {
        try {
            return shaHash(outputStream);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] sha1(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] shaHash(ByteArrayOutputStream outputStream) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-1").digest(outputStream.toByteArray());
    }

}
