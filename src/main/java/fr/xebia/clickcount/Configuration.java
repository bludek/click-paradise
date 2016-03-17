package fr.xebia.clickcount;

import javax.inject.Singleton;

@Singleton
public class Configuration {

    public final String redisHost;
    public final int redisPort;
    public final int redisConnectionTimeout;  //milliseconds

    public Configuration() {
        redisHost = System.getProperty("redis_host", "redis");
        redisPort = Integer.parseInt(System.getProperty("redis_port", "6379"));
        redisConnectionTimeout = Integer.parseInt(System.getProperty("redis_connection_timeout", "2000"));
    }
}
