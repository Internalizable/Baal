package me.internalizable.tscripts.redis.handlers;


import lombok.Getter;
import lombok.Setter;
import me.internalizable.tscripts.redis.TwitterAuthenticator;
import me.internalizable.tscripts.redis.bus.annotation.RedisHandler;
import me.internalizable.tscripts.toml.TOMLConfiguration;

public class AuthPropagation {

    @Getter
    private static String consumerKey;

    @Getter
    private static String consumerKeySecret;

    @RedisHandler("twitter-oauth-callback")
    public void onAuthentication(TwitterAuthenticator request) {
        consumerKey = request.getOauth_token();
        consumerKeySecret = request.getOauth_token_secret();

        TOMLConfiguration.latch.countDown();
    }

}
