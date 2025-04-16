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
package org.jahia.bundles.provisioning.impl.operations;

import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.OperationResult;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.*;

/**
 * Uninstall module operation
 */
@Component(service = Operation.class, property = "type=uninstallModule")
public class UninstallModule implements Operation {
    // Legacy keys for backward compatibility
    public static final String UNINSTALL_BUNDLE = "uninstallBundle";

    // Valid keys
    public static final String UNINSTALL_MODULE = "uninstallModule";
    private static final Map<String, String> DEPRECATED_KEYS = Map.of(
            UNINSTALL_BUNDLE, UNINSTALL_MODULE
    );
    public static final String TARGET = "target";
    private ModuleManager moduleManager;

    @Reference
    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.containsKey(UNINSTALL_BUNDLE) || entry.containsKey(UNINSTALL_MODULE);
    }

    @Override
    public Map<String, String> getDeprecatedOperations() {
        return DEPRECATED_KEYS;
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        String cmd = entry.containsKey(UNINSTALL_BUNDLE) ? UNINSTALL_BUNDLE : UNINSTALL_MODULE;
        List<Map<String, Object>> entries = ProvisioningScriptUtil.convertToList(entry, cmd, "key");
        List<OperationResult> uninstallResults = new ArrayList<>();
        for (Map<String, Object> subEntry : entries) {
            uninstallResults.add(moduleManager.uninstall((String) subEntry.get(cmd), (String) entry.get(TARGET)));
        }
        if (executionContext.getContext().get("result") instanceof Collection) {
            ((Collection) executionContext.getContext().get("result")).add(Collections.singletonMap("uninstall", uninstallResults));
        }
    }

    @Override
    public String getType() {
        return UNINSTALL_MODULE;
    }

    @Override
    public String[] getAPIsForAccessControl() {
        return new String[]{UNINSTALL_MODULE, UNINSTALL_BUNDLE};
    }
}
