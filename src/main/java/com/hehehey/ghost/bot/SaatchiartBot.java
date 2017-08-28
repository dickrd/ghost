package com.hehehey.ghost.bot;

import com.hehehey.ghost.network.HttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 8/21/2017.
 * Bot for downloading saatchiart.com drawings.
 * Result will be saved in a folder called img_art, preserving the directory structure ont the website,
 * so when resumed, there will be no duplicate.
 */
public class SaatchiartBot {
    private static final Logger logger = Logger.getLogger(SaatchiartBot.class.getName());
    private static final HttpClient httpClient = new HttpClient();
    private static final String outputDir = "img_art/";
    private static final String baseUrl = "https://www.saatchiart.com/drawings/fine-art?page=";

    private int startPage = 1;

    private void downloadImage(String src) throws IOException {
        if (!src.startsWith("https://")) {
            logger.warning("Image source abnormal: " + src);
            return;
        }

        File imageFile = new File(src.replaceFirst("https://", outputDir));
        if (imageFile.exists()) {
            logger.info("Image already exist, skipping: " + src);
            return;
        }

        boolean succeed = imageFile.getParentFile().mkdirs();
        if (succeed)
            logger.info("Parent directory created.");

        byte[] imgBytes = httpClient.get(src);
        FileOutputStream outputStream = new FileOutputStream(imageFile, false);
        outputStream.write(imgBytes);
        outputStream.close();
    }

    private void parseImage(String url) throws IOException {
        logger.info("Parsing: " + url);
        Document document = Jsoup.parse(httpClient.getAsString(url), url);
        for (Element element : document.select(".list-art-image img")) {
            String src = "https:" + element.attr("src");
            src = src.replace("-6.jpg", "-8.jpg");
            try {
                downloadImage(src);
            }
            catch (IOException e) {
                logger.warning("Network error for (will retry): " + src + ", " + e);
                try {
                    downloadImage(src);
                    logger.info("Downloaded: " + src);
                }
                catch (Exception ie) {
                    logger.warning("Still failed: " + src + ", " + e);
                }
            }
            catch (Exception e) {
                logger.warning("Image download failed: " + src + ", " + e);
            }
        }
    }

    private void run() {
        for (int i = startPage; i < 1002; i++) {
            try {
                parseImage(baseUrl + i);
            } catch (Exception e) {
                logger.warning("Page failed (" + i +"): " + e);
            }
        }
    }

    private SaatchiartBot(int i) {
        startPage = i;
    }

    /**
     * Start downloading.
     * @param args Beginning page number.
     */
    public static void main(String[] args) {
        int startPoint = 0;
        try {
            startPoint = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e) {
            System.out.println(e + "\nusage: saatchiartbot <page-number>");
        }
        SaatchiartBot bot = new SaatchiartBot(startPoint);
        bot.run();
    }
}
