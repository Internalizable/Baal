package me.internalizable.tscripts.redis.bus;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import me.internalizable.tscripts.redis.RedisManager;
import me.internalizable.tscripts.redis.bus.annotation.RedisHandler;
import org.apache.commons.lang.ArrayUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class RedisBus {

    private final Gson gson;
    private final Multimap<Object, Method> listeners = ArrayListMultimap.create();
    private final Set<String> registeredChannels = new HashSet<>();
    private JedisPool jedisPool;

    public RedisBus(JedisPool jedisPool) {
        this.gson = RedisManager.getGson();
        try {
            this.jedisPool = jedisPool;
        } catch(CompletionException e) {
            //todo log
        }
    }

    public Gson getGson() {
        return gson;
    }

    public void registerListener(Object object) {
        if (Arrays.stream(object.getClass().getDeclaredMethods()).noneMatch(method -> method.isAnnotationPresent(RedisHandler.class)))
            return;
        Set<Method> methods = Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(RedisHandler.class))
                .filter(method -> method.getParameters().length == 1)
                .collect(Collectors.toSet());

        methods.forEach(method -> registeredChannels.addAll(Arrays.asList(method.getAnnotation(RedisHandler.class).value())));
        listeners.putAll(object, methods);
    }

    public Multimap<Object, Method> getListeners() {
        return listeners;
    }

    public Set<String> getRegisteredChannels() {
        return registeredChannels;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void publishPayload(String channel, Object payload) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, gson.toJson(payload));
        }
    }

    public void init() {
        CompletableFuture.runAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(new JedisPubSub() {

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {

                    }

                    @Override
                    public void onMessage(String channel, String message) {
                        getListeners().entries()
                                .stream()
                                .filter(entry -> ArrayUtils.contains(entry.getValue().getAnnotation(RedisHandler.class).value(), channel))
                                .forEach(entry -> {
                                    try {
                                        entry.getValue().invoke(entry.getKey(), gson.fromJson(message, entry.getValue().getParameterTypes()[0]));
                                    } catch (IllegalAccessException | InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                });
                    }
                }, getRegisteredChannels().toArray(new String[0]));
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            //todo log
            return null;
        });
    }

}