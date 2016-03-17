package fr.xebia.clickcount.repository;

import fr.xebia.clickcount.Configuration;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.client.RedisClient;
import org.redisson.client.RedisConnection;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.protocol.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClickRepository {

    private static final Logger log = LoggerFactory.getLogger(ClickRepository.class);

    private final Redisson redisson;

    private final RedisClient redisClient;

    @Inject
    public ClickRepository(Configuration configuration) {
        Config config = new Config();
        config.useSingleServer().setAddress(String.format("%s:%d", configuration.redisHost, configuration.redisPort));

        redisson = Redisson.create(config);
        redisClient = new RedisClient(new NioEventLoopGroup(), NioSocketChannel.class, configuration.redisHost, configuration.redisPort, configuration.redisConnectionTimeout);
    }

    public String ping() {
        RedisConnection conn = null;
        try {
            conn = redisClient.connect();
            return conn.sync(RedisCommands.PING);

        } catch (RedisConnectionException e) {
            return e.getCause().getMessage();
        } finally {
            if (conn != null) {
                conn.closeAsync();
            }
        }
    }

    public long getCount() {
        log.info(">> getCount");
        return redisson.getAtomicLong("count").get();
    }

    public long incrementAndGet() {
        log.info(">> incrementAndGet");
        return redisson.getAtomicLong("count").incrementAndGet();
    }

}
