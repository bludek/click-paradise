package fr.xebia.clickcount;

import javax.inject.Singleton;

@Singleton
public class Configuration {

    public final String redisHost;
    public final int redisPort;
    public final int redisConnectionTimeout;  //milliseconds

    public Configuration() {
        String host = System.getenv().get("REDIS_HOST");
        String port = System.getenv().get("REDIS_PORT");
        String timeout = System.getenv().get("redis_connection_timeout");

        redisHost = host != null ? host : "redis";
        redisPort = Integer.parseInt(port != null ? port : "6379");
        redisConnectionTimeout = Integer.parseInt(timeout != null ? timeout : "2000");
    }
}
