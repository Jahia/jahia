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
package org.jahia.bundles.provisioning.impl.operations;

import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.jahia.tools.patches.Patcher;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Execute script operation
 */
@Component(service = Operation.class, property = "type=executeScript")
public class ExecuteScript implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(ExecuteScript.class);
    public static final String EXECUTE_SCRIPT = "executeScript";

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.containsKey(EXECUTE_SCRIPT);
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        List<Map<String, Object>> entries = ProvisioningScriptUtil.convertToList(entry, EXECUTE_SCRIPT, "url");
        for (Map<String, Object> subEntry : entries) {
            String script = (String) subEntry.get(EXECUTE_SCRIPT);
            if (script != null) {
                try {
                    Patcher.getInstance().executeScripts(Collections.singleton(ProvisioningScriptUtil.getResource(script, executionContext)), "",
                            (resource, s) -> {
                                logger.info("Script {} result: {}", script, s);
                                if (executionContext.getContext().get("result") instanceof Collection) {
                                    ((Collection) executionContext.getContext().get("result")).add(s);
                                }
                            }
                    );
                } catch (Exception e) {
                    logger.error("Cannot include {}", script, e);
                }
            }
        }

    }

    @Override
    public String getType() {
        return EXECUTE_SCRIPT;
    }
}
