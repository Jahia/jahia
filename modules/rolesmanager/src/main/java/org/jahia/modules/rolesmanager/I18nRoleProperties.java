package org.jahia.modules.rolesmanager;

import java.io.Serializable;

public class I18nRoleProperties implements Serializable {
    private String language;
    private String title = "";
    private String description = "";

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}