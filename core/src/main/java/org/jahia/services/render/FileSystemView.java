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
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.templates.ModuleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStream;
import java.io.IOException;

import javax.servlet.RequestDispatcher;

/**
 * Implementation of the {@link View} that uses {@link RequestDispatcher} to forward to a JSP resource.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 7:20:38 PM
 *
 * @deprecated in favour of {@link BundleView}
 */
@Deprecated
public class FileSystemView implements Comparable<View>, View {
    private static final Properties EMPTY_PROPERTIES = new Properties();
    private static Logger logger = LoggerFactory.getLogger(FileSystemView.class);
    private String path;
    private String fileExtension;
    private String key;
    private JahiaTemplatesPackage ownerPackage;
    private String moduleVersion;
    private String displayName;
    private Properties properties;
    private Properties defaultProperties;
    public static final String THUMBNAIL = "image";

    private static Map<String,Properties> propCache = new ConcurrentHashMap<String, Properties>();

    public static void clearPropertiesCache() {
        propCache.clear();
    }

    public FileSystemView(String path, String key, JahiaTemplatesPackage ownerPackage, ModuleVersion version, String displayName) {
        this.path = path;
        this.key = key;
        this.ownerPackage = ownerPackage;
        this.moduleVersion = StringUtils.defaultIfEmpty(version.toString(), null);
        this.displayName = displayName;
        int lastDotPos = path.lastIndexOf(".");
        if (lastDotPos > 0) {
            this.fileExtension = path.substring(lastDotPos+1);
        }

        String pathWithoutExtension = path.substring(0, lastDotPos);
        String propName = pathWithoutExtension + ".properties";
        String absolutePath = StringUtils.substringBeforeLast(path,File.separator);
        String fileName = StringUtils.substringAfterLast(path,File.separator);
        String pathBeforeDot = StringUtils.substringBefore(fileName, ".");
        String defaultPropName = pathBeforeDot + ".properties";
        String thumbnail = pathWithoutExtension + ".png";
        String defaultThumbnail = pathBeforeDot + ".png";
        getProperties(propName, thumbnail, false);
        getProperties(absolutePath+File.separator+defaultPropName, defaultThumbnail, true);
    }

    private void getProperties(String propName, String thumbnail, boolean defaultProps) {
        Properties properties = propCache.get(propName);
        if (properties == null) {
            properties = EMPTY_PROPERTIES;
            InputStream is = JahiaContextLoaderListener.getServletContext().getResourceAsStream(propName);
            if (is != null) {
                properties = new Properties();
                try {
                    properties.load(is);
                } catch (IOException e) {
                    logger.warn("Unable to read associated properties file under " + propName, e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
            // add thumbnail to properties

//            if (TemplateUtils.isResourceAvailable(thumbnail)) {
//                properties.put(THUMBNAIL, Jahia.getContextPath() + thumbnail);
//            }

            propCache.put(propName, properties);
        }

        if(defaultProps) {
            this.defaultProperties = properties;
        } else {
            this.properties = properties;
        }
    }

    public String getPath() {
        return path;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getKey() {
        return key;
    }

    public JahiaTemplatesPackage getModule() {
        return ownerPackage;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Return printable information about the script : type, localization, file, .. in order to help
     * template developer to find the original source of the script
     *
     * @return
     */
    public String getInfo() {
        return "Path dispatch: " + path;
    }

    /**
     * Return properties of the template
     *
     * @return
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Return default properties of the node type template
     *
     * @return
     */
    public Properties getDefaultProperties() {
        return defaultProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileSystemView template = (FileSystemView) o;

        if (displayName != null ? !displayName.equals(template.displayName) : template.displayName != null) {
            return false;
        }
        if (key != null ? !key.equals(template.key) : template.key != null) {
            return false;
        }
        if (ownerPackage != null ? !ownerPackage.equals(template.ownerPackage) : template.ownerPackage != null) {
            return false;
        }
        if (path != null ? !path.equals(template.path) : template.path != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (ownerPackage != null ? ownerPackage.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        return result;
    }

    public int compareTo(View template) {
        if (ownerPackage == null) {
            if (template.getModule() != null ) {
                return 1;
            } else {
                return key.compareTo(template.getKey());
            }
        } else {
            if (template.getModule() == null ) {
                return -1;
            } else if (!ownerPackage.equals(template.getModule())) {
                return ownerPackage.getName().compareTo(template.getModule().getName());
            } else {
                return key.compareTo(template.getKey());
            }
        }
    }
}
