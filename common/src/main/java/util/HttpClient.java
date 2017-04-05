package util;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by Dick Zhou on 3/28/2017.
 * Handles http request and sessions.
 */
public class HttpClient {

    private static final int chunkSize = 1024;
    private static final Charset defaultCharset = StandardCharsets.UTF_8;
    private static final boolean useCaches = false;
    private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
    private static final String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    private static final String acceptLanguage = "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,zh-TW;q=0.2";

    public HttpClient() {
        newSession();
    }

    public void newSession() {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }

    public String getAsString(String urlString) throws IOException {
        return getAsString(urlString, defaultCharset.name());
    }

    public String getAsString(String urlString, String charset) throws IOException {
        URLConnection connection = new URL(urlString).openConnection();

        // Set connection properties.
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Accept", accept);
        connection.setRequestProperty("Accept-Language", acceptLanguage);
        connection.setRequestProperty("Accept-Charset", charset);
        //connection.setRequestProperty("Cookie", formatCookie());
        connection.setUseCaches(useCaches);
        connection.connect();

        // Read response byte.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream inputStream = connection.getInputStream();
        byte[] chunk = new byte[chunkSize];
        int readSize;
        while (( readSize = inputStream.read(chunk)) > 0)
        {
            baos.write(chunk, 0, readSize);
        }
        inputStream.close();
        return baos.toString(charset);
    }
}
