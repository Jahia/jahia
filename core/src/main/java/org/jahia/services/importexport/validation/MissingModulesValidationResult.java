/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a validation result, containing missing modules in the content to be imported.
 *
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class MissingModulesValidationResult implements ValidationResult, Serializable {

    private static final long serialVersionUID = 5602829144652574806L;

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
    public boolean isBlocking() {
        return true;
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
