package org.jahia.bundles.extender.jahiamodules.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.bundles.extender.jahiamodules.Activator;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleUtils;
import org.osgi.framework.Bundle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

/**
 * Created by loom on 06.01.16.
 */
@Command(scope = "jahia", name = "modules", description="List Jahia modules with their state")
@Service
public class ModuleStateCommand implements Action {
    @Override
    public Object execute() throws Exception {
        Map<ModuleState.State, Set<Bundle>> modulesByState = Activator.getInstance().getModulesByState();

        ShellTable table = new ShellTable();
        table.column(new Col("Id"));
        table.column(new Col("State"));
        table.column(new Col("Symbolic-Name"));
        table.column(new Col("Version"));
        table.column(new Col("Depends on"));
        table.column(new Col("Details"));

        for (ModuleState.State moduleState : modulesByState.keySet()) {
            Set<Bundle> bundlesInState = modulesByState.get(moduleState);
            for (Bundle bundleInState : bundlesInState) {
                ModuleState bundleModuleState = Activator.getInstance().getModuleState(bundleInState);
                JahiaTemplatesPackage modulePackage = BundleUtils.getModule(bundleInState);
                String dependsOn = "";
                if (modulePackage != null) {
                    dependsOn = modulePackage.getDepends().toString();
                }
                String details = "";
                if (bundleModuleState.getDetails() != null) {
                    if (bundleModuleState.getDetails() instanceof Throwable) {
                        Throwable t = (Throwable) bundleModuleState.getDetails();
                        StringWriter stringWriter = new StringWriter();
                        t.printStackTrace(new PrintWriter(stringWriter));
                        details = stringWriter.getBuffer().toString();
                    } else {
                        details = bundleModuleState.getDetails().toString();
                    }
                }
                table.addRow().addContent(bundleInState.getBundleId(),
                        moduleState,
                        bundleInState.getSymbolicName(),
                        bundleInState.getHeaders().get("Implementation-Version"),
                        dependsOn,
                        details);
            }
        }

        table.print(System.out, true);
        return null;
    }
}
