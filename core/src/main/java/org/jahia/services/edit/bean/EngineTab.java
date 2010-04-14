package org.jahia.services.edit.bean;

import org.jahia.services.toolbar.bean.Visibility;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 14, 2010
 * Time: 12:37:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class EngineTab implements Serializable, BeanNameAware {
    private String name;
    private String key;
    private Visibility visibility;

    public EngineTab() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}
