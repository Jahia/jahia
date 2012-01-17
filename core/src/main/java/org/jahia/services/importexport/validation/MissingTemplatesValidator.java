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
import java.util.regex.Matcher;
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
        Map<String, Integer> templateSetsMissingCounts = Collections.emptyMap();
        if (!missingInAllTemplateSets.isEmpty()) {
            templateSetsMissingCounts = new LinkedHashMap<String, Integer>(
                    missingInAllTemplateSets.size());

            List<Map.Entry<String, Integer>> mapEntries = new LinkedList<Map.Entry<String, Integer>>(
                    missingInAllTemplateSets.entrySet());
            Collections.sort(mapEntries, MISSING_COUNT_COMPARATOR);
            for (Map.Entry<String, Integer> entry : mapEntries) {
                templateSetsMissingCounts.put(entry.getKey(), entry.getValue());
            }
        }
        return new MissingTemplatesValidationResult(missingTemplates, targetTemplateSet,
                targetTemplateSetPresent, templateSetsMissingCounts);
    }

    public void initDependencies(String templateSetName, List<String> modules) {
        targetTemplateSet = templateSetName;
        this.modules = new LinkedHashSet<String>(modules);
    }

    private boolean isTemplatePresent(String templatePath) {
        if (targetTemplateSetPresent) {
            return templateManagerService.isTemplatePresent(templatePath, dependencies);
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
                boolean found = templateManagerService.isTemplatePresent(templatePath,
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
                    .getTemplatePackageByNodeName(targetTemplateSet) != null;
        }
        if (dependencies.isEmpty()) {
            return;
        }

        String templateAttr = atts.getValue("j:templateNode");
        if (StringUtils.isEmpty(templateAttr)) {
            // no template attribute
            return;
        }
        templateAttr = ISO9075.decode(templateAttr);
        Matcher matcher = TEMPLATE_PATTERN.matcher(templateAttr);

        String templatePath = matcher.matches() ? matcher.group(2) : null;
        if (StringUtils.isEmpty(templatePath)) {
            logger.warn("j:templateNode value '{}' does not seem well-formed. Skipping.",
                    templateAttr);
            return;
        }

        templatePath = "/" + templatePath;

        if (checked.containsKey(templatePath)) {
            // we have already checked that template
            if (!checked.get(templatePath)) {
                // the template is missing -> add the path to the set
                missingTemplates.get(templatePath).add(currentPath);
            }
        } else {
            // not yet checked -> do check it
            if (!isTemplatePresent(templatePath)) {
                checked.put(templatePath, Boolean.FALSE);
                TreeSet<String> pathes = new TreeSet<String>();
                pathes.add(currentPath);
                missingTemplates.put(templatePath, pathes);
            } else {
                checked.put(templatePath, Boolean.TRUE);
            }
        }
    }
}
