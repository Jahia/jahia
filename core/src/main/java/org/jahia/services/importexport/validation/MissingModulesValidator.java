/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
                    .getTemplatePackageByNodeName(targetTemplateSet) != null;

            // validate modules
            if (modules != null) {
                for (String module : modules) {
                    if (templateManagerService.getTemplatePackageByNodeName(module) == null) {
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
