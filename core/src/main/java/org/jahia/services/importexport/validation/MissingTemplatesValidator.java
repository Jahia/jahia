/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.jahia.api.Constants;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

/**
 * Helper class for performing a validation for missing templates in the imported content.
 * 
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class MissingTemplatesValidator implements ImportValidator, ModuleDependencyAware {

    private static final Logger logger = LoggerFactory.getLogger(MissingTemplatesValidator.class);

    private static final Comparator<Map.Entry<String, Integer>> MISSING_COUNT_COMPARATOR = new Comparator<Map.Entry<String, Integer>>() {
        public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
            return entry1.getValue().compareTo(entry2.getValue());
        }
    };

    private static final Pattern TEMPLATE_PATTERN = Pattern
            .compile("(#?/sites/[^/]*|\\$currentSite)/templates/(.*)");

    private Set<String> availableTemplateSets;

    private Map<String, Boolean> checked = new HashMap<String, Boolean>();

    private Set<String> dependencies = Collections.emptySet();

    private Map<String, Integer> missingInAllTemplateSets = new HashMap<String, Integer>();

    private Map<String, Set<String>> missingTemplates = new TreeMap<String, Set<String>>();

    private Set<String> modules = Collections.emptySet();

    private String targetTemplateSet;

    private boolean targetTemplateSetPresent;

    private JahiaTemplateManagerService templateManagerService;

    private Set<String> getAvailableTemplateSets() {
        if (null == availableTemplateSets) {
            availableTemplateSets = templateManagerService.getTemplateSetNames();
        }

        return availableTemplateSets;
    }

    public ValidationResult getResult() {
        Map<String, Integer> modulesMissingCounts = Collections.emptyMap();
        if (!missingInAllTemplateSets.isEmpty()) {
            modulesMissingCounts = new LinkedHashMap<String, Integer>(
                    missingInAllTemplateSets.size());

            List<Map.Entry<String, Integer>> mapEntries = new LinkedList<Map.Entry<String, Integer>>(
                    missingInAllTemplateSets.entrySet());
            Collections.sort(mapEntries, MISSING_COUNT_COMPARATOR);
            for (Map.Entry<String, Integer> entry : mapEntries) {
                modulesMissingCounts.put(entry.getKey(), entry.getValue());
            }
        }
        return new MissingTemplatesValidationResult(missingTemplates, targetTemplateSet,
                targetTemplateSetPresent, modulesMissingCounts);
    }

    public void initDependencies(String templateSetName, List<String> modules) {
        targetTemplateSet = templateSetName;
        this.modules = new LinkedHashSet<String>(modules);
    }

    private boolean isTemplatePresent(String templateName) {
        if (targetTemplateSetPresent) {
            return templateManagerService.isTemplatePresent(templateName, dependencies);
        } else {
            // we do not have the target template set
            // will populate the information for available template sets
            for (String setName : getAvailableTemplateSets()) {
                Set<String> dependenciesToCheck = new LinkedHashSet<String>(modules.size() + 1);
                dependenciesToCheck.add(setName);
                dependenciesToCheck.addAll(modules);

                if (!missingInAllTemplateSets.containsKey(setName)) {
                    missingInAllTemplateSets.put(setName, Integer.valueOf(0));
                }
                boolean found = templateManagerService.isTemplatePresent(templateName,
                        dependenciesToCheck);
                if (!found) {
                    missingInAllTemplateSets.put(setName,
                            Integer.valueOf(1 + missingInAllTemplateSets.get(setName)));
                }
            }

            return true;
        }
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void validate(String decodedLocalName, String decodedQName, String currentPath,
            Attributes atts) {
        if (Constants.JAHIANT_VIRTUALSITE.equals(StringUtils.defaultString(atts
                .getValue(Constants.JCR_PRIMARYTYPE)))) {
            // we have the site element -> initialize dependencies
            dependencies = new LinkedHashSet<String>(modules.size() + 1);
            dependencies.add(targetTemplateSet);
            dependencies.addAll(modules);

            // validate if we have the template set deployed
            targetTemplateSetPresent = templateManagerService
                    .getTemplatePackageById(targetTemplateSet) != null;
        }
        if (dependencies.isEmpty()) {
            return;
        }

        String templateAttr = atts.getValue("j:templateName");
        if (StringUtils.isEmpty(templateAttr)) {
            templateAttr = StringUtils.substringAfterLast(atts.getValue("j:templateNode"), "/");
        }
        if (StringUtils.isEmpty(templateAttr)) {
            // no template attribute
            return;
        }
        String templateName = ISO9075.decode(templateAttr);

        if (checked.containsKey(templateName)) {
            // we have already checked that template
            if (!checked.get(templateName)) {
                // the template is missing -> add the path to the set
                missingTemplates.get(templateName).add(currentPath);
            }
        } else {
            // not yet checked -> do check it
            if (!isTemplatePresent(templateName)) {
                checked.put(templateName, Boolean.FALSE);
                TreeSet<String> pathes = new TreeSet<String>();
                pathes.add(currentPath);
                missingTemplates.put(templateName, pathes);
            } else {
                checked.put(templateName, Boolean.TRUE);
            }
        }
    }
}
