/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.data.beans;

/**
 * <p>Title: Jahia Action URI bean </p>
 * <p>Description: This object contains data relative to an action available on
 * an object in a Jahia system. We used to have only URIs to perform actions
 * in Jahia, but here we need more information such as a unique name identifier
 * which can be used to lookup icons, resource bundle keys, etc... </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ActionURIBean {

    private String name;
    private String uri;
    private String launcherUri;
    private boolean authorized = true;
    private boolean locked = false;
    private boolean releaseable = true;

    /**
     * Empty parameter constructor to respect JavaBean pattern.
     */
    public ActionURIBean() {
    }

    public ActionURIBean(final String name, final String uri, final String launcherUri) {
        this.name = name;
        this.uri = uri;
        this.launcherUri = launcherUri;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public String getLauncherUri() {
        return launcherUri;
    }
    public void setLauncherUri(String launcherUri) {
        this.launcherUri = launcherUri;
    }
    public boolean isAuthorized() {
        return authorized;
    }
    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }
    public boolean isLocked() {
        return locked;
    }
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public boolean isReleaseable() {
        return releaseable;
    }
    public void setReleaseable(boolean releaseable) {
        this.releaseable = releaseable;
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("ActionURIBean: Name=");
        buff.append(name);
        buff.append(", uri=");
        buff.append(uri);
        buff.append(", launcherUri=");
        buff.append(launcherUri);
        return buff.toString();
    }
}