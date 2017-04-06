package com.hehehey.ghost.source;

import com.hehehey.ghost.content.JsoupContent;
import com.hehehey.ghost.content.RegexContent;
import com.hehehey.ghost.util.HttpClient;
import com.hehehey.ghost.content.ParseMethod;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/28/2017.
 * Download result from search engines and parse the links.
 */
public class SearchSource {

    private static final Logger logger = Logger.getLogger(SearchSource.class.getName());

    private SearchEngine engines[];
    private HttpClient client;

    public SearchSource(SearchEngine[] engines) {
        this.engines = engines;

        client = new HttpClient();
    }

    public String[] searchAll(String keyword) {
        List<String> results = new ArrayList<>();
        for (SearchEngine engine: engines) {
            logger.log(Level.INFO, "Searching " + engine.name);
            try {
                String aResult = searchResultOf(keyword, engine);
                results.addAll(Arrays.asList(parseResult(aResult, engine)));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Engine failed: " + engine.name, e);
            }
        }
        return results.toArray(new String[0]);
    }

    private String searchResultOf(String keyword, SearchEngine engine) throws IOException{
        String requestUrl = String.format(engine.queryUrl, URLEncoder.encode(keyword, engine.charset));
        return client.getAsString(requestUrl, engine.charset);
    }

    private String[] parseResult(String htmlString, SearchEngine engine) {
        switch (engine.method.getType()) {
            case jsoup:
                JsoupContent jsoupContent = new JsoupContent(htmlString, engine.queryUrl);
                return jsoupContent.parseLinks(engine.method.getData());
            case regex:
                RegexContent regexContent = new RegexContent(htmlString, engine.queryUrl);
                return regexContent.parseLinks(engine.method.getData(), engine.method.getStrip());
            default:
                logger.log(Level.INFO, "No matching parse method: " + engine.method.getType());
                return new String[0];
        }
    }

    public class SearchEngine {
        String name;
        String queryUrl;
        String charset;
        ParseMethod method;
    }
}
