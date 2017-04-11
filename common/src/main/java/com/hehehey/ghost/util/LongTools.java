package com.hehehey.ghost.util;

import java.security.SecureRandom;

/**
 * Created by Dick Zhou on 4/11/2017.
 *
 */
public class LongTools {

    private static SecureRandom random = new SecureRandom();

    public static long notDecrease(long number, long bound) {
        number = number + (random.nextLong() % number);

        if (number > bound)
            return bound;
        else
            return number;
    }

    public static long increase(long number, long bound) {
        number = number + number;

        if (number > bound)
            return bound;
        else
            return number;
    }
}
