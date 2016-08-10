package org.jahia.osgi.spring;

import java.util.Dictionary;
import java.util.Hashtable;

public class BeanExposerSupport {

    protected Dictionary<String, Object> getBasicServiceProperties(String beanID, Object bean) {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("springBeanID", beanID);
        return properties;
    }
}
