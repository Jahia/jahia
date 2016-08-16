package org.jahia.osgi.spring;

import java.text.MessageFormat;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposes a bean as one of bean's own classes (either bean class, or one of the super classes,
 * or one of the interfaces implemented), dependent on exposer's own configuration.
 */
public class BeanExposerAsOwnClass extends BeanExposerSupport implements SpringBridge.BeanExposer {

    private static final Logger logger = LoggerFactory.getLogger(BeanExposerAsOwnClass.class);

    private Class<?> clazz;

    /**
     * Create an instance that will expose a bean as its class as returned by bean.getClass()
     */
    public BeanExposerAsOwnClass() {
    }

    /**
     * Create an instance that will expose a bean as a specific class or interface;
     * the class/interface must be extended/implemented by the bean class.
     * @param clazz Class or interface to expose a bean as
     */
    public BeanExposerAsOwnClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void exposeBean(String beanID, Object bean, BundleContext bundleContext) {
        Class<?> cl;
        if (clazz == null) {
            cl = bean.getClass();
        } else {
            if (!clazz.isInstance(bean)) {
                throw new IllegalArgumentException(MessageFormat.format("{0} bean is not a {1} instance", clazz.getName()));
            }
            cl = clazz;
        }
        bundleContext.registerService(cl.getName(), bean, getBasicServiceProperties(beanID, bean));
        logBeanExposed(logger, beanID, bean, cl);
    }
}
