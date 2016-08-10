package org.jahia.osgi.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;

public class BeanSelectorByBeanID implements SpringBridge.BeanSelector {

    private Collection<String> beanIDs;

    public BeanSelectorByBeanID(String beanID) {
        this.beanIDs = Collections.singleton(beanID);
    }

    public BeanSelectorByBeanID(Collection<String> beanIDs) {
        this.beanIDs = new ArrayList<String>(beanIDs);
    }

    @Override
    public Map<String, Object> selectBeans(ApplicationContext applicationContext) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>(beanIDs.size());
        for (String beanID : beanIDs) {
            Object bean = applicationContext.getBean(beanID);
            result.put(beanID, bean);
        }
        return result;
    }
}
