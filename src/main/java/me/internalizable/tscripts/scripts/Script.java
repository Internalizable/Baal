package me.internalizable.tscripts.scripts;

import io.github.redouane59.twitter.TwitterClient;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Getter @Setter
public abstract class Script {
    private String description;
    private TwitterClient twitterClient;

    private Logger scriptLogger;

    public Script() {
        scriptLogger = LoggerFactory.getLogger(Script.class);
    }

    public abstract void execute(Object... objects);
}
