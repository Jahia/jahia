/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.seo;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Vanity Url mapped to content
 * 
 * @author Benjamin Papez
 * 
 */
public class VanityUrl {

    private String url;

    private String language;

    private String identifier;

    private String path;

    private String site;

    private boolean defaultMapping;

    private boolean active;

    public VanityUrl() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * @param url mapping URL
     * @param site current virtual site
     * @param language the mapping language
     */
    public VanityUrl(String url, String site, String language) {
        super();
        this.url = url;
        this.site = site;
        this.language = language;
    }

    /**
     * Initializes an instance of this class.
     * @param url mapping URL
     * @param site current virtual site
     * @param language the mapping language
     * @param defaultMapping is it the default mapping for the node?
     * @param active whether the mapping is active
     */
    public VanityUrl(String url, String site, String language, boolean defaultMapping, boolean active) {
        this(url, site, language);
        this.defaultMapping = defaultMapping;
        this.active = active;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isDefaultMapping() {
        return defaultMapping;
    }

    public void setDefaultMapping(boolean defaultMapping) {
        this.defaultMapping = defaultMapping;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getUrl()).append(getLanguage()).append(getSite()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)
                || obj != null
                && (obj instanceof VanityUrl)
                && new EqualsBuilder().append(getUrl(),
                        ((VanityUrl) obj).getUrl()).append(
                        getLanguage(), ((VanityUrl) obj).getLanguage()).append(
                        getSite(), ((VanityUrl) obj).getSite()).isEquals();
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
