package me.internalizable.tscripts;

import io.github.redouane59.twitter.TwitterClient;
import me.internalizable.tscripts.manager.UrlSelenium;
import me.internalizable.tscripts.manager.UtilManager;
import me.internalizable.tscripts.redis.RedisManager;
import me.internalizable.tscripts.toml.TOMLConfiguration;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Core {

    private static CountDownLatch latch;

    public static void main(String[] args) {
        RedisManager redisManager = new RedisManager();
        redisManager.init();

        TOMLConfiguration tomlConfiguration = new TOMLConfiguration();

        TwitterClient twitterClient = tomlConfiguration.init();

        if(twitterClient == null)
            System.exit(-1);

        UtilManager utilManager = new UtilManager(twitterClient);
        utilManager.search();

        Scanner scanner = new Scanner(System.in);
        String a = scanner.next();

        while(!Objects.equals(a, "stop")) {
            a = scanner.next();
        }

        utilManager.stopStream();
    }

}
