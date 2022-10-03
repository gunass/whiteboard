package util;

import java.io.Serializable;

/**
 * Store credentials. Not very secure.
 * @author Alex Epstein
 */
public class UserIdentity implements Serializable {
    public String username;
    public String secret;

    public UserIdentity(String username, String secret){
        this.username = username;
        this.secret = secret;
    }

    public boolean is(UserIdentity u) {

        return u.username.equals(this.username) && u.secret.equals(this.secret);
    }

}
