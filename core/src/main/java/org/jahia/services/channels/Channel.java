package org.jahia.services.channels;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A channel describes a rendering target, so it may be a mobile device or any other kind of rendering browser
 * /technologies (RSS), etc...
 */
public class Channel implements Serializable {

    public static final String GENERIC_CHANNEL = "generic";

    private String identifier;

    Map<String,String> capabilities = new HashMap<String,String>();

    public Channel(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public boolean isGeneric() {
        return identifier.equals(GENERIC_CHANNEL);
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
