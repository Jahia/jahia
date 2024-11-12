/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Base class for resources views.
 *
 * @author Sergiy Shyrkov
 */
public abstract class BaseView implements View, Comparable<View> {

    private static final Logger logger = LoggerFactory.getLogger(BaseView.class);

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
//        if (defaultProperties == null) {
//            readProperties();
//        }
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
        String modulePath = module.getSourcesFolder() != null ? getModule().getSourcesFolder().getPath() + "/src/main/resources" : "/modules/" + module
                .getIdWithVersion();
        return "Path dispatch: " + modulePath +  path;
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
//        if (properties == null) {
//            readProperties();
//        }
        return properties;
    }

//    protected Properties getProperties(String path, boolean defaultProperties) {
//        String pathWithoutExtension = defaultProperties ? StringUtils.substringBefore(path, ".") : StringUtils.substringBeforeLast(path, ".");
//        Properties p = propCache.get(pathWithoutExtension);
//        if (p == null) {
//            p = loadProperties(pathWithoutExtension + ".properties", pathWithoutExtension + ".png");
//            propCache.put(pathWithoutExtension, p);
//        }
//
//        return p;
//    }

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
        Properties p = new Properties();
        InputStream is = null;
        try {
            is = getInputStream(path);
            if (is != null) {
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

//    protected void readProperties() {
//        properties = getProperties(path, false);
//        defaultProperties = getProperties(path, true);
//    }


    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setDefaultProperties(Properties defaultProperties) {
        this.defaultProperties = defaultProperties;
    }

    @Override
    public String toString() {
        return getInfo();
    }
}
