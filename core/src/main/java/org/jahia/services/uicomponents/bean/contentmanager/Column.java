package org.jahia.services.uicomponents.bean.contentmanager;

import org.jahia.services.uicomponents.bean.Visibility;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 12, 2010
 * Time: 3:03:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Column implements Serializable, BeanNameAware {
    private String name;
    private String key;
    private Visibility visibility;

    public Column() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
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
}