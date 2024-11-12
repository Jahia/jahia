/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jcrcommands.provisioning;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.services.provisioning.ProvisioningManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Execute a single provisioning operation
 */
@Command(scope = "provisioning",
        name = "operation",
        description = "Execute a single provisioning operation",
        detailedDescription = "provisioning:operation installBundle \"mvn:org.jahia.modules/article/3.0.0\"")
@Service
@SuppressWarnings({"java:S106","java:S1166"})
public class ExecuteOperation implements Action {
    @Reference
    private ProvisioningManager provisioningManager;

    @Argument(description = "Operation arguments", multiValued = true)
    private String[] args;

    @Override
    public Object execute() throws Exception {
        if (args.length == 0 || args.length % 2 != 0) {
            System.err.println("Invalid arguments");
        } else {
            Map<String, Object> entry = new HashMap<>();
            for (int i = 0; i < args.length; i += 2) {
                entry.put(args[i], args[i + 1]);
            }
            provisioningManager.executeScript(Collections.singletonList(entry));
        }
        return null;
    }

}
