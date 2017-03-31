/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.channels;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A channel describes a rendering target, so it may be a mobile device or any other kind of rendering browser
 * /technologies (RSS), etc...
 */
public class Channel implements Serializable {

    private static final long serialVersionUID = -6550768281739417976L;

    public static final String GENERIC_CHANNEL = "generic";

    private String identifier;
    private String fallBack = GENERIC_CHANNEL;
    private boolean visible = true;

    Map<String,String> capabilities = new HashMap<String,String>();

    public Channel() {
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

    public String getFallBack() {
        return fallBack;
    }

    public void setFallBack(String fallBack) {
        this.fallBack = fallBack;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isGeneric() {
        return identifier.equals(GENERIC_CHANNEL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Channel channel = (Channel) o;

        if (!identifier.equals(channel.identifier)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

}
