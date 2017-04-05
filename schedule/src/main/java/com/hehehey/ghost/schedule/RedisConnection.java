package com.hehehey.ghost.schedule;

import com.hehehey.ghost.message.UserRequest;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import util.SecurityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dick Zhou on 3/29/2017.
 * Store url to Redis.
 */
public class RedisConnection {

    private static final Logger logger = Logger.getLogger(RedisConnection.class.getName());
    private static final JedisPool pool = new JedisPool(new JedisPoolConfig(), "qc.hehehey.com");

    private static final String taskSet = "tasks";
    private static final String taskList = "queue";
    private static final String taskUrlSetPrefix = "task:";
    private static final String taskUrlListPrefix = "task:queue:";

    public String addTask(UserRequest.SourceType type, String[] data) throws Exception{
        try (Jedis jedis = pool.getResource()) {
            String id = SecurityUtil.bytesToHex(SecurityUtil.md5(Arrays.toString(data).getBytes()));
            if (jedis.sismember(taskSet, id)) {
                throw new Exception("Task already exist.");
            }
            else {
                jedis.sadd(taskSet, id);
                jedis.lpush(taskList, id);
                jedis.lpush(taskSet, data);
            }
            return id;
        }
    }

    public void addUrls(String id, String[] urls) {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.sismember(taskSet, id)) {
                jedis.sadd(taskSet, id);
                jedis.lpush(taskList, id);
            }

            for (String url: urls) {
                Boolean isMember = jedis.sismember(taskUrlSetPrefix + id, url);
                if (!isMember) {
                    jedis.sadd(taskUrlSetPrefix + id, url);
                    jedis.lpush(taskUrlListPrefix + id, url);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Insert failed.", e);
        }
    }

    public void returnUrls(String id, List<String> urls) {
        try (Jedis jedis = pool.getResource()) {
            for (String url: urls) {
                jedis.lpush(taskUrlListPrefix + id, url);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Return failed.", e);
        }
    }

    public String getTask() {
        try (Jedis jedis = pool.getResource()) {
            String task = jedis.rpop(taskList);
            while (task != null) {
                if (jedis.llen(taskUrlListPrefix + task) > 0) {
                    jedis.lpush(taskList, task);
                    break;
                } else {
                    task = jedis.rpop(taskList);
                }
            }

            return task;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Get failed.", e);
            return "";
        }
    }

    public List<String> getUrls(String id, int size) {
        List<String> urls = new ArrayList<>();

        try (Jedis jedis = pool.getResource()) {
            for (int i = 0; i < size; i++) {
                urls.add(jedis.rpop(taskUrlListPrefix + id));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Get failed.", e);
        }

        return urls;
    }
}
