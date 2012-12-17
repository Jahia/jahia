package org.jahia.bundles.extender.jahiamodules;

import org.osgi.framework.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        Map<Activator.ModuleState, List<Bundle>> modulesByState = new TreeMap<Activator.ModuleState, List<Bundle>>();
        for (Bundle bundle : moduleStates.keySet()) {
            Activator.ModuleState moduleState = moduleStates.get(bundle);
            List<Bundle> bundlesInState = modulesByState.get(moduleState);
            if (bundlesInState == null) {
                bundlesInState = new ArrayList<Bundle>();
            }
            bundlesInState.add(bundle);
            modulesByState.put(moduleState, bundlesInState);
        }

        for (Activator.ModuleState moduleState : modulesByState.keySet()) {
            System.out.println("Module State: " + moduleState);
            System.out.println("----------------------------------------");
            List<Bundle> bundlesInState = modulesByState.get(moduleState);
            for (Bundle bundleInState : bundlesInState) {
                JahiaBundleTemplatesPackage modulePackage = activator.getRegisteredBundles().get(bundleInState);
                List<String> dependsOn = new ArrayList<String>();
                if (modulePackage != null) {
                    dependsOn = modulePackage.getDepends();
                }
                System.out.println(bundleInState.getSymbolicName() + " v" + bundleInState.getVersion() + " depends on:" + dependsOn);
            }
        }
    }
}
