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
package org.jahia.services.importexport.validation;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.xml.sax.Attributes;

/**
 * Helper class for performing a validation for missing templates in the imported content.
 *
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class MissingModulesValidator implements ImportValidator, ModuleDependencyAware {

    private boolean done;

    private Set<String> missingModules = new TreeSet<String>();

    private List<String> modules = Collections.emptyList();

    private String targetTemplateSet;

    private boolean targetTemplateSetPresent = true;

    private JahiaTemplateManagerService templateManagerService;

    @Override
    public ValidationResult getResult() {
        return new MissingModulesValidationResult(missingModules, targetTemplateSet,
                targetTemplateSetPresent);
    }

    @Override
    public void initDependencies(String templateSetName, List<String> modules) {
        this.targetTemplateSet = templateSetName;
        this.modules = modules;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    @Override
    public void validate(String decodedLocalName, String decodedQName, String currentPath,
            Attributes atts) {

        if (done) {
            return;
        }

        if (Constants.JAHIANT_VIRTUALSITE.equals(StringUtils.defaultString(atts
                .getValue(Constants.JCR_PRIMARYTYPE)))) {
            // we got the site element -> check the dependencies

            // validate if we have the template set deployed
            targetTemplateSetPresent = templateManagerService
                    .getTemplatePackageById(targetTemplateSet) != null;

            // validate modules
            if (modules != null) {
                for (String module : modules) {
                    if (templateManagerService.getTemplatePackageById(module) == null) {
                        missingModules.add(module);
                    }
                }
            }

            done = true;
        } else {
            targetTemplateSetPresent = true;
            missingModules.clear();
        }
    }
}
