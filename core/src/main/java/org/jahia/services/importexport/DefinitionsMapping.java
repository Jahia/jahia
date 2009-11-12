package org.jahia.services.importexport;

import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 29, 2009
 * Time: 7:09:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefinitionsMapping {
    Properties properties;

    public DefinitionsMapping() {
        this.properties = new Properties();

        properties.put("metadata.jahia:defaultCategory", "jmix:categorized.j:defaultCategory");
        properties.put("metadata.jahia:keywords", "jmix:keywords.j:keywords");
        properties.put("metadata.jahia:createdBy", "mix:created.jcr:createdBy");
        properties.put("metadata.jahia:lastModifiedBy", "mix:lastModified.jcr:lastModifiedBy");
        properties.put("metadata.jahia:lastPublishingDate", "jmix:lastPublished.j:lastPublished");
        properties.put("metadata.jahia:lastPublisher", "jmix:lastPublished.j:lastPublishedBy");
    }

    public void load(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        for (String line; (line=br.readLine())!= null;) {
            if (!line.startsWith("#") && line.indexOf('=')>0) {
                properties.put(StringUtils.substringBefore(line, "=").trim() , StringUtils.substringAfter(line, "=").trim()) ;
            }
        }
    }

    public Properties getProperties() {
        return properties;
    }

    private String[] getMappedTypeEntries(ExtendedNodeType type) {
        String key = type.getName();
        if (properties.containsKey(key)) {
            return properties.getProperty(key).split(",");
        }
        return null;
    }

    public String getMappedType(ExtendedNodeType type) {
        String[] s = getMappedTypeEntries(type);
        if (s != null) {
            return s[0];
        }
        if (type.isNodeType("jnt:box")) {
            return "#box";
        }
        return type.getName();
    }

    private String[] getMappedItemEntries(String baseName, String propertyName) {
        String key = baseName + "." + propertyName;
        if (properties.containsKey(key)) {
            return properties.getProperty(key).split(",");
        }
        key = "*." + propertyName;
        if (properties.containsKey(key)) {
            return properties.getProperty(key).split(",");
        }
        return null;
    }

    public String getMappedItem(String baseName, String propertyName) {
        String[] s = getMappedItemEntries(baseName, propertyName);
        if (s != null) {
            return s[0];
        }

        if (propertyName.endsWith("Title") || propertyName.equals(("title"))) {
            return "mix:title.jcr:title";
        }

        return propertyName;
    }

    private String[] getMappedPropertyValueEntries(String baseName, String propertyName, String value) {
        String key = baseName + "." + propertyName + "." + value;
        if (properties.containsKey(key)) {
            return properties.getProperty(key).split(",");
        }
        key = "*." + propertyName + "." + value;
        if (properties.containsKey(key)) {
            return properties.getProperty(key).split(",");
        }
        return null;
    }

    public String getMappedPropertyValue(String baseName, String propertyName, String value) {
        String s[] = getMappedPropertyValueEntries(baseName, propertyName, value);
        if (s != null) {
            return s[0];
        }

        return value;
    }


    public List<String> getMappedMixinTypesForType(ExtendedNodeType type) {
        return getMappedMixinTypes(getMappedTypeEntries(type));
    }

    public List<String> getMappedMixinTypesForItem(String baseName, String propertyName) {
        return getMappedMixinTypes(getMappedItemEntries(baseName, propertyName));
    }

    public List<String> getMappedMixinTypesForPropertyValue(String baseName, String propertyName, String value) {
        return getMappedMixinTypes(getMappedPropertyValueEntries(baseName, propertyName, value));
    }

    private List<String> getMappedMixinTypes(String[] s) {
        List<String> l = new ArrayList<String>();

        if (s != null) {
            for (int i = 1; i < s.length; i++) {
                String mix = s[i].trim();
                if (mix.startsWith("addMixin")) {
                    l.add(StringUtils.substringAfter(mix,"addMixin").trim());
                }
            }
        }
        return l;
    }



    public Map<String,String> getAutosetPropertiesForType(ExtendedNodeType type) {
        return getAutosetProperties(getMappedTypeEntries(type));
    }
    public Map<String,String> getAutosetPropertiesForItem(String baseName, String propertyName) {
        return getAutosetProperties(getMappedItemEntries(baseName, propertyName));
    }
    public Map<String,String> getAutosetPropertiesForPropertyValue(String baseName, String propertyName, String value) {
        return getAutosetProperties(getMappedPropertyValueEntries(baseName, propertyName, value));
    }

    private Map<String,String> getAutosetProperties(String[] s) {
        Map<String,String> m  = new HashMap<String,String>();

        if (s != null) {
            for (int i = 1; i < s.length; i++) {
                String mix = s[i].trim();
                if (mix.startsWith("setProperty")) {
                    String set = StringUtils.substringAfter(mix,"setProperty").trim();
                    m.put(StringUtils.substringBefore(set,"=").trim(), StringUtils.substringAfter(set,"=").trim());
                }
            }
        }

        return m;
    }


    @Override
    public String toString() {
        return "ImportMapping{" +
                "properties=" + properties +
                '}';
    }
}
