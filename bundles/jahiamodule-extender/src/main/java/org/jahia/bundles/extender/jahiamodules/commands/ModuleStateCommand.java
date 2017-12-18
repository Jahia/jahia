/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
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
