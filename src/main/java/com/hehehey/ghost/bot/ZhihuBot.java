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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private static final int answerPerPage = 20;
    private static final long memberUpdateTimeSpan = 7 * 24 * 60 * 60;

    private static final String questionApiUrl = "https://www.zhihu.com/api/v4/questions/{0}/answers?sort_by=default" +
            "&include=data%5B*%5D.content%2Cvoteup_count%2Ccreated_time%2Cupdated_time%3Bdata%5B*%5D.author.follower_count" +
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

    private void getMember(Header authorizationHeader, String memberId) {
        MongoCollection<Document> zhihu = database.getCollection("member");
        Member member = new Member();

        boolean passedUpdateDue = true;
        for (Document found: zhihu.find(new Document("memberId", memberId))) {
            if (member.timestamp - found.getLong("timestamp") < memberUpdateTimeSpan)
                passedUpdateDue = false;
        }
        if (!passedUpdateDue) {
            logger.fine("Member already up-to-date: " + memberId);
            return;
        }
        else {
            logger.fine("Update member: " + memberId);
        }

        String url = MessageFormat.format(memberApiUrl, memberId);
        JsonObject jsonObject;
        try {
            String responseString = httpClient.getAsString(url,
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

        for (JsonElement element: jsonObject.get("locations").getAsJsonArray()) {
            member.location.add(element.getAsJsonObject().get("name").getAsString());
        }
        for (JsonElement element: jsonObject.get("educations").getAsJsonArray()) {
            for (Map.Entry<String, JsonElement> entry: element.getAsJsonObject().entrySet()) {
                member.education.add(entry.getValue().getAsJsonObject().get("name").getAsString());
            }
        }
        for (JsonElement element: jsonObject.get("employments").getAsJsonArray()) {
            for (Map.Entry<String, JsonElement> entry: element.getAsJsonObject().entrySet()) {
                member.employment.add(entry.getValue().getAsJsonObject().get("name").getAsString());
            }
        }
        for (JsonElement element: jsonObject.get("business").getAsJsonArray()) {
            member.business.add(element.getAsJsonObject().get("name").getAsString());
        }

        zhihu.insertOne(Document.parse(gson.toJson(member)));
    }

    private void getAnswer(Header authorizationHeader, String questionId) {
        String url = MessageFormat.format(questionApiUrl, questionId, answerPerPage, 0);
        boolean isEnd;
        while (true) {
            logger.fine("Parsing: " + url);
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

                MongoCollection<Document> zhihu = database.getCollection("answer");
                zhihu.updateOne(new Document().append("answerId", answer.answerId).append("updatedAt", answer.updatedAt),
                        new Document("$set", Document.parse(gson.toJson(answer))),
                        new UpdateOptions().upsert(true));
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
