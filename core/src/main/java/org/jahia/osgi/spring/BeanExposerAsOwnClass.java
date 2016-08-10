package org.jahia.osgi.spring;

import java.text.MessageFormat;

import org.osgi.framework.BundleContext;

public class BeanExposerAsOwnClass extends BeanExposerSupport implements SpringBridge.BeanExposer {

    private Class<?> clazz;

    public BeanExposerAsOwnClass() {
    }

    public BeanExposerAsOwnClass(Class<?> ancestor) {
        this.clazz = ancestor;
    }

    @Override
    public void exposeBean(String beanID, Object bean, BundleContext bundleContext) {
        Class<?> cl;
        if (clazz == null) {
            cl = bean.getClass();
        } else {
            if (!clazz.isInstance(bean)) {
                throw new IllegalArgumentException(MessageFormat.format("'{0}' bean is not a {1} instance", clazz.getName()));
            }
            cl = clazz;
        }
        bundleContext.registerService(cl.getName(), bean, getBasicServiceProperties(beanID, bean));
    }
}
