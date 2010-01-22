package org.jahia.services.render;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.bin.Jahia;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.io.IOUtils;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;

import javax.servlet.RequestDispatcher;

/**
 * Implementation of the {@link Template} that uses {@link RequestDispatcher} to forward to a JSP resource.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 7:20:38 PM
 */
public class FileSystemTemplate implements Comparable<FileSystemTemplate>, Template {
    private static Logger logger = Logger.getLogger(FileSystemTemplate.class);
    private String path;
    private String fileExtension;
    private String key;
    private JahiaTemplatesPackage ownerPackage;
    private String displayName;    
    private Properties properties;

    private static Map<String,Properties> propCache = new HashMap<String, Properties>();

    public FileSystemTemplate(String path, String key, JahiaTemplatesPackage ownerPackage, String displayName) {
        this.path = path;
        this.key = key;
        this.ownerPackage = ownerPackage;
        this.displayName = displayName;
        int lastDotPos = path.lastIndexOf(".");
        if (lastDotPos > 0) {
            this.fileExtension = path.substring(lastDotPos+1);
        }

        String propName = StringUtils.substringBeforeLast(path, "." + fileExtension) + ".properties";

        if (!propCache.containsKey(propName)) {
            properties = new Properties();            
            propCache.put(propName, properties);
            InputStream is = Jahia.getStaticServletConfig().getServletContext().getResourceAsStream(propName);
            if (is != null) {
                try {
                    properties.load(is);
                } catch (IOException e) {
                    logger.warn("Unable to read associated properties file under " + propName, e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        } else {
            properties = propCache.get(propName);
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
        return "Path dispatch : " + path;
    }

    /**
     * Return properties of the template
     *
     * @return
     */
    public Properties getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileSystemTemplate template = (FileSystemTemplate) o;

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

    public int compareTo(FileSystemTemplate template) {
        if (ownerPackage == null) {
            if (template.ownerPackage != null ) {
                return 1;
            } else {
                return key.compareTo(template.key);
            }
        } else {
            if (template.ownerPackage == null ) {
                return -1;
            } else if (!ownerPackage.equals(template.ownerPackage)) {
                return ownerPackage.getName().compareTo(template.ownerPackage.getName());
            } else {
                return key.compareTo(template.key);
            }
        }
    }
}
