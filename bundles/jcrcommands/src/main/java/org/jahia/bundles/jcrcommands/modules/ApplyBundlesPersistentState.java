/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jcrcommands.modules;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.OperationResult;

/**
 * Short description of the class
 *
 * @author yousria
 */
@Command(
        scope = "bundle",
        name = "apply-bundle-states",
        description = "Apply persistent bundle states from JCR"
)
@Service
@SuppressWarnings({"java:S106","java:S1166"})
public class ApplyBundlesPersistentState implements Action {

    @Reference
    private ModuleManager moduleManager;

    @Argument(description = "target")
    private String target;

    @Override
    public Object execute() throws Exception {
        if (target == null) {
            target = "default";
        }

        OperationResult result = moduleManager.applyBundlesPersistentStates(target);
        if (result.getBundleInfos().isEmpty()) {
            System.out.println("All bundles status were up-to-date");
        } else {
            // Fill the table to output result.
            ShellTable table = new ShellTable();
            table.column(new Col("Symbolic-Name"));
            table.column(new Col("Version"));
            table.column(new Col("Location"));
            for (BundleInfo bundleInfo : result.getBundleInfos()) {
                table.addRow().addContent(bundleInfo.getSymbolicName(), bundleInfo.getVersion(), bundleInfo.getKey());
            }
            table.print(System.out, true);
        }

        return null;
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
}
