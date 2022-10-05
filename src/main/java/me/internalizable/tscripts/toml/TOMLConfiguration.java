package me.internalizable.tscripts.toml;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import io.github.redouane59.twitter.TwitterClient;
import me.internalizable.tscripts.redis.handlers.AuthPropagation;
import me.internalizable.tscripts.twitter.AuthenticationObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class TOMLConfiguration {

    public static final CountDownLatch latch = new CountDownLatch(1);
    private final String PATH = "cfg/keys.toml";
    private Logger configLogger;

    public TOMLConfiguration() {
        configLogger = LoggerFactory.getLogger(TOMLConfiguration.class);

        File dir = new File("cfg");

        if(dir.mkdir())
            configLogger.info("Created directory");

        File cfgFile = new File(PATH);

        try {
            if(cfgFile.createNewFile()) {
                configLogger.info("No TOML Configuration was found, generating file and populating.");
                populate(cfgFile);
            }
        } catch (IOException e) {
            configLogger.error("An error occurred whilst creating new configuration file..");
            e.printStackTrace();
        }
    }

    private void populate(File file) {
        TomlWriter tomlWriter = new TomlWriter();

        try {
            tomlWriter.write(new AuthenticationObject("null", "null", "null", "null"), file);
            configLogger.info("Successfully written blank object to configuration file.");
        } catch (IOException e) {
            configLogger.error("An error occurred whilst writing blank object to new configuration file..");
            e.printStackTrace();
        }
    }

    public TwitterClient init() {
        Toml toml = new Toml().read(new File(PATH));

        AuthenticationObject obj = toml.to(AuthenticationObject.class);

        if(!obj.isAPIActive()) {
            configLogger.error("Please edit the configuration file with correct values before starting the script. Disabling...");
            return null;
        }

        if(obj.isAPIActive() && !obj.isConsumerActive()) {
            configLogger.error("API Keys are active, blocking thread until request is received from webserver.");

            try {
                latch.await();

                obj.setAccessToken(AuthPropagation.getConsumerKey());
                obj.setAccessTokenSecret(AuthPropagation.getConsumerKeySecret());

                TomlWriter tomlWriter = new TomlWriter();

                try {
                    tomlWriter.write(obj, new File(PATH));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }

        return obj.buildClient();
    }
}
