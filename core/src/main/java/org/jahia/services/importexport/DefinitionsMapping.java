package org.jahia.services.importexport;

import org.jahia.services.content.nodetypes.ExtendedNodeType;

import java.util.Properties;
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
    }

    public void load(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        for (String line; (line=br.readLine())!= null;) {
            String[] s = line.split("=");
            properties.put(s[0].trim(), s[1].trim());
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public String getMappedType(ExtendedNodeType type) {
        String key = type.getName();
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        if (type.isNodeType("jnt:box")) {
            return "#box";
        }

        return key;
    }

    public String getMappedList(ExtendedNodeType parentType, String propertyName) {
        String key = parentType.getName() + "." + propertyName;
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }

        return propertyName;
    }

    public String getMappedField(ExtendedNodeType parentType, String propertyName) {
        String key = parentType.getName() + "." + propertyName;
        if (properties.containsKey(key)) {
            return properties.getProperty(key);
        }
        if (propertyName.endsWith("Title") || propertyName.equals(("title"))) {
            return "jcr:title";
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
