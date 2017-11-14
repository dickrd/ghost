package com.hehehey.ghost.bot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hehehey.ghost.network.HttpClient;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.cli.*;
import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.bson.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Dick Zhou on 8/28/2017.
 * 爬取知乎内容，并保存到mongodb。
 */
public class ZhihuBot {
    private static final Logger logger = Logger.getLogger(ZhihuBot.class.getName());
    private static final HttpClient httpClient = new HttpClient("cookie.zhihu.obj");
    private static final Gson gson = new Gson();
    private static final int answerPerPage = 20;
    private static final long memberUpdateTimeSpan = 7 * 24 * 60 * 60;

    private static final String questionApiUrl = "https://www.zhihu.com/api/v4/questions/{0}/answers?sort_by=default" +
            "&include=data%5B*%5D.id%2Cvoteup_count%2Cthanks_count%2Ccomment_count%2Ccreated_time%2Cupdated_time%2Ccontent%3Bdata%5B*%5D.author.id%3Bdata%5B*%5D.question.id%2Ctitle" +
            "&limit={1}" +
            "&offset={2}";
    private static final String memberApiUrl = "https://www.zhihu.com/api/v4/members/{0}?include=" +
            "locations%2Cemployments%2Cgender%2Ceducations%2Cbusiness%2Cvoteup_count%2Cthanked_count%2Cfollower_count%2Cfollowing_count%2Cavatar_url%2Cdescription";

    private final MongoDatabase database;

    private ZhihuBot(String mongodb) {
        MongoClientURI connectionString = new MongoClientURI(mongodb);
        MongoClient mongoClient = new MongoClient(connectionString);
        database = mongoClient.getDatabase("zhihu");
    }

    private void saveImage(String imgUrl) throws IOException {
        File imgFile = new File(imgUrl.replaceFirst("https?://", "img_zhihu/"));
        if (imgFile.exists()) {
            logger.info("Image exist, skipping: " + imgUrl);
            return;
        }

        boolean succeed = imgFile.getParentFile().mkdirs();
        if (succeed)
            logger.fine("Parent directory created.");

        byte[] bytes = httpClient.get(imgUrl);
        FileOutputStream stream = new FileOutputStream(imgFile);
        stream.write(bytes);
        stream.close();
    }

    private void getMember(Header authorizationHeader, String memberId) {
        if (memberId.contentEquals("0")) {
            logger.warning("Anonymous user.");
            return;
        }

        MongoCollection<Document> zhihu = database.getCollection("member");
        Member member = new Member();

        boolean passedUpdateDue = true;
        for (Document found: zhihu.find(new Document("memberId", memberId))) {
            if (member.timestamp - found.getInteger("timestamp").longValue() < memberUpdateTimeSpan)
                passedUpdateDue = false;
        }
        if (!passedUpdateDue) {
            logger.info("Member already up-to-date: " + memberId);
            return;
        }

        logger.info("Update member: " + memberId);
        String url = MessageFormat.format(memberApiUrl, memberId);
        String responseString;
        JsonObject jsonObject;
        try {
            responseString = httpClient.getAsString(url,
                    Charsets.UTF_8.toString(),
                    new Header[]{authorizationHeader});
            jsonObject = gson.fromJson(responseString, JsonObject.class);
        } catch (IOException e) {
            logger.warning("Network error: " + e);
            return;
        } catch (Exception e) {
            logger.warning("Api failed: " + e);
            return;
        }

        try {
            member.memberId = jsonObject.get("id").getAsString();
            member.name = jsonObject.get("name").getAsString();
            member.gender = jsonObject.get("gender").getAsInt();
            member.avatarUrl = jsonObject.get("avatar_url").getAsString();
            member.description = jsonObject.get("description").getAsString();
            member.headline = jsonObject.get("headline").getAsString();

            member.voteCount = jsonObject.get("voteup_count").getAsInt();
            member.thanksCount = jsonObject.get("thanked_count").getAsInt();
            member.followerCount = jsonObject.get("follower_count").getAsInt();
            member.followingCount = jsonObject.get("following_count").getAsInt();

            if (jsonObject.get("locations") != null && jsonObject.get("locations").isJsonArray())
                for (JsonElement element: jsonObject.get("locations").getAsJsonArray()) {
                    member.location.add(element.getAsJsonObject().get("name").getAsString());
                }

            if (jsonObject.get("educations") != null && jsonObject.get("educations").isJsonArray())
                for (JsonElement element: jsonObject.get("educations").getAsJsonArray()) {
                    for (Map.Entry<String, JsonElement> entry: element.getAsJsonObject().entrySet()) {
                        member.education.add(entry.getValue().getAsJsonObject().get("name").getAsString());
                    }
                }
            if (jsonObject.get("employments") != null && jsonObject.get("employments").isJsonArray())
                for (JsonElement element: jsonObject.get("employments").getAsJsonArray()) {
                    for (Map.Entry<String, JsonElement> entry: element.getAsJsonObject().entrySet()) {
                        member.employment.add(entry.getValue().getAsJsonObject().get("name").getAsString());
                    }
                }
            if (jsonObject.get("business") != null && jsonObject.get("business").isJsonArray())
                for (JsonElement element: jsonObject.get("business").getAsJsonArray()) {
                    member.business.add(element.getAsJsonObject().get("name").getAsString());
                }
        }
        catch (Exception e) {
            logger.warning("Json parse failed: " + e);
            logger.warning("Message is: " + responseString);
            return;
        }

        zhihu.insertOne(Document.parse(gson.toJson(member)));

        try {
            saveImage(member.avatarUrl);
        } catch (IOException e) {
            logger.warning("Avatar save failed: " + e);
            logger.warning("Avatar url: " + member.avatarUrl);
        }
        try {
            String fullAvatar = member.avatarUrl.replace("_is", "");
            saveImage(fullAvatar);
        } catch (IOException e) {
            logger.warning("Full avatar save failed: " + e);
            logger.warning("Full avatar url: " + member.avatarUrl);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void getAnswer(Header authorizationHeader, String questionId, boolean updateAuthor) {
        Pattern imgUrlPattern = Pattern.compile("https?://[^\" ]+\\.(jpg|jpeg|png|gif|bmp|webp|svg)");
        String url = MessageFormat.format(questionApiUrl, questionId, answerPerPage, 0);
        String responseString;
        boolean isEnd;
        while (true) {
            logger.fine("Parsing: " + url);
            JsonObject jsonObject;
            try {
                responseString = httpClient.getAsString(url,
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

            if (jsonObject.get("data") == null || !jsonObject.get("data").isJsonArray()) {
                logger.warning("Incorrect answer json: " + responseString);
                return;
            }
            for (JsonElement element : jsonObject.getAsJsonArray("data")) {
                Answer answer = new Answer();
                try {
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
                    answer.question = questionObject.get("title").getAsString();
                }   
                catch (Exception e) {
                    logger.warning("Json parse failed: " + e);
                    logger.warning("Message is: " + element);
                    continue;
                }
                
                MongoCollection<Document> zhihu = database.getCollection("answer");
                zhihu.updateOne(new Document().append("answerId", answer.answerId).append("updatedAt", answer.updatedAt),
                        new Document("$set", Document.parse(gson.toJson(answer))),
                        new UpdateOptions().upsert(true));

                Matcher matcher = imgUrlPattern.matcher(answer.content);
                while (matcher.find()) {
                    String imgUrl = matcher.group();
                    try {
                        saveImage(imgUrl);
                    } catch (IOException e) {
                        logger.warning("Image save failed: " + e);
                        logger.warning("Image url: " + imgUrl);
                    }
                }

                if (updateAuthor)
                    getMember(authorizationHeader, answer.authorId);
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
        options.addOption(Option.builder("u")
                .longOpt("update")
                .hasArg(false)
                .desc("update all questions in database.")
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

        if (line.hasOption("update")) {
            List<BasicDBObject> optionList = new ArrayList<>();
            optionList.add(new BasicDBObject("$group", new BasicDBObject("_id", "$questionId")));
            AggregateIterable<Document> questionIds = zhihuBot.database.getCollection("answer")
                    .aggregate(optionList);
            for (Document questionId: questionIds)
            {
                logger.info("Updating question: " + questionId.get("_id"));
                zhihuBot.getAnswer(authorizationHeader, questionId.get("_id").toString(), true);
                httpClient.saveCookie();
            }
        }

        if (line.hasOption("question")) {
            for (String questionId : line.getOptionValues("question")) {
                logger.info("Parsing question: " + questionId);
                zhihuBot.getAnswer(authorizationHeader, questionId, true);
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

        String question;

        Answer() {
            timestamp = System.currentTimeMillis() / 1000;
        }
    }

    public static class Member {
        long timestamp;
        String memberId;
        String name;
        int gender;
        String avatarUrl;
        String description;
        String headline;

        int voteCount;
        int thanksCount;
        int followerCount;
        int followingCount;

        List<String> location;
        List<String> education;
        List<String> employment;
        List<String> business;

        Member() {
            timestamp = System.currentTimeMillis() / 1000;

            location = new ArrayList<>();
            education = new ArrayList<>();
            employment = new ArrayList<>();
            business = new ArrayList<>();
        }
    }
}
