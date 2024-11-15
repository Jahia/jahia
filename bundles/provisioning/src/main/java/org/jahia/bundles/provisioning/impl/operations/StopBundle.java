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
package org.jahia.bundles.provisioning.impl.operations;

import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.OperationResult;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.*;

/**
 * Stop bundle operation
 */
@Component(service = Operation.class, property = "type=stopBundle")
public class StopBundle implements Operation {
    public static final String STOP_BUNDLE = "stopBundle";
    public static final String TARGET = "target";
    private ModuleManager moduleManager;

    @Reference
    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.containsKey(STOP_BUNDLE);
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        List<Map<String, Object>> entries = ProvisioningScriptUtil.convertToList(entry, STOP_BUNDLE, "key");
        List<OperationResult> stopResults = new ArrayList<>();
        for (Map<String, Object> subEntry : entries) {
            stopResults.add(moduleManager.stop((String) subEntry.get(STOP_BUNDLE), (String) entry.get(TARGET)));
        }
        if (executionContext.getContext().get("result") instanceof Collection) {
            ((Collection) executionContext.getContext().get("result")).add(Collections.singletonMap("stop", stopResults));
        }
    }
}
