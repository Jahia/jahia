package org.jahia.services.uicomponents.bean.contentmanager;

import org.jahia.services.uicomponents.bean.Visibility;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 12, 2010
 * Time: 3:03:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Column implements Serializable {
    private String key;
    private String title;
    private String titleKey;
    private String declaringNodeType;
    private int size = 100;

    private Visibility visibility;

    public Column() {
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

    public String getDeclaringNodeType() {
        return declaringNodeType;
    }

    public void setDeclaringNodeType(String declaringNodeType) {
        this.declaringNodeType = declaringNodeType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}