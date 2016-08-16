package org.jahia.osgi.spring;

import java.util.Dictionary;
import java.util.Hashtable;

import org.jahia.bin.Jahia;
import org.osgi.framework.Constants;
import org.slf4j.Logger;

/**
 * Contains common functionality which may be useful for BeanExposer implementors.
 */
public class BeanExposerSupport {

    /**
     * OSGi service property that displays the ID of the original Spring bean.
     */
    public static final String SERVICE_PROPERTY_SPRING_BEAN_ID = "springBeanID";

    /**
     * OSGi service property that displays the class of the original Spring bean.
     */
    public static final String SERVICE_PROPERTY_SPRING_BEAN_CLASS = "springBeanClass";

    /**
     * Generate basic OSGi service properties.
     * @param beanID Spring bean ID
     * @param bean Spring bean to be exposed as an OSGi service
     * @return Basic OSGi service properties
     */
    protected static Dictionary<String, Object> getBasicServiceProperties(String beanID, Object bean) {
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_VENDOR, Jahia.VENDOR_NAME);
        properties.put(SERVICE_PROPERTY_SPRING_BEAN_ID, beanID);
        properties.put(SERVICE_PROPERTY_SPRING_BEAN_CLASS, bean.getClass().getName());
        return properties;
    }

    /**
     * Log information about bean exposed as an OSGi service.
     * @param logger Logger to use for logging
     * @param beanID Spring bean ID
     * @param bean Spring bean exposed as an OSGi service
     * @param clazz OSGi service class the bean was exposed as
     */
    protected static void logBeanExposed(Logger logger, String beanID, Object bean, Class<?> clazz) {
        logger.info("Bean '{}' of class '{}' exposed to OSGi as '{}'", new Object[] {beanID, bean.getClass().getName(), clazz.getName()});
    }
}
