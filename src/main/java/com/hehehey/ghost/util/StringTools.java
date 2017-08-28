package com.hehehey.ghost.util;

/**
 * Created by Dick Zhou on 3/29/2017.
 *
 */
public class StringTools {

    public static String generateUniqueId() {
        return "i" + Long.toHexString(System.currentTimeMillis());
    }
}
