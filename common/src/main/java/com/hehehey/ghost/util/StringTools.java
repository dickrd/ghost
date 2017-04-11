package com.hehehey.ghost.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class StringTools {

    public static String generateUniqueId() {
        Base64.Encoder urlEncoder = Base64.getUrlEncoder();

        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return urlEncoder.encodeToString(bb.array());
    }
}
