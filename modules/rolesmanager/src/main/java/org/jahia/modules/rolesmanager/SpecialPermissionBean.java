package org.jahia.modules.rolesmanager;

import java.util.List;
import java.util.Map;

public class SpecialPermissionBean extends PermissionBean {

    private List<String> targetPaths;
    private Map<String,Boolean> setForPath;

    public List<String> getTargetPaths() {
        return targetPaths;
    }

    public void setTargetPaths(List<String> targetPaths) {
        this.targetPaths = targetPaths;
    }

    public Map<String, Boolean> getSetForPath() {
        return setForPath;
    }

    public void setSetForPath(Map<String, Boolean> setForPath) {
        this.setForPath = setForPath;
    }
}
