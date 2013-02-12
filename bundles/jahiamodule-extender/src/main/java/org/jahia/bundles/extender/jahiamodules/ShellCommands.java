package org.jahia.bundles.extender.jahiamodules;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.osgi.framework.Bundle;

import java.util.*;

/**
 * Basic Jahia extender shell commands
 * @todo we might want to move this to a separate bundle later.
 */
public class ShellCommands {

    private Activator activator;

    public ShellCommands(Activator activator) {
        this.activator = activator;
    }

    public void modules() {

        Map<Activator.ModuleState, Set<Bundle>> modulesByState = activator.getModulesByState();
        for (Activator.ModuleState moduleState : modulesByState.keySet()) {
            System.out.println("");
            System.out.println("Module State: " + moduleState);
            System.out.println("----------------------------------------");
            Set<Bundle> bundlesInState = modulesByState.get(moduleState);
            for (Bundle bundleInState : bundlesInState) {
                JahiaTemplatesPackage modulePackage = activator.getRegisteredBundles().get(bundleInState);
                String dependsOn = "";
                if (modulePackage != null) {
                    dependsOn = " depends on " + modulePackage.getDepends();
                }
                System.out.println(bundleInState.getBundleId() + " : " + bundleInState.getSymbolicName() + " v" + bundleInState.getHeaders().get("Implementation-Version") + dependsOn);
            }
        }
    }
}
