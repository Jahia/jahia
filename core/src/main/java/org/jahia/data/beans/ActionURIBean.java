/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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