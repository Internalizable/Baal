package me.internalizable.tscripts.redis;

import lombok.Getter;
import lombok.Setter;
import me.internalizable.tscripts.twitter.AuthenticationObject;

@Getter @Setter
public class TwitterAuthenticator {
    private String user_id;
    private String oauth_token;
    private String oauth_token_secret;
    private String screen_name;
}
