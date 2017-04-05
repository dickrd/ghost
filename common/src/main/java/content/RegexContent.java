package content;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dick Zhou on 4/5/2017.
 * Parse content using regex.
 */
public class RegexContent {
    private final String htmlString;
    private final String baseUrl;

    public RegexContent(String htmlString, String baseUrl) {
        this.htmlString = htmlString;

        if (baseUrl.contains("/"))
            this.baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
        else
            this.baseUrl = baseUrl + "/";
    }

    public String[] parseLinks(String regex, String strip) {
        List<String> results = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlString);

        while (matcher.find()) {
            results.add(matcher.group().replaceAll(strip, ""));
        }
        return results.toArray(new String[0]);
    }
}
