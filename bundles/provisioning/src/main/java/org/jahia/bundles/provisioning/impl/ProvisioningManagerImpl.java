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
package org.jahia.bundles.provisioning.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;
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
    private StringSubstitutor stringSubstitutor;
    private ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Activate
     */
    @Activate
    public void activate() {
        Map<String, StringLookup> l = new HashMap<>();
        l.put("jahia", key -> SettingsBean.getInstance().getPropertyValue(key));
        stringSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.interpolatorStringLookup(l, null, true));
        stringSubstitutor.setEnableSubstitutionInVariables(true);

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
                operations.stream()
                        .filter(op -> op.canHandle(entry))
                        .findFirst()
                        .ifPresent(op -> op.perform(entry, executionContext)));
    }

    @Override
    public List<Map<String, Object>> parseScript(URL url) throws IOException {
        return parseScript(IOUtils.toString(url, StandardCharsets.UTF_8), StringUtils.substringAfterLast(url.getFile(), "."));
    }

    @Override
    public List<Map<String, Object>> parseScript(String content, String format) throws IOException {
        boolean yaml = format.equalsIgnoreCase("yml") || format.equalsIgnoreCase("yaml");
        content = stringSubstitutor.replace(content);
        ObjectMapper objectMapper = yaml ? yamlMapper : jsonMapper;
        return objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
    }
}
