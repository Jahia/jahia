package org.jahia.bundles.extender.jahiamodules.render;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.render.View;
import org.jahia.services.templates.ModuleVersion;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Implementation of a view stored in an OSGi bundle
 */
public class BundleView implements Comparable<View>, View {
    private static final Properties EMPTY_PROPERTIES = new Properties();

    private static Logger logger = LoggerFactory.getLogger(BundleView.class);

    private Bundle bundle;
    private String path;
    private String key;
    private JahiaTemplatesPackage module;
    private String moduleVersion;
    private String displayName;
    private String fileExtension;
    private Properties properties;
    private Properties defaultProperties;
    public static String THUMBNAIL = "image";

    private static Map<String,Properties> propCache = new HashMap<String, Properties>();

    public static void clearPropertiesCache() {
        propCache.clear();
    }

    public BundleView(Bundle bundle, String path, String key, JahiaTemplatesPackage module, ModuleVersion moduleVersion, String displayName) {
        this.bundle = bundle;
        this.path = path;
        this.key = key;
        this.module = module;
        this.moduleVersion = StringUtils.defaultIfEmpty(moduleVersion.toString(), null);
        this.displayName = displayName;
        int lastDotPos = path.lastIndexOf(".");
        if (lastDotPos > 0) {
            this.fileExtension = path.substring(lastDotPos+1);
        }

        String pathWithoutExtension = path.substring(0, lastDotPos);
        String propName = pathWithoutExtension + ".properties";
        String pathBeforeDot = StringUtils.substringBefore(path, ".");
        String defaultPropName = pathBeforeDot + ".properties";
        String thumbnail = pathWithoutExtension + ".png";
        String defaultThumbnail = pathBeforeDot + ".png";
        getProperties(propName, thumbnail, false);
        getProperties(defaultPropName, defaultThumbnail, true);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BundleView that = (BundleView) o;

        if (!bundle.equals(that.bundle)) return false;
        if (defaultProperties != null ? !defaultProperties.equals(that.defaultProperties) : that.defaultProperties != null)
            return false;
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) return false;
        if (fileExtension != null ? !fileExtension.equals(that.fileExtension) : that.fileExtension != null)
            return false;
        if (!key.equals(that.key)) return false;
        if (!module.equals(that.module)) return false;
        if (moduleVersion != null ? !moduleVersion.equals(that.moduleVersion) : that.moduleVersion != null)
            return false;
        if (!path.equals(that.path)) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = bundle.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + module.hashCode();
        result = 31 * result + (moduleVersion != null ? moduleVersion.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (fileExtension != null ? fileExtension.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (defaultProperties != null ? defaultProperties.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(View bundleView) {
        if (module == null) {
            if (bundleView.getModule() != null ) {
                return 1;
            } else {
                return key.compareTo(bundleView.getKey());
            }
        } else {
            if (bundleView.getModule() == null ) {
                return -1;
            } else if (!module.equals(bundleView.getModule())) {
                return module.getName().compareTo(bundleView.getModule().getName());
            } else {
                return key.compareTo(bundleView.getKey());
            }
        }
    }

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
        return moduleVersion;
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
    public String getPath() {
        return path;
    }

    public Bundle getBundle() {
        return bundle;
    }

    @Override
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

    private void getProperties(String propName, String thumbnail, boolean defaultProps) {
        Properties properties = propCache.get(propName);
        if (properties == null) {
            properties = EMPTY_PROPERTIES;
            URL resourceURL = bundle.getResource(propName);
            if (resourceURL != null) {
                properties = new Properties();
                InputStream is = null;
                try {
                    is = resourceURL.openStream();
                    properties.load(is);
                } catch (IOException e) {
                    logger.warn("Unable to read associated properties file under " + propName, e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
            // add thumbnail to properties

            URL thumbnailURL = bundle.getResource(thumbnail);
            if (thumbnailURL != null) {
                properties.put(THUMBNAIL, thumbnailURL); // @todo build a real URL to serve a resource from a bundle (and also build proper servlet to serve them)
            }

            propCache.put(propName, properties);
        }

        if(defaultProps) {
            this.defaultProperties = properties;
        } else {
            this.properties = properties;
        }
    }

}
