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

import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.jahia.services.provisioning.ProvisioningManager;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Include operation
 */
@Component(service = Operation.class, property = "type=include")
public class Include implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(Include.class);
    public static final String INCLUDE_SCRIPT = "include";

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.containsKey(INCLUDE_SCRIPT);
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        List<Map<String, Object>> entries = ProvisioningScriptUtil.convertToList(entry, INCLUDE_SCRIPT, "url");
        for (Map<String, Object> subEntry : entries) {
            String include = (String) subEntry.get(INCLUDE_SCRIPT);
            try {
                ProvisioningManager manager = executionContext.getProvisioningManager();
                List<Map<String, Object>> script = manager.parseScript(ProvisioningScriptUtil.getResource(include, executionContext).getURL());
                manager.executeScript(script, executionContext);
            } catch (Exception e) {
                logger.error("Cannot include {}", include, e);
            }
        }
    }

    @Override
    public String getType() {
        return INCLUDE_SCRIPT;
    }
}
