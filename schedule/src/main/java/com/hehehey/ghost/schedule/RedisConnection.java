package com.hehehey.ghost.schedule;

import com.hehehey.ghost.message.frontend.UserRequest;
import com.hehehey.ghost.util.SecurityUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dick Zhou on 3/29/2017.
 * Store url to Redis.
 */
public class RedisConnection {

    private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), "qc.hehehey.com");

    private static final String SET_ALL_TASK = "tasks.set";
    private static final String LIST_ALL_TASK = "tasks.queue";
    private static final String TASK_PREFIX = "task:";

    private static final String SET_URL_SUFFIX = ":set";
    private static final String LIST_URL_SUFFIX = ":queue";
    private static final String LIST_WORD_SUFFIX = ":words";
    private static final String LIST_SEED_URL_SUFFIX = ":seeds";

    /**
     * Add task to redis task list.
     * @param type Source type.
     * @param data Either keywords for search type or seed urls for seeds type.
     * @return Generated task id.
     * @throws Exception If the keywords or seed urls combination exists or type not supported.
     */
    public String addTask(UserRequest.SourceType type, String[] data) throws Exception{
        try (Jedis jedis = pool.getResource()) {
            String id = SecurityUtil.bytesToHex(SecurityUtil.md5(Arrays.toString(data).getBytes()));
            if (jedis.sismember(SET_ALL_TASK, id)) {
                throw new Exception("Task already exist.");
            }
            else {
                jedis.sadd(SET_ALL_TASK, id);
                jedis.lpush(LIST_ALL_TASK, id);

                switch (type) {
                    case search:
                        jedis.lpush(TASK_PREFIX + id + LIST_WORD_SUFFIX, data);
                        break;
                    case seedUrl:
                        jedis.lpush(TASK_PREFIX + id + LIST_SEED_URL_SUFFIX, data);
                        break;
                    default:
                        throw new Exception("Unsupported source type: " + type);
                }
            }
            return id;
        }
    }

    public void addUrls(String id, String[] urls) {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.sismember(SET_ALL_TASK, id)) {
                jedis.sadd(SET_ALL_TASK, id);
                jedis.lpush(LIST_ALL_TASK, id);
            }

            for (String url: urls) {
                Boolean isMember = jedis.sismember(TASK_PREFIX + id + SET_URL_SUFFIX, url);
                if (!isMember) {
                    jedis.sadd(TASK_PREFIX + id + SET_URL_SUFFIX, url);
                    jedis.lpush(TASK_PREFIX + id + LIST_URL_SUFFIX, url);
                }
            }
        }
    }

    public String getTask() {
        try (Jedis jedis = pool.getResource()) {
            String task = jedis.rpop(LIST_ALL_TASK);
            while (task != null) {
                if (jedis.llen(TASK_PREFIX + task + LIST_URL_SUFFIX) > 0) {
                    jedis.lpush(LIST_ALL_TASK, task);
                    break;
                } else {
                    task = jedis.rpop(LIST_ALL_TASK);
                }
            }

            return task;
        }
    }

    public String[] getUrls(String id, String name, int size) throws Exception {
        if (id == null || id.contentEquals("") || name == null || name.contentEquals(""))
            throw new Exception("No id or name: " + id + ", " + name);

        List<String> urls = new ArrayList<>();
        try (Jedis jedis = pool.getResource()) {
            for (int i = 0; i < size; i++) {
                String item = jedis.rpop(TASK_PREFIX + id + LIST_URL_SUFFIX);
                if (item == null)
                    break;
                else
                    urls.add(item);
            }
        }

        return urls.toArray(new String[0]);
    }

    public int count(String id) {
        return 0;
    }
}
