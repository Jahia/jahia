/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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
public class MissingTemplatesValidator implements ImportValidator {

    private static final Logger logger = LoggerFactory.getLogger(MissingTemplatesValidator.class);

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("/?sites/[^/]*/templates/(.*)");

    private Map<String, Boolean> checked = new HashMap<String, Boolean>();

    private Map<String, Set<String>> missing = new TreeMap<String, Set<String>>();
    private JahiaTemplateManagerService templateManagerService;

    public ValidationResult getResult() {
        return new MissingTemplatesValidationResult(missing);
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void validate(String decodedLocalName, String decodedQName, String currentPath,
            Attributes atts) {
        String templateAttr = atts.getValue("j:templateNode");
        if (StringUtils.isEmpty(templateAttr)) {
            // no template attribute
            return;
        }

        Matcher matcher = TEMPLATE_PATTERN.matcher(templateAttr);
        String templatePath = matcher.matches() ? matcher.group(1) : null;
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
                missing.get(templatePath).add(currentPath);
            }
        } else {
            // not yet checked -> do check it
            if (!templateManagerService.isTemplatePresent(templatePath)) {
                checked.put(templatePath, Boolean.FALSE);
                TreeSet<String> pathes = new TreeSet<String>();
                pathes.add(currentPath);
                missing.put(templatePath, pathes);
            } else {
                checked.put(templatePath, Boolean.TRUE);
            }
        }
    }
}
