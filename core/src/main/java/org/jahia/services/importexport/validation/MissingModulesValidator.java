/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.importexport.validation;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.xml.sax.Attributes;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

    private boolean targetTemplateSetPresent;

    private JahiaTemplateManagerService templateManagerService;

    public ValidationResult getResult() {
        return new MissingModulesValidationResult(missingModules, targetTemplateSet,
                targetTemplateSetPresent);
    }

    public void initDependencies(String templateSetName, List<String> modules) {
        this.targetTemplateSet = templateSetName;
        this.modules = modules;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

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
