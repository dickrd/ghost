import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hehehey.ghost.util.HttpClient;
import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 7/4/2017.
 * Get car images from newcar.xcar.com.cn
 */
public class XcarRecursiveBot {

    private static final Logger logger = Logger.getLogger(XcarRecursiveBot.class.getName());
    private static final HttpClient httpClient = new HttpClient();
    private static final Gson gson = new Gson();
    private static final String baseUrl = "http://newcar.xcar.com.cn/photo/";

    private Set<String> models = new HashSet<>();
    private Set<String> brands = new HashSet<>();
    private Queue<String> urls = new ConcurrentLinkedQueue<>();

    private void save() throws IOException {
        FileWriter writer = new FileWriter("models.json");
        writer.write(gson.toJson(models));
        writer.close();
        writer = new FileWriter("brands.json");
        writer.write(gson.toJson(brands));
        writer.close();
        writer = new FileWriter("urls.json");
        writer.write(gson.toJson(urls));
        writer.close();
        logger.info("Bot info saved.");
    }

    private void load() throws FileNotFoundException {
        models = gson.fromJson(new FileReader("models.json"), new TypeToken<HashSet<String>>(){}.getType());
        brands = gson.fromJson(new FileReader("brands.json"), new TypeToken<HashSet<String>>(){}.getType());
        urls = gson.fromJson(new FileReader("urls.json"), new TypeToken<ConcurrentLinkedQueue<String>>(){}.getType());
        logger.info("Bot info loaded.");
    }

    /**
     * Analyze page structure and generate new urls accordingly.
     * @param carPage The page to analyze.
     */
    private void parseCategory(CarPage carPage) {
        String currentModel = "";

        for (Element item: carPage.document.select(".pic_tabs_menu a")) {
            String brand = item.attr("href").split("/")[2];
            if (!brands.contains(brand)) {
                if (item.hasClass("active")) {
                    brands.add(brand);
                    logger.info("Old brand(sub): " + brand);
                }
                else {
                    brands.add(brand);
                    urls.add(baseUrl + brand);
                    logger.info("New brand(sub): " + brand);
                }
            }
        }
        for (Element item: carPage.document.select(".fra_box_lt a")) {
            String brand = item.attr("id");
            if (!brands.contains(brand) && (brand.startsWith("ps") || brand.startsWith("pb"))) {
                brands.add(brand);
                urls.add(baseUrl + brand);
                logger.info("New brand: " + brand);
            }
        }

        if (carPage.url.startsWith("pb")) {
            logger.info("Brand page, return.");
            return;
        }

        for (Element item: carPage.document.select(".pic_c_i_car a")) {
            String[] href = item.attr("href").split("/");
            String model = href[href.length - 1].split("_")[0];
            if (!models.contains(model)) {
                models.add(model);
                urls.add(baseUrl + model + "_1/");
                logger.info("New model found: " + model);
            }

            if (item.hasClass("active")) {
                carPage.isModelPage = true;
                currentModel = model;
                logger.info("Current model: " + model);
            }
        }

        if (!carPage.isModelPage) {
            logger.info("Not a model page, return.");
            return;
        }


        for (Element item: carPage.document.select(".color_item a")) {
            String[] href = item.attr("href").split("/");
            String[] categories = href[href.length - 1].split("_");
            carPage.colors.add(categories[categories.length - 1]);

            if (item.hasClass("active"))
                carPage.inColor = true;
        }
        logger.info("Color of this model: " + carPage.colors);

        if (!carPage.inColor && !carPage.colors.isEmpty()) {
            logger.info("Generating color urls...");
            for (String color : carPage.colors) {
                String newUrl = baseUrl + currentModel + "_1_" + color;
                urls.add(newUrl);
                logger.fine("Added: " + newUrl);
            }
        }
    }

    private CarImage[] parseImage(String url, Document document) throws IOException {
        List<CarImage> carImages = new ArrayList<>();

        StringBuilder brand = new StringBuilder();
        for (Element item: document.select(".atlas_nav a")) {
            if (!item.attr("href").startsWith("http")) {
                brand.append(item.text().replace("图片", "")).append("#");
            }
        }
        brand = brand.reverse().deleteCharAt(0).reverse();
        logger.info("Brand of this page: " + brand);

        String color = "";
        for (Element item: document.select(".color_item")) {
            Element i = item.child(0);
            Element a = item.child(1);
            if (a.hasClass("active")) {
                color = a.attr("title");
                color += i.attr("style")
                        .replace("background-color:", "")
                        .replace(";", "");
                logger.info("Color of this page: " + color);
                break;
            }
        }
        for (Element item: document.select(".pic-con img")) {
            CarImage carImage = new CarImage();

            String alt = item.attr("alt");
            logger.info("Adding : " + alt);
            carImage.model = alt.substring(0, alt.lastIndexOf(" "));
            carImage.perspective = alt.substring(alt.lastIndexOf(" ") + 1, alt.length());

            carImage.url = url;
            carImage.address = item.attr("src").replace("c_", "");
            carImage.color = color;
            carImage.brand = brand.toString();

            logger.info("Downloading image...");
            carImage.image = Base64.encodeBase64String(httpClient.get(carImage.address));
            carImages.add(carImage);
            logger.info("Done.");
        }
        return carImages.toArray(new CarImage[0]);
    }

    public static void main(String[] args) throws IOException {
        XcarRecursiveBot bot = new XcarRecursiveBot();
        bot.urls.add("http://newcar.xcar.com.cn/photo/ps1760/");
        int count = 0;

        while (!bot.urls.isEmpty()) {
            String url = bot.urls.peek();

            try {
                logger.info("Downloading: " + url);
                String string = httpClient.getAsString(url);
                CarPage carPage = new CarPage(url.replace(baseUrl, ""), Jsoup.parse(string));

                bot.parseCategory(carPage);
                if (carPage.isModelPage && (carPage.colors.isEmpty() || carPage.inColor)) {
                    CarImage[] carImages = bot.parseImage(url, carPage.document);
                    for (CarImage item : carImages) {
                        File jsonFile = new File(item.address.replaceFirst("http://", "root/") + ".json");
                        boolean succeed = jsonFile.getParentFile().mkdirs();
                        if (succeed)
                            logger.fine("Parent directory created.");
                        String gsonString = gson.toJson(item);
                        FileWriter writer = new FileWriter(jsonFile);
                        writer.write(gsonString);
                        writer.close();
                    }
                }
                bot.urls.poll();
            }
            catch (Exception e) {
                logger.warning(e.toString());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    logger.warning(e.getMessage());
                }
            }

            count++;
            if (count > 100) {
                bot.save();
                count = 0;
            }
        }
    }

    public static class CarImage {
        String url;
        String address;
        String brand;
        String model;
        String color;
        String perspective;
        String image;
    }

    public static class CarPage {
        boolean isModelPage = false;
        boolean inColor = false;
        List<String> colors = new ArrayList<>();
        String url;
        Document document;

        CarPage(String url, Document document) {
            this.url = url;
            this.document = document;
        }
    }
}
