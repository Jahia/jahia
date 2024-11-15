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
package org.jahia.bundles.jcrcommands.maintenance;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.support.completers.StringsCompleter;

/**
 * BAse Command for maintenance operations
 */
@SuppressWarnings({"java:S106","java:S1166"})
public abstract class AbstractMaintenanceCommand implements Action {

    @Argument(description = "on/off")
    @Completion(value = StringsCompleter.class , values = {"ON", "OFF"})
    private String enable;

    @Override
    public final Object execute() throws Exception {
        try {
            if (enable != null) {
                if (!enable.equalsIgnoreCase("on") && !enable.equalsIgnoreCase("off")) {
                    System.out.println("Please choose ON or OFF");
                } else {
                    setMaintenanceStatus(enable.equalsIgnoreCase("on"));
                }
            }
        } finally {
            printMaintenanceStatus();
        }
        return null;
    }

    protected void printMaintenanceStatus() {
        System.out.println("Current status: " + getMaintenanceStatus());
    }

    protected abstract String getMaintenanceStatus();

    protected abstract void setMaintenanceStatus(boolean enable);
}
