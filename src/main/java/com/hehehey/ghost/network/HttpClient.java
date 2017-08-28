package com.hehehey.ghost.network;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dick Zhou on 3/28/2017.
 * Handles http request and sessions.
 */
public class HttpClient {

    private static final Charset defaultCharset = StandardCharsets.UTF_8;

    private static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
    private static final String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    private static final String acceptLanguage = "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4,zh-TW;q=0.2";

    private CloseableHttpClient httpclient;
    private JsonCookieStore cookieStore;

    public HttpClient() {
        this("");
    }

    public HttpClient(String cookieFile) {
        cookieStore = new JsonCookieStore(cookieFile);
        newSession();
    }

    public String getAsString(String urlString) throws IOException {
        return getAsString(urlString, defaultCharset.name(), new Header[0]);
    }

    public String getAsString(String urlString, String charset, Header[] headers) throws IOException {
        HttpEntity entity = connect(HttpMethod.get, urlString, headers, new byte[0]);
        return EntityUtils.toString(entity, charset);
    }

    public byte[] get(String urlString) throws IOException {
        HttpEntity entity = connect(HttpMethod.get, urlString, new byte[0]);
        return EntityUtils.toByteArray(entity);
    }

    public void saveCookie() {
        saveCookie("");
    }

    public void saveCookie(String cookieFile) {
        cookieStore.save(cookieFile);
    }

    private HttpEntity connect(HttpMethod method, String url, byte[] data) throws IOException {
        return connect(method, url, new Header[]{}, data);
    }

    private HttpEntity connect(HttpMethod method, String url, Header[] headers, byte[] data) throws IOException {
        CloseableHttpResponse response;
        switch (method) {
            case delete:
                HttpDelete httpDelete = new HttpDelete(url);
                httpDelete.setHeaders(headers);
                response = httpclient.execute(httpDelete);
                break;
            case get:
                HttpGet httpGet = new HttpGet(url);
                httpGet.setHeaders(headers);
                response = httpclient.execute(httpGet);
                break;
            case post:
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeaders(headers);
                httpPost.setEntity(new ByteArrayEntity(data));
                response = httpclient.execute(httpPost);
                break;
            case put:
                HttpPut httpPut = new HttpPut(url);
                httpPut.setHeaders(headers);
                httpPut.setEntity(new ByteArrayEntity(data));
                response = httpclient.execute(httpPut);
                break;
            default:
                throw new IOException("Unsupported method.");
        }

        return response.getEntity();
    }

    private void newSession() {
        this.cookieStore.clear();

        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

        // Validate connections after 1 sec of inactivity
        connManager.setValidateAfterInactivity(1000);
        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .build();
        // Configure the connection manager to use connection configuration either
        // by default or for a specific host.
        connManager.setDefaultConnectionConfig(connectionConfig);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);

        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD)
                .setExpectContinueEnabled(true)
                .build();

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("User-Agent", userAgent));
        headers.add(new BasicHeader("Accept", accept));
        headers.add(new BasicHeader("Accept-Language", acceptLanguage));
        headers.add(new BasicHeader("Accept-Charset", defaultCharset.name()));

        // Create an HttpClient with the given custom dependencies and configuration.
        httpclient = HttpClients.custom()
                .setDefaultHeaders(headers)
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(defaultRequestConfig)
                .setDefaultCookieStore(this.cookieStore)
                .build();
    }

    public enum HttpMethod {
        get,
        post,
        put,
        delete
    }
}
