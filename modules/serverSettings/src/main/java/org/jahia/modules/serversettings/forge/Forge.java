package org.jahia.modules.serversettings.forge;

import java.io.Serializable;

public class Forge implements Serializable {

    private static final long serialVersionUID = 2031426003900898977L;
    String url;
    String user;
    String password;
    String id;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
