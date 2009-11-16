package org.jahia.services.render;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.bin.Jahia;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 28, 2009
* Time: 7:20:38 PM
* To change this template use File | Settings | File Templates.
*/
public class RequestDispatcherTemplate implements Comparable<RequestDispatcherTemplate>, Template {
    private String path;
    private String key;
    private JahiaTemplatesPackage ownerPackage;
    private String displayName;    
    private Properties properties;

    private static Map<String,Properties> propCache = new HashMap<String, Properties>();

    public RequestDispatcherTemplate(String path, String key, JahiaTemplatesPackage ownerPackage, String displayName) {
        this.path = path;
        this.key = key;
        this.ownerPackage = ownerPackage;
        this.displayName = displayName;

        String propName = StringUtils.substringBeforeLast(path, ".jsp") + ".properties";

        if (!propCache.containsKey(propName)) {
            properties = new Properties();            
            propCache.put(propName, properties);
            InputStream is = Jahia.getStaticServletConfig().getServletContext().getResourceAsStream(propName);
            if (is != null) {
                try {
                    properties.load(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            properties = propCache.get(propName);
        }
    }

    public String getPath() {
        return path;
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
        return "JSP dispatch : " + path;
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

        RequestDispatcherTemplate template = (RequestDispatcherTemplate) o;

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

    public int compareTo(RequestDispatcherTemplate template) {
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
