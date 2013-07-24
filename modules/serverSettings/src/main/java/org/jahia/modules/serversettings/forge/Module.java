package org.jahia.modules.serversettings.forge;

import org.jahia.services.templates.ModuleVersion;

import java.io.Serializable;

/**
 * Bean for Forge Module
 */
public class Module implements Serializable {

    private static final long serialVersionUID = 5507292105100115258L;
    private String name;
    private boolean used;
    private String version;
    private String downloadUrl;
    private String title;
    private String groupId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
