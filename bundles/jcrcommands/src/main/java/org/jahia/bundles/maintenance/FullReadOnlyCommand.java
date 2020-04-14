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
package org.jahia.bundles.maintenance;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.settings.SettingsBean;
import org.jahia.settings.readonlymode.ReadOnlyModeController;
import org.jahia.settings.readonlymode.ReadOnlyModeStatusInfo;

import java.util.List;

@Command(scope = "jahia", name = "full-read-only")
@Service
public class FullReadOnlyCommand extends AbstractMaintenanceCommand {

    @Override
    protected void printMaintenanceStatus() {
        if (SettingsBean.getInstance().isClusterActivated()) {
            print(ReadOnlyModeController.getInstance().getReadOnlyStatuses());
        } else {
            super.printMaintenanceStatus();
        }
    }

    @Override
    protected String getMaintenanceStatus() {
        return ReadOnlyModeController.getInstance().getReadOnlyStatus().name();
    }

    @Override
    protected void setMaintenanceStatus(boolean enable) {
        try {
            ReadOnlyModeController.getInstance().switchReadOnlyMode(enable);
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
        }
    }

    private void print(List<ReadOnlyModeStatusInfo> statuses) {
        ShellTable table = new ShellTable();
        table.column(new Col("Origin"));
        table.column(new Col("Status"));
        for (ReadOnlyModeStatusInfo status : statuses) {
            table.addRow().addContent(status.getOrigin(), status.getValue());
        }
        table.print(System.out, true);
    }

}
