package org.jahia.modules.serversettings.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.cache.CacheHelper;

import java.io.Serializable;

/**
 * Bean for cache management
 * User: david
 * Date: 3/19/13
 * Time: 11:15 AM
 */
public class CacheManagement implements Serializable {

    private boolean showActions;
    private boolean showConfig;
    private boolean showBytes;
    private String propagate;
    private String action;
    private String name;

    public boolean isShowActions() {
        return showActions;
    }

    public void setShowActions(boolean showActions) {
        this.showActions = showActions;
    }

    public boolean isShowConfig() {
        return showConfig;
    }

    public void setShowConfig(boolean showConfig) {
        this.showConfig = showConfig;
    }

    public boolean isShowBytes() {
        return showBytes;
    }

    public void setShowBytes(boolean showBytes) {
        this.showBytes = showBytes;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropagate() {
        return propagate;
    }

    public void setPropagate(String propagate) {
        this.propagate = propagate;
    }

}
