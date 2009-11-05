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

    public String getMappedType(ExtendedNodeType type) {
        String key = type.getName();
        if (properties.containsKey(key)) {
            return properties.getProperty(key).split(",")[0];
        }
        if (type.isNodeType("jnt:box")) {
            return "#box";
        }

        return key;
    }

    public List<String> getMappedMixinTypes(ExtendedNodeType type) {
        List<String> l = new ArrayList<String>();

        String key = type.getName();
        if (properties.containsKey(key)) {
            String[] s = properties.getProperty(key).split(",");
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
        Map<String,String> m  = new HashMap<String,String>();

        String key = type.getName();
        if (properties.containsKey(key)) {
            String[] s = properties.getProperty(key).split(",");
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

    public String getMappedList(ExtendedNodeType parentType, String propertyName) {
        String key = parentType.getName() + "." + propertyName;
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }

        return propertyName;
    }

    public String getMappedPropertyValue(String baseName, String propertyName, String value) {
        String key = baseName + "." + propertyName + "." + value;
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        key = "*." + propertyName + "." + value;
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }

        return value;
    }

    public String getMappedField(String baseName, String propertyName) {
        String key = baseName + "." + propertyName;
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        key = "*." + propertyName;
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        if (propertyName.endsWith("Title") || propertyName.equals(("title"))) {
            return "mix:title.jcr:title";
        }

        return propertyName;
    }

    @Override
    public String toString() {
        return "ImportMapping{" +
                "properties=" + properties +
                '}';
    }
}
