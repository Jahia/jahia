package org.jahia.modules.serversettings.users.management;

import java.io.Serializable;

/**
 * @author rincevent
 */
public class SearchCriteria implements Serializable {
    private static final long serialVersionUID = 6922751122839696683L;
    
    private String searchString;
    private String searchIn;
    private String[] properties;
    private String storedOn;
    private String[] providers;

    public String[] getProperties() {
        return properties;
    }

    public void setProperties(String[] properties) {
        this.properties = properties;
    }

    public String[] getProviders() {
        return providers;
    }

    public void setProviders(String[] providers) {
        this.providers = providers;
    }

    public String getSearchIn() {
        return searchIn;
    }

    public void setSearchIn(String searchIn) {
        this.searchIn = searchIn;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getStoredOn() {
        return storedOn;
    }

    public void setStoredOn(String storedOn) {
        this.storedOn = storedOn;
    }
}
