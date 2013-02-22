/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for resources views.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BaseView implements View, Comparable<View> {

    private static final Properties EMPTY_PROPERTIES = new Properties();

    private static final Logger logger = LoggerFactory.getLogger(BaseView.class);

    private static Map<String, Properties> propCache = new ConcurrentHashMap<String, Properties>(512);

    public static void clearPropertiesCache() {
        propCache.clear();
    }

    private Properties defaultProperties;

    private String displayName;

    private String fileExtension;

    private String key;

    private JahiaTemplatesPackage module;

    private String path;

    private Properties properties;

    /**
     * Initializes an instance of this class.
     * 
     * @param path
     *            the resource path
     * @param key
     *            the key of the view
     * @param module
     *            corresponding {@link JahiaTemplatesPackage} instance
     */
    protected BaseView(String path, String key, JahiaTemplatesPackage module) {
        super();
        this.path = path;
        this.key = key;
        this.module = module;
        this.fileExtension = StringUtils.defaultIfEmpty(StringUtils.substringAfterLast(path, "."), null);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param path
     *            the resource path
     * @param key
     *            the key of the view
     * @param module
     *            corresponding {@link JahiaTemplatesPackage} instance
     * @param displayName
     *            the display name for this view
     */
    protected BaseView(String path, String key, JahiaTemplatesPackage module, String displayName) {
        this(path, key, module);
        this.displayName = displayName;
    }

    @Override
    public int compareTo(View otherView) {
        if (module == null) {
            if (otherView.getModule() != null) {
                return 1;
            } else {
                return key.compareTo(otherView.getKey());
            }
        } else {
            if (otherView.getModule() == null) {
                return -1;
            } else if (!module.equals(otherView.getModule())) {
                return module.getName().compareTo(otherView.getModule().getName());
            } else {
                return key.compareTo(otherView.getKey());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BaseView other = (BaseView) o;

        if (path != null ? !path.equals(other.path) : other.path != null) {
            return false;
        }
        if (key != null ? !key.equals(other.key) : other.key != null) {
            return false;
        }
        if (module != null ? !module.equals(other.module) : other.module != null) {
            return false;
        }
        if (fileExtension != null ? !fileExtension.equals(other.fileExtension) : other.fileExtension != null) {
            return false;
        }
        if (displayName != null ? !displayName.equals(other.displayName) : other.displayName != null) {
            return false;
        }
//        if (defaultProperties != null ? !defaultProperties.equals(other.defaultProperties)
//                : other.defaultProperties != null) {
//            return false;
//        }
//        if (properties != null ? !properties.equals(other.properties) : other.properties != null) {
//            return false;
//        }

        return true;
    }

    @Override
    public Properties getDefaultProperties() {
        if (defaultProperties == null) {
            readProperties();
        }
        return defaultProperties;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String getInfo() {
        return "Path dispatch: " + path;
    }

    /**
     * Returns an input stream for the specified resource or null if the resource does not exist.
     * 
     * @param resource
     *            the resource path to obtain input stream for
     * @return an input stream for the specified resource or null if the resource does not exist
     * @throws IOException
     *             in case of an I/O errors
     */
    protected abstract InputStream getInputStream(String resource) throws IOException;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public JahiaTemplatesPackage getModule() {
        return module;
    }

    @Override
    public String getModuleVersion() {
        return module != null && module.getVersion() != null ? module.getVersion().toString() : null;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Properties getProperties() {
        if (properties == null) {
            readProperties();
        }
        return properties;
    }

    protected Properties getProperties(String path, boolean defaultProperties) {
        String pathWithoutExtension = defaultProperties ? StringUtils.substringBefore(path, ".") : StringUtils.substringBeforeLast(path, ".");
        Properties p = propCache.get(pathWithoutExtension);
        if (p == null) {
            p = loadProperties(pathWithoutExtension + ".properties", pathWithoutExtension + ".png");
            propCache.put(pathWithoutExtension, p);
        }

        return p;
    }

    protected abstract URL getResource(String resource);

    /**
     * Obtains the path for the specified resource which can be used to read it using corresponding script, e.g. request dispatcher. If the
     * resource does not exist, return <code>null</code>.
     * 
     * @param resource
     *            the resource to get the path for
     * @return the path for the specified resource which can be used to read it using corresponding script, e.g. request dispatcher. If the
     *         resource does not exist, return <code>null</code>
     */
    protected abstract String getResourcePath(String resource);

    @Override
    public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + module.hashCode();
        result = 31 * result + (fileExtension != null ? fileExtension.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
//        result = 31 * result + (properties != null ? properties.hashCode() : 0);
//        result = 31 * result + (defaultProperties != null ? defaultProperties.hashCode() : 0);
        return result;
    }

    protected Properties loadProperties(String path, String thumbnailPath) {
        Properties p = EMPTY_PROPERTIES;
        InputStream is = null;
        try {
            is = getInputStream(path);
            if (is != null) {
                p = new Properties();
                p.load(is);
            }
        } catch (IOException e) {
            logger.warn("Unable to read associated properties file under " + path, e);
        } finally {
            IOUtils.closeQuietly(is);
        }

        // add thumbnail to the properties if exists
        String thumbnailURL = getResourcePath(thumbnailPath);
        if (thumbnailURL != null) {
            properties.put("image", thumbnailURL);
        }

        return p;
    }

    protected void readProperties() {
        properties = getProperties(path, false);
        defaultProperties = getProperties(path, true);
    }

    @Override
    public String toString() {
        return getInfo();
    }
}
