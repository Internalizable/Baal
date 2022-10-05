package me.internalizable.tscripts.manager;

import com.github.scribejava.core.model.Response;
import io.github.redouane59.twitter.IAPIEventListener;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.collections.CollectionsResponse;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.rules.FilteredStreamRulePredicate;
import io.github.redouane59.twitter.dto.stream.StreamRules;
import io.github.redouane59.twitter.dto.tweet.*;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@RequiredArgsConstructor
public class UtilManager {

    public final TwitterClient client;
    private Future<Response> response = null;

    public void search() {

        try {
            if(client.retrieveFilteredStreamRules() == null || client.retrieveFilteredStreamRules() != null && client.retrieveFilteredStreamRules().stream().noneMatch(rule -> rule.getValue().equals("@TheBaalBot from:CharSequence")))
                client.addFilteredStreamRule(FilteredStreamRulePredicate.withMention("TheBaalBot").and(FilteredStreamRulePredicate.withUser("CharSequence")),"Pull my tweets and keep them up to date.");
        } catch(IllegalArgumentException exception) {
            exception.printStackTrace();
        }

        client.retrieveFilteredStreamRules().forEach(streamRule -> {
            System.out.println(streamRule.getValue());
        });

        response = client.startFilteredStream(new IAPIEventListener() {
            @Override
            public void onStreamError(final int httpCode, final String error) {
                System.out.println("Error occurred with http code: " + httpCode + " and error " + error);
            }

            @Override
            public void onTweetStreamed(final Tweet tweet) {

                System.out.println("Recieved tweet with id " + tweet.getId());
                System.out.println(tweet.getText());
                System.out.println("------------------------------");

                if(tweet instanceof TweetV2 tweetV2) {
                    assert tweetV2.getText() != null;

                    if(tweetV2.getText().contains("blocked")) {

                        String parentTweetId = tweetV2.getInReplyToStatusId(TweetType.REPLIED_TO);

                        System.out.println("Parent ID: " + parentTweetId);

                        String quoteTweetId = client.getTweet(parentTweetId).getInReplyToStatusId(TweetType.QUOTED);

                        System.out.println("Quote ID: " + quoteTweetId);

                        Tweet quotedTweet = client.getTweet(quoteTweetId);

                        UploadMediaResponse uploadMediaResponse = null;
                        try {
                            uploadMediaResponse = client.uploadMedia("", UrlSelenium.capture(quotedTweet.getUser().getName(), quotedTweet.getId()), MediaCategory.TWEET_IMAGE);

                            client.postTweet(TweetParameters.builder()
                                    .reply(TweetParameters.Reply.builder().inReplyToTweetId(tweet.getId()).build())
                                    .media(TweetParameters.Media.builder().mediaIds(List.of(uploadMediaResponse.getMediaId())).build())
                                    .text("I've attached the tweet below, take a look!")
                                    .build());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }

                }

            }

            @Override
            public void onUnknownDataStreamed(final String json) {
                System.out.println("Unknown data streamed: " + json);
            }

            @Override
            public void onStreamEnded(final Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stopStream() {
        client.stopFilteredStream(response);
    }

    private void sendTweet(TweetV2 tweetV2, String command) {

    }
}
