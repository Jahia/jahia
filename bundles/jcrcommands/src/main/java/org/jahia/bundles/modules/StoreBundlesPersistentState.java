/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.modules;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.services.modulemanager.BundlePersistentInfo;
import org.jahia.services.modulemanager.ModuleManager;

import java.util.Collection;

@Command(
    scope = "bundle",
    name = "store-all-local-persistent-states",
    description = "Output DX modules with their persistent state and version, and save those information in the JCR"
)
@Service
public class StoreBundlesPersistentState implements Action {

    @Reference
    private ModuleManager moduleManager;

    @Override
    public Object execute() throws Exception {

        Collection<BundlePersistentInfo> modules = moduleManager.storeAllLocalPersistentStates();

        // Fill the table to output result.
        ShellTable table = new ShellTable();
        table.column(new Col("Symbolic-Name"));
        table.column(new Col("State"));
        table.column(new Col("Version"));
        table.column(new Col("Location"));
        table.column(new Col("Start-Level"));
        for (BundlePersistentInfo module : modules) {
            table.addRow().addContent(
                module.getSymbolicName(),
                module.getState(),
                module.getVersion(),
                module.getLocation(),
                module.getStartLevel()
            );
        }

        table.print(System.out, true);

        return null;
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
}
