/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jcrcommands.maintenance;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Col;
import org.apache.karaf.shell.support.table.ShellTable;
import org.jahia.settings.SettingsBean;
import org.jahia.settings.readonlymode.ReadOnlyModeController;
import org.jahia.settings.readonlymode.ReadOnlyModeStatusInfo;

import java.util.List;

/**
 * Switch to full read only command
 */
@Command(scope = "jahia", name = "full-read-only")
@Service
@SuppressWarnings({"java:S106","java:S1166"})
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
