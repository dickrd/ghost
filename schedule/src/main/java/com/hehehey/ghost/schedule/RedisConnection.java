package com.hehehey.ghost.schedule;

import com.hehehey.ghost.util.SecurityUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dick Zhou on 3/29/2017.
 * Store url to Redis.
 */
public class RedisConnection {

    private static JedisPool pool = null;

    private static final String SET_ALL_TASK = "tasks.set";
    private static final String LIST_ALL_TASK = "tasks.queue";
    private static final String TASK_PREFIX = "task:";

    private static final String SET_URL_SUFFIX = ":set";
    private static final String LIST_URL_MID = ":queue:";
    private static final String COUNT_URL_SUFFIX = ":count";
    private static final String LIST_WORD_SUFFIX = ":words";
    private static final String LIST_SEED_URL_SUFFIX = ":seeds";

    public static void newPool() throws URISyntaxException {
        if (pool != null)
            pool.close();

        pool = new JedisPool(new URI(MasterConfig.INSTANCE.getRedisUri()));
    }

    /**
     * Add task to redis task list.
     * @param seeds Seed urls.
     * @param words Keywords for search.
     * @return Generated task id.
     * @throws Exception If the keywords and seed urls combination exists.
     */
    public String addTask(String[] seeds, String[] words) throws Exception{
        String id = SecurityUtil.generateUniqueId();

        try (Jedis jedis = pool.getResource()) {
            if (jedis.sismember(SET_ALL_TASK, id)) {
                throw new Exception("Task already exist.");
            }
            else {
                jedis.sadd(SET_ALL_TASK, id);
                jedis.lpush(LIST_ALL_TASK, id);
            }
        }
        addSource(id, seeds, words);

        return id;
    }

    public void addSource(String id, String[] seeds, String[] words) {
        try (Jedis jedis = pool.getResource()) {
            if (words != null && words.length > 0)
                jedis.lpush(TASK_PREFIX + id + LIST_WORD_SUFFIX, words);
            if (seeds != null && seeds.length > 0)
                jedis.lpush(TASK_PREFIX + id + LIST_SEED_URL_SUFFIX, seeds);
        }
    }

    public void addUrls(String id, String[] urls) throws URISyntaxException {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.sismember(SET_ALL_TASK, id)) {
                jedis.sadd(SET_ALL_TASK, id);
                jedis.lpush(LIST_ALL_TASK, id);
            }

            for (String url: urls) {
                Boolean isMember = jedis.sismember(TASK_PREFIX + id + SET_URL_SUFFIX, url);
                if (isMember)
                    continue;

                URI uri = new URI(url);
                String host = uri.getHost();
                if (host == null || host.contentEquals(""))
                    continue;

                jedis.incr(TASK_PREFIX + id + COUNT_URL_SUFFIX);
                jedis.sadd(TASK_PREFIX + id + SET_URL_SUFFIX, url);
                jedis.lpush(TASK_PREFIX + id + LIST_URL_MID + host, url);
            }
        }
    }

    public String getTask() {
        try (Jedis jedis = pool.getResource()) {
            String task = jedis.rpop(LIST_ALL_TASK);
            while (task != null) {
                Long wordCount = jedis.llen(TASK_PREFIX + task + LIST_WORD_SUFFIX);
                Long seedCount = jedis.llen(TASK_PREFIX + task + LIST_SEED_URL_SUFFIX);
                Long urlCount = 0L;

                String urlCountString = jedis.get(TASK_PREFIX + task + COUNT_URL_SUFFIX);
                if (urlCountString != null)
                    urlCount = Long.valueOf(urlCountString);

                if (wordCount + seedCount + urlCount > 0) {
                    jedis.lpush(LIST_ALL_TASK, task);
                    break;
                } else {
                    task = jedis.rpop(LIST_ALL_TASK);
                }
            }

            return task;
        }
    }

    public String[] getUrls(String id, String name, int size) {
        List<String> urls = new ArrayList<>();
        try (Jedis jedis = pool.getResource()) {
            for (int i = 0; i < size; i++) {
                String item = jedis.rpop(TASK_PREFIX + id + LIST_URL_MID + name);
                jedis.decr(TASK_PREFIX + id + COUNT_URL_SUFFIX);
                if (item == null)
                    break;
                else
                    urls.add(item);
            }
        }

        return urls.toArray(new String[0]);
    }

    public long count(String id) {
        long count;
        try (Jedis jedis = pool.getResource()) {
            count = Long.valueOf(jedis.get(TASK_PREFIX + id + COUNT_URL_SUFFIX));
        }

        return count;
    }

    public String[] getWords(String id, int size) {
        return getMany(TASK_PREFIX + id + LIST_WORD_SUFFIX, size);
    }

    public String[] getSeeds(String id, int size) {
        return getMany(TASK_PREFIX + id + LIST_SEED_URL_SUFFIX, size);
    }

    private String[] getMany(String key, int size) {
        List<String> source = new ArrayList<>();

        try (Jedis jedis = pool.getResource()) {
            for (int i = 0; i < size; i++) {
                String item = jedis.rpop(key);
                if (item == null)
                    break;
                else
                    source.add(item);
            }
        }

        return source.toArray(new String[0]);
    }
}
