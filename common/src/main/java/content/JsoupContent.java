package content;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dick Zhou on 3/28/2017.
 * Parse content of page using Jsoup.
 */
public class JsoupContent {
    private Document document;

    public JsoupContent(String htmlString, String baseUrl) {
        this.document = Jsoup.parse(htmlString, baseUrl);
    }

    public String[] parseLinks(String selector) {
        List<String> resultUrls = new ArrayList<>();

        for (Element element: document.select(selector)) {
            if (element.hasAttr("href")) {
                String href = element.attr("href");
                if (href.contains("#"))
                    resultUrls.add(href.substring(0, href.lastIndexOf("#")));
                else
                    resultUrls.add(href);
            }
        }

        return resultUrls.toArray(new String[0]);
    }
}
