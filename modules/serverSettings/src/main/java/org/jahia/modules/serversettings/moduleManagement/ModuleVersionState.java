package org.jahia.modules.serversettings.moduleManagement;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * State information for a module version, including possible actions.
 * 
 * @author Sergiy Shyrkov
 */
public class ModuleVersionState implements Serializable {

    private static final long serialVersionUID = 7311222686981884149L;

    private boolean canBeStarted;

    private boolean canBeStopped;

    private boolean canBeUninstalled;

    private Set<String> dependencies = new TreeSet<String>();

    private boolean systemDependency;

    private Set<String> unresolvedDependencies = new TreeSet<String>();

    private Set<String> usedInSites = new TreeSet<String>();

    public Set<String> getDependencies() {
        return dependencies;
    }

    public Set<String> getUnresolvedDependencies() {
        return unresolvedDependencies;
    }

    public Set<String> getUsedInSites() {
        return usedInSites;
    }

    public boolean isCanBeStarted() {
        return canBeStarted;
    }

    public boolean isCanBeStopped() {
        return canBeStopped;
    }

    public boolean isCanBeUninstalled() {
        return canBeUninstalled;
    }

    public boolean isSystemDependency() {
        return systemDependency;
    }

    public void setCanBeStarted(boolean canBeStarted) {
        this.canBeStarted = canBeStarted;
    }

    public void setCanBeStopped(boolean canBeStopped) {
        this.canBeStopped = canBeStopped;
    }

    public void setCanBeUninstalled(boolean canBeUninstalled) {
        this.canBeUninstalled = canBeUninstalled;
    }

    public void setSystemDependency(boolean systemDependency) {
        this.systemDependency = systemDependency;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}