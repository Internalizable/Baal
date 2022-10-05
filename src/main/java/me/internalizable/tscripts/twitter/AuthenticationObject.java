package me.internalizable.tscripts.twitter;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class AuthenticationObject {
    private String apiKey;
    private String apiSecretKey;
    private String accessToken;
    private String accessTokenSecret;

    public boolean isEmpty() {
        return apiKey.equals("null") || apiSecretKey.equals("null") || accessToken.equals("null") || accessTokenSecret.equals("null");
    }

    public boolean isAPIActive() {
        return !apiKey.equals("null") && !apiSecretKey.equals("null");
    }

    public boolean isConsumerActive() { return !accessToken.equals("null") && !accessTokenSecret.equals("null"); }

    public TwitterClient buildClient() {
        return new TwitterClient(TwitterCredentials.builder()
                .accessToken(accessToken)
                .accessTokenSecret(accessTokenSecret)
                .apiKey(apiKey)
                .apiSecretKey(apiSecretKey)
                .build());
    }
}
