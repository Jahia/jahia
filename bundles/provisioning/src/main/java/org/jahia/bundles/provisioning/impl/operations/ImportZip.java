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

import org.jahia.services.content.JCRTemplate;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.jahia.services.importexport.DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE;

/**
 * Import site operation
 */
@Component(service = Operation.class, property = "type=importZip")
public class ImportZip implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(ImportZip.class);
    public static final String IMPORT = "import";
    public static final String ROOT_PATH = "rootPath";

    private JCRTemplate jcrTemplate;
    private ImportExportService importExportService;

    @Reference
    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    @Reference
    public void setImportExportService(ImportExportService importExportService) {
        this.importExportService = importExportService;
    }

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.containsKey(IMPORT);
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {

        try {
            jcrTemplate.doExecuteWithSystemSession(session -> {
                List<Map<String, Object>> entries = ProvisioningScriptUtil.convertToList(entry, IMPORT, "url");
                for (Map<String, Object> subEntry : entries) {
                    String url = (String) subEntry.get(IMPORT);
                    String rootPath = (String) subEntry.get(ROOT_PATH);
                    try {
                        importExportService.importZip(rootPath != null ? rootPath : "/", ProvisioningScriptUtil.getResource(url, executionContext), ROOT_BEHAVIOUR_IGNORE, session);
                    } catch (IOException e) {
                        // hide full details of the IOException that may contain sensitive information
                        throw new RepositoryException(String.format("An IOException occurred, unable to import site from %s (rootPath: %s)", url, rootPath));
                    }
                }
                return null;
            });
        } catch (RepositoryException e) {
            logger.error("Cannot import site", e);
        }
    }

}
