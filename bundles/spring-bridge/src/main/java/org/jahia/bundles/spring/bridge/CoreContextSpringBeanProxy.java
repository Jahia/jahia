package org.jahia.bundles.spring.bridge;

import org.jahia.services.SpringContextSingleton;

/**
 * Simple factory that give the reference to the target bean.
 * Only useful as a workaround for osgi:service limitation, which require a local bean definition.
 */
public class CoreContextSpringBeanProxy implements org.springframework.beans.factory.FactoryBean {

    private Object target;

    public void setTarget(Object target) {
        this.target = target;
    }

    @Override
    public Object getObject() throws Exception {
        return target;
    }

    @Override
    public Class<?> getObjectType() {
        return  target.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
