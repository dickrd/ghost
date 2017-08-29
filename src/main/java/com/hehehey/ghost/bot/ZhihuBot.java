package com.hehehey.ghost.bot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hehehey.ghost.network.HttpClient;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.cli.*;
import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.bson.Document;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 8/28/2017.
 * 爬取知乎内容，并保存到mongodb。
 */
public class ZhihuBot {
    private static final Logger logger = Logger.getLogger(ZhihuBot.class.getName());
    private static final HttpClient httpClient = new HttpClient("cookie.zhihu.obj");
    private static final Gson gson = new Gson();
    private static final int answerPerPage = 30;

    private static final String questionApiUrl = "https://www.zhihu.com/api/v4/questions/{0}/answers?sort_by=default" +
            "&include=data%5B*%5D.content%2Cvoteup_count%2Ccreated_time%2Cupdated_time%3Bdata%5B*%5D.author.follower_count" +
            "&limit={1}" +
            "&offset={2}";

    private final MongoDatabase database;

    private ZhihuBot(String mongodb) {
        MongoClientURI connectionString = new MongoClientURI(mongodb);
        MongoClient mongoClient = new MongoClient(connectionString);
        database = mongoClient.getDatabase("zhihu");
    }

    @SuppressWarnings("SameParameterValue")
    private void save(String collection, Document filter, Document data) {
        MongoCollection<Document> zhihu = database.getCollection(collection);
        zhihu.updateOne(filter, new Document("$set", data), new UpdateOptions().upsert(true));
    }

    private void getAnswer(Header authorizationHeader, String questionId) {
        String url = MessageFormat.format(questionApiUrl, questionId, answerPerPage, 0);
        boolean isEnd;
        while (true) {
            logger.info("Parsing: " + url);
            JsonObject jsonObject;
            try {
                String responseString = httpClient.getAsString(url,
                        Charsets.UTF_8.toString(),
                        new Header[]{authorizationHeader});
                jsonObject = gson.fromJson(responseString, JsonObject.class);
                JsonObject paging = jsonObject.getAsJsonObject("paging");
                isEnd = paging.get("is_end").getAsBoolean();
                url = paging.get("next").getAsString();
            } catch (IOException e) {
                logger.warning("Network error: " + e);
                continue;
            } catch (Exception e) {
                logger.warning("Api failed: " + e);
                return;
            }

            for (JsonElement element : jsonObject.getAsJsonArray("data")) {
                Answer answer = new Answer();
                JsonObject currentObject = element.getAsJsonObject();
                answer.answerId = currentObject.get("id").getAsLong();

                answer.voteCount = currentObject.get("voteup_count").getAsInt();
                answer.thanksCount = currentObject.get("thanks_count").getAsInt();
                answer.commentCount = currentObject.get("comment_count").getAsInt();

                answer.createdAt = currentObject.get("created_time").getAsLong();
                answer.updatedAt = currentObject.get("updated_time").getAsLong();
                answer.content = currentObject.get("content").getAsString();

                JsonObject authorObject = currentObject.getAsJsonObject("author");
                answer.authorId = authorObject.get("id").getAsString();

                JsonObject questionObject = currentObject.getAsJsonObject("question");
                answer.questionId = questionObject.get("id").getAsLong();

                save("answer",
                        new Document().append("answerId", answer.answerId).append("updatedAt", answer.updatedAt),
                        Document.parse(gson.toJson(answer)));
            }

            if (isEnd) {
                logger.info("Done.");
                return;
            }
        }
    }

    public static void main(String[] args) {
        // Parse command line options.
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(Option.builder("h")
                .longOpt("help")
                .hasArg(false)
                .desc("display help.")
                .build());
        options.addOption(Option.builder("d")
                .longOpt("mongodb")
                .numberOfArgs(1)
                .desc("mongodb connection string.")
                .build());
        options.addOption(Option.builder("a")
                .longOpt("auth")
                .numberOfArgs(1)
                .desc("authorization header.")
                .build());
        options.addOption(Option.builder("q")
                .longOpt("question")
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .desc("id of the question to retrieve.")
                .build());
        CommandLine line;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("ghost zhihu", options);
            return;
        }

        if (line.hasOption("help")) {
            formatter.printHelp("ghost zhihu", options);
            return;
        }
        if (!line.hasOption("auth")) {
            logger.log(Level.SEVERE, "Must specify auth code(--auth).");
            return;
        }
        if (!line.hasOption("mongodb")) {
            logger.log(Level.SEVERE, "Must specify mongodb connection(--mongodb).");
            return;
        }

        BasicHeader authorizationHeader = new BasicHeader("authorization", line.getOptionValue("auth"));
        ZhihuBot zhihuBot = new ZhihuBot(line.getOptionValue("mongodb"));

        if (line.hasOption("question")) {
            for (String questionId : line.getOptionValues("question")) {
                logger.info("Parsing question: " + questionId);
                zhihuBot.getAnswer(authorizationHeader, questionId);
                httpClient.saveCookie();
            }
        }
    }

    public static class Answer {
        long timestamp;
        long answerId;
        String content;

        int voteCount;
        int thanksCount;
        int commentCount;

        long createdAt;
        long updatedAt;

        String authorId;
        long questionId;

        Answer() {
            timestamp = System.currentTimeMillis() / 1000;
        }
    }
}
