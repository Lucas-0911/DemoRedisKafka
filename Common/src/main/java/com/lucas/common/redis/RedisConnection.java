package com.lucas.common.redis;

import redis.clients.jedis.Jedis;

public class RedisConnection {
//private static final String HOST = "127.0.0.1";
    private static final String HOST = "14.225.36.39";
    private static final int PORT = 6319;
    private static final String PASSWORD = "";

    private RedisConnection() {
    }

    public static Jedis getConnection() {
        Jedis jedis = new Jedis(HOST, PORT);
//        jedis.auth(PASSWORD);
        return jedis;
    }

    public static void closeConnection(Jedis jedis) {
        jedis.close();
    }
}