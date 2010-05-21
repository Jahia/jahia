package org.jahia.services.uicomponents.bean.contentmanager;

import org.jahia.services.uicomponents.bean.Visibility;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 12, 2010
 * Time: 3:03:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Repository implements Serializable {
    private String key;
    private String title;
    private String titleKey;
    private Visibility visibility;
    private List<String> pathes;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public List<String> getPathes() {
        return pathes;
    }

    public void setPathes(List<String> pathes) {
        this.pathes = pathes;
    }
}