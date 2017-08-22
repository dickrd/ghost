import com.google.gson.Gson;
import com.hehehey.ghost.util.HttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 8/14/2017.
 *
 */
public class AutohomeClassBot {

    private static final Logger logger = Logger.getLogger(AutohomeClassBot.class.getName());
    private static final HttpClient httpClient = new HttpClient();
    private static final Gson gson = new Gson();

    private static final String URL_MICRO = "http://www.autohome.com.cn/a00/";
    private static final String URL_MINI = "http://www.autohome.com.cn/a0/";
    private static final String URL_COMPACT = "http://www.autohome.com.cn/a/";
    private static final String URL_MIDDLE = "http://www.autohome.com.cn/b/";
    private static final String URL_MIDFULL = "http://www.autohome.com.cn/c/";
    private static final String URL_FULL = "http://www.autohome.com.cn/d/";

    private static final String URL_MPV = "http://www.autohome.com.cn/mpv/";
    private static final String URL_SPORTS = "http://www.autohome.com.cn/s/";
    private static final String URL_PICKUP = "http://www.autohome.com.cn/p/";
    private static final String URL_MICROVAN = "http://www.autohome.com.cn/mb/";
    private static final String URL_LIGHT = "http://www.autohome.com.cn/qk/";
    private static final String URL_SUV = "http://www.autohome.com.cn/suv/";

    private String[] parseBrands(Document page) {
        Elements elements = page.select(".rank-list-ul h4 a");

        List<String> brands = new ArrayList<>();
        for (Element item: elements) {
            brands.add(item.text());
        }

        return brands.toArray(new String[0]);
    }

    private String[] parseUrl(String url) throws IOException {
        Document document = Jsoup.parse(httpClient.getAsString(url), url);
        return parseBrands(document);
    }

    public static void main(String[] args) {
        AutohomeClassBot bot = new AutohomeClassBot();
        CarClass classes = new CarClass();

        try {
            classes.micro = bot.parseUrl(URL_MICRO);
            logger.info("Parsed micro.");

            classes.mini = bot.parseUrl(URL_MINI);
            logger.info("Parsed mini.");

            classes.compact = bot.parseUrl(URL_COMPACT);
            logger.info("Parsed compact.");

            classes.middle = bot.parseUrl(URL_MIDDLE);
            logger.info("Parsed middle.");

            classes.midfull = bot.parseUrl(URL_MIDFULL);
            logger.info("Parsed midfull.");

            classes.full = bot.parseUrl(URL_FULL);
            logger.info("Parsed full.");


            classes.mpv = bot.parseUrl(URL_MPV);
            logger.info("Parsed mpv.");

            classes.sports = bot.parseUrl(URL_SPORTS);
            logger.info("Parsed sports.");

            classes.pickup = bot.parseUrl(URL_PICKUP);
            logger.info("Parsed pickup.");

            classes.microvan = bot.parseUrl(URL_MICROVAN);
            logger.info("Parsed microvan.");

            classes.light = bot.parseUrl(URL_LIGHT);
            logger.info("Parsed light.");

            classes.suv = bot.parseUrl(URL_SUV);
            logger.info("Parsed suv.");

            FileWriter fileWriter = new FileWriter("car_class.json", false);
            fileWriter.write(gson.toJson(classes));
            fileWriter.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Parse failed: " + e);
        }
    }
}

class CarClass {
    String[] micro;
    String[] mini;
    String[] compact;
    String[] middle;
    String[] midfull;
    String[] full;

    String[] mpv;
    String[] sports;
    String[] pickup;
    String[] microvan;
    String[] light;
    String[] suv;
}