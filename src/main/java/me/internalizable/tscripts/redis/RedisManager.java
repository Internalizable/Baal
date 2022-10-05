package me.internalizable.tscripts.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import me.internalizable.tscripts.redis.bus.RedisBus;
import me.internalizable.tscripts.redis.handlers.AuthPropagation;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.awt.desktop.SystemEventListener;


@Getter
@Setter
public class RedisManager {

    private final JedisPool jedisPool;

    @Getter
    private static Gson gson = new GsonBuilder().create();

    private RedisBus redisBus;

    @Getter
    private static Jedis jedis;

    public RedisManager() {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);;
    }

    public void init() {
        try(Jedis resJedis = jedisPool.getResource()) {
            this.redisBus = new RedisBus(jedisPool);
            jedis = resJedis;

            redisBus.registerListener(new AuthPropagation());
            redisBus.init();
        } catch(JedisException e) {

        }
    }

    public void clear() {
        redisBus.getListeners().clear();
        redisBus.getRegisteredChannels().clear();
    }

}