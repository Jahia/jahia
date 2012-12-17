package org.jahia.bundles.extender.jahiamodules;

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
        Map<Bundle, Activator.ModuleState> moduleStates = activator.getModuleStates();

        Map<Activator.ModuleState, Set<Bundle>> modulesByState = new TreeMap<Activator.ModuleState, Set<Bundle>>();
        for (Bundle bundle : moduleStates.keySet()) {
            Activator.ModuleState moduleState = moduleStates.get(bundle);
            Set<Bundle> bundlesInState = modulesByState.get(moduleState);
            if (bundlesInState == null) {
                bundlesInState = new TreeSet<Bundle>();
            }
            bundlesInState.add(bundle);
            modulesByState.put(moduleState, bundlesInState);
        }

        for (Activator.ModuleState moduleState : modulesByState.keySet()) {
            System.out.println("");
            System.out.println("Module State: " + moduleState);
            System.out.println("----------------------------------------");
            Set<Bundle> bundlesInState = modulesByState.get(moduleState);
            for (Bundle bundleInState : bundlesInState) {
                JahiaBundleTemplatesPackage modulePackage = activator.getRegisteredBundles().get(bundleInState);
                String dependsOn = "";
                if (modulePackage != null) {
                    dependsOn = " depends on " + modulePackage.getDepends();
                }
                System.out.println(bundleInState.getBundleId() + " : " + bundleInState.getSymbolicName() + " v" + bundleInState.getVersion() + dependsOn);
            }
        }
    }
}
