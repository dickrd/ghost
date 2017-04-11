package com.hehehey.ghost.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class SecurityUtil {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] md5(byte[] bytes) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(bytes);
    }

    public static String generateUniqueId() {
        Base64.Encoder urlEncoder = Base64.getUrlEncoder();

        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return urlEncoder.encodeToString(bb.array());
    }
}
