/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
