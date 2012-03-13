package org.jahia.services.channels;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A channel describes a rendering target, so it may be a mobile device or any other kind of rendering browser
 * /technologies (RSS), etc...
 */
public class Channel implements Serializable {

    public static final Channel DEFAULT_CHANNEL = new Channel("default", "Default");

    private String identifier;
    private String displayName;

    Map<String,String> capabilities = new HashMap<String,String>();

    public Channel(String identifier) {
        this.identifier = identifier;
    }

    public Channel(String identifier, String displayName) {
        this.identifier = identifier;
        this.displayName = displayName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Map<String, String> capabilities) {
        this.capabilities = capabilities;
    }

    public boolean hasCapabilityValue(String capabilityName) {
        return capabilities.containsKey(capabilityName);
    }

    public String getCapability(String capabilityName) {
        return capabilities.get(capabilityName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Channel)) return false;

        Channel channel = (Channel) o;

        if (!identifier.equals(channel.identifier)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

}
