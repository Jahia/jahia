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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.provisioning.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.jahia.services.provisioning.ProvisioningManager;
import org.jahia.settings.SettingsBean;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service to provision bundles/features/configs/content with script
 */
@Component(service = ProvisioningManager.class, immediate = true)
public class ProvisioningManagerImpl implements ProvisioningManager {
    private static final Logger logger = LoggerFactory.getLogger(ProvisioningManagerImpl.class);

    private Collection<Operation> operations = new ArrayList<>();
    private ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Activate
     */
    @Activate
    public void activate() {

        String script = System.getProperty("executeProvisioningScript");
        if (script != null) {
            try {
                executeScript(new URL(script));
            } catch (IOException e) {
                logger.error("Cannot read script {}", script, e);
            }
        }
    }

    /**
     * Register operation service
     *
     * @param operation operation
     */
    @Reference(service = Operation.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void addOperation(Operation operation) {
        this.operations.add(operation);
    }

    /**
     * Unregister operation service
     *
     * @param operation operation
     */
    public void removeOperation(Operation operation) {
        this.operations.remove(operation);
    }

    @Override
    public void executeScript(URL url) throws IOException {
        logger.info("Execute script at {}", url);
        executeScript(parseScript(url));
    }

    @Override
    public void executeScript(String content, String format) throws IOException {
        executeScript(parseScript(content, format));
    }

    @Override
    public void executeScript(List<Map<String, Object>> script) {
        executeScript(script, Collections.emptyMap());
    }

    @Override
    public void executeScript(List<Map<String, Object>> script, Map<String,Object> context) {
        ExecutionContext executionContext = new ExecutionContext(this);
        executionContext.getContext().putAll(context);

        operations.forEach(op -> op.init(executionContext));

        executeScript(script, executionContext);

        operations.forEach(op -> op.cleanup(executionContext));
    }

    @Override
    public void executeScript(List<Map<String, Object>> script, ExecutionContext executionContext) {
        script.forEach(entry ->
        {
            Optional<Operation> operation = operations.stream()
                        .filter(op -> op.canHandle(entry))
                        .findFirst();
            if (operation.isPresent()) {
                operation.ifPresent(op -> op.perform(entry, executionContext));
            } else {
                logger.warn("Operation {} not found", Joiner.on(",").withKeyValueSeparator("=").useForNull("").join(entry));
            }
        }
        );
    }

    @Override
    public List<Map<String, Object>> parseScript(URL url) throws IOException {
        String format = "json";

        if ("mvn".equals(url.getProtocol())) {
            String[] parts = StringUtils.split(url.getFile(), "/");
            if (parts.length >= 4) {
                format = parts[3];
            }
        } else {
            format = StringUtils.substringAfterLast(url.getFile(), ".");
        }
        return parseScript(IOUtils.toString(url, StandardCharsets.UTF_8), format);
    }

    @Override
    public List<Map<String, Object>> parseScript(String content, String format) throws IOException {
        boolean yaml = format.equalsIgnoreCase("yml") || format.equalsIgnoreCase("yaml");
        content = SettingsBean.getInstance().replaceBySubsitutor(content);
        ObjectMapper objectMapper = yaml ? yamlMapper : jsonMapper;
        try {
            return objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            e.clearLocation();
            throw e;
        }
    }
}
