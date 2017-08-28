package com.hehehey.ghost.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieIdentityComparator;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 8/28/2017.
 * Store cookie in a json file.
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class JsonCookieStore implements CookieStore {

    private static final Logger logger = Logger.getLogger(JsonCookieStore.class.getName());
    private static final Gson gson = new Gson();

    private TreeSet<Cookie> cookies;

    JsonCookieStore(String cookieFile) {
        if (cookieFile.trim().isEmpty())
            cookieFile = "cookie.json";

        try {
            cookies = gson.fromJson(new FileReader(cookieFile), new TypeToken<TreeSet<Cookie>>(){}.getType());
        }
        catch (Exception e) {
            logger.warning("Cookie read failed: " + e);
            logger.info("New cookie session.");
            cookies = new TreeSet<>(new CookieIdentityComparator());
        }
    }

    synchronized void save(String cookieFile) {
        if (cookieFile.trim().isEmpty())
            cookieFile = "cookie.json";

        try {
            new FileWriter(cookieFile, false).write(gson.toJson(cookies));
        }
        catch (Exception e) {
            logger.warning("Cookie save failed: " +e);
        }
    }

    @Override
    public synchronized void addCookie(final Cookie cookie) {
        if (cookie != null) {
            // first remove any old cookie that is equivalent
            cookies.remove(cookie);
            if (!cookie.isExpired(new Date())) {
                cookies.add(cookie);
            }
        }
    }

    @Override
    public synchronized List<Cookie> getCookies() {
        //create defensive copy so it won't be concurrently modified
        return new ArrayList<>(cookies);
    }

    @Override
    public synchronized boolean clearExpired(final Date date) {
        if (date == null) {
            return false;
        }
        boolean removed = false;
        for (final Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
            if (it.next().isExpired(date)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public synchronized void clear() {
        cookies.clear();
    }

    @Override
    public synchronized String toString() {
        return cookies.toString();
    }
}
