package com.hehehey.ghost.content;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dick Zhou on 4/5/2017.
 * Parse content using regex.
 */
public class RegexContent {

    private static final Logger logger = Logger.getLogger(RegexContent.class.getName());

    private final String htmlString;
    private final String baseUrl;

    public RegexContent(String htmlString, String baseUrl) {
        this.htmlString = htmlString;

        String host = "";
        try {
            URI uri = new URI(baseUrl);
            host = uri.getHost();
        } catch (Exception e) {
            logger.log(Level.INFO, "Base url failed, using empty string: " + baseUrl, e);
        }

        this.baseUrl = host;
    }

    public String[] parseLinks(String regex, String strip) {
        List<String> results = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlString);

        while (matcher.find()) {
            String url = matcher.group().replaceAll(strip, "");
            try {
                URI uri = new URI(url);
                if (uri.getHost() == null)
                    results.add(baseUrl + url);
                else
                    results.add(url);
            } catch (Exception e) {
                logger.log(Level.INFO, "Url format error: " + url);
            }
        }
        return results.toArray(new String[0]);
    }
}
