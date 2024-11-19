/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.seo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.Serializable;

/**
 * Vanity Url mapped to content
 *
 * @author Benjamin Papez
 *
 */
public class VanityUrl implements Serializable {

    private static final long serialVersionUID = 5736971828491108878L;

    private String url;

    private String language;

    private String identifier;

    private String path;

    private String site;

    private boolean defaultMapping;

    private boolean active;

    private boolean file;

    public VanityUrl() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * @param url mapping URL
     * @param site virtual site of the mapped content object
     * @param language the mapping language
     */
    public VanityUrl(String url, String site, String language) {
        super();
        setUrl(url);
        this.site = site;
        this.language = language;
    }

    /**
     * Initializes an instance of this class.
     * @param url mapping URL
     * @param site virtual site of the mapped content object
     * @param language the mapping language
     * @param defaultMapping is it the default mapping for the node?
     * @param active whether the mapping is active
     */
    public VanityUrl(String url, String site, String language, boolean defaultMapping, boolean active) {
        this(url, site, language);
        this.defaultMapping = defaultMapping;
        this.active = active;
    }

    /**
     * Returns the vanity URL
     * @return the vanity URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the vanity URL
     * @param url the vanity URL
     */
    public void setUrl(String url) {
        // we strip excessive slashes from the URL mapping and add a leading one if it does not present
        url = StringUtils.stripEnd(url, "/");
        if (url != null) {
            // this check is here to avoid unneeded string creation
            if (url.length() > 1 && (url.charAt(0) == '/' && url.charAt(1) == '/')) {
                url = '/' + StringUtils.stripStart(url, "/");
            } else {
                url = url.length() > 0 && url.charAt(0) != '/' ? '/' + url : url;
            }
        }
        this.url = url;
    }

    /**
     * Returns the language of the content object to which the vanity URL maps to
     * @return language of the mapping
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language of the content object to which the vanity URL maps to
     * @param language the mapping language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Returns true whether this URL mapping is the default one for the language
     * @return true whether this URL mapping is the default one for the language, otherwise false
     */
    public boolean isDefaultMapping() {
        return defaultMapping;
    }

    /**
     * Sets the default flag for the URL mapping - true if mapping should be the
     * default for the language, otherwise false
     * @param defaultMapping true if mapping should be the default for the language, otherwise false
     */
    public void setDefaultMapping(boolean defaultMapping) {
        this.defaultMapping = defaultMapping;
    }

    /**
     * Returns true if the URL mapping is activated or false if it is not activated.
     * @return true if the URL mapping is activated or false if it is not activated.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the activation flag for the URL mapping - true if mapping is active,
     * or false if is not active (will not be assigned and resource can no longer be found via the mapping)
     * @param active true if mapping should be active, otherwise false
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the UUID of the mapping node in the JCR repository or null if URL is
     * not saved yet
     * @return the UUID of the mapping node in the JCR repository or null if not saved yet
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set the unique identifier of the node in the JCR repository
     * @param identifier the unique identifier (JCR repository node UUID)
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the path of the node in the JCR repository or null if not yet set
     * @return the path of the node in the JCR repository or null if not yet set
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the path of the node in the JCR repository
     * @param path the path of the node in the JCR repository
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the virtual site of the mapped content object
     * @return the virtual site of the mapped content object
     */
    public String getSite() {
        return site;
    }

    /**
     * Sets the virtual site of the mapped content object
     * @param site virtual site of the mapped content object
     */
    public void setSite(String site) {
        this.site = site;
    }

    /**
     * Is the vanity url for file access
     * @return true if file, false if render
     */
    public boolean isFile() {
        return file;
    }

    /**
     * Set if the vanity url is for file access
     * @param file true if file, false if render
     */
    public void setFile(boolean file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VanityUrl vanityUrl = (VanityUrl) o;

        if (defaultMapping != vanityUrl.defaultMapping) return false;
        if (active != vanityUrl.active) return false;
        if (url != null ? !url.equals(vanityUrl.url) : vanityUrl.url != null) return false;
        if (language != null ? !language.equals(vanityUrl.language) : vanityUrl.language != null) return false;
        if (identifier != null ? !identifier.equals(vanityUrl.identifier) : vanityUrl.identifier != null) return false;
        if (path != null ? !path.equals(vanityUrl.path) : vanityUrl.path != null) return false;
        return site != null ? site.equals(vanityUrl.site) : vanityUrl.site == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (site != null ? site.hashCode() : 0);
        result = 31 * result + (defaultMapping ? 1 : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
