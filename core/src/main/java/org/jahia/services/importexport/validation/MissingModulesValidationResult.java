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

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a validation result, containing missing modules in the content to be imported.
 * 
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class MissingModulesValidationResult implements ValidationResult {

    private Set<String> missing = new TreeSet<String>();

    private String targetTemplateSet;

    private boolean targetTemplateSetPresent;

    /**
     * Initializes an instance of this class, merging the two validation results into one.
     * 
     * @param result1
     *            the first validation result instance to be merged
     * @param result2
     *            the second validation result instance to be merged
     */
    protected MissingModulesValidationResult(MissingModulesValidationResult result1,
            MissingModulesValidationResult result2) {
        super();
        targetTemplateSet = result1.getTargetTemplateSet();
        targetTemplateSetPresent = result1.isTargetTemplateSetPresent();
        if (targetTemplateSetPresent && !result2.isTargetTemplateSetPresent()) {
            targetTemplateSet = result2.getTargetTemplateSet();
            targetTemplateSetPresent = false;
        }
        missing.addAll(result1.getMissingModules());
        missing.addAll(result2.getMissingModules());
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param missingModules
     *            missing modules information
     * @param targetTemplateSet
     *            the template set from the import file
     * @param targetTemplateSetPresent
     *            is template set from import file present on the system?
     * @param templateSetsMissingCounts
     *            if the target template set is not present on the system we verify templates against all available template sets and check
     *            how many are missing in each of them
     */
    public MissingModulesValidationResult(Set<String> missingModules, String targetTemplateSet,
            boolean targetTemplateSetPresent) {
        super();
        if (missing != null) {
            this.missing.addAll(missingModules);
        }
        this.targetTemplateSet = targetTemplateSet;
        this.targetTemplateSetPresent = targetTemplateSetPresent;
    }

    /**
     * Returns a set with missing modules.
     * 
     * @return a set with missing modules
     */
    public Set<String> getMissingModules() {
        return missing;
    }

    /**
     * Returns the name of the template set, specified in the imprt file.
     * 
     * @return the name of the template set, specified in the imprt file
     */
    public String getTargetTemplateSet() {
        return targetTemplateSet;
    }

    /**
     * Returns <code>true</code> if the current validation result is successful, meaning no missing templates were detected.
     * 
     * @return <code>true</code> if the current validation result is successful, meaning no missing templates were detected
     */
    public boolean isSuccessful() {
        return isTargetTemplateSetPresent() && missing.isEmpty();
    }

    public boolean isTargetTemplateSetPresent() {
        return targetTemplateSet == null || targetTemplateSetPresent;
    }

    /**
     * Performs a merge of current validation results with provided one.
     * 
     * @return returns a merged view of both results
     */
    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith == null || toBeMergedWith.isSuccessful()
                || !(toBeMergedWith instanceof MissingModulesValidationResult) ? this
                : new MissingModulesValidationResult(this,
                        (MissingModulesValidationResult) toBeMergedWith);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(128);
        out.append("[").append(StringUtils.substringAfterLast(getClass().getName(), "."))
                .append("=").append(isSuccessful() ? "successful" : "failure");
        if (!isSuccessful()) {
            out.append(", targetTemplateSet=").append(targetTemplateSet);
            out.append(", targetTemplateSetPresent=").append(targetTemplateSetPresent);
            out.append(", missingModules=").append(missing);
        }
        out.append("]");

        return out.toString();
    }
}
