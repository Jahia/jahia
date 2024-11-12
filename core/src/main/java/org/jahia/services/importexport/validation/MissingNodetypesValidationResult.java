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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

/**
 * An instance of this class is being returned when validating JCR document view import files and contains information about expected import
 * failures.
 */
public class MissingNodetypesValidationResult implements ValidationResult, Serializable {

    private static final long serialVersionUID = -5817064601349300396L;

    private Map<String, Set<String>> missingMixins = new TreeMap<String, Set<String>>();

    private Map<String, Set<String>> missingNodetypes = new TreeMap<String, Set<String>>();

    /**
     * Initializes an instance of this class.
     *
     * @param missingNodetypes
     * @param missingMixins
     */
    public MissingNodetypesValidationResult(Map<String, Set<String>> missingNodetypes,
            Map<String, Set<String>> missingMixins) {
        super();
        this.missingNodetypes = missingNodetypes;
        this.missingMixins = missingMixins;
    }

    /**
     * Initializes an instance of this class, merging the two validation results into one.
     *
     * @param result1
     *            the first validation result instance to be merged
     * @param result2
     *            the second validation result instance to be merged
     */
    protected MissingNodetypesValidationResult(MissingNodetypesValidationResult result1,
            MissingNodetypesValidationResult result2) {
        super();
        missingNodetypes.putAll(result1.getMissingNodetypes());
        missingMixins.putAll(result1.getMissingMixins());
        for (Map.Entry<String, Set<String>> item : result2.getMissingNodetypes().entrySet()) {
            if (missingNodetypes.containsKey(item.getKey())) {
                missingNodetypes.get(item.getKey()).addAll(item.getValue());
            } else {
                missingNodetypes.put(item.getKey(), item.getValue());
            }
        }
        for (Map.Entry<String, Set<String>> item : result2.getMissingMixins().entrySet()) {
            if (missingMixins.containsKey(item.getKey())) {
                missingMixins.get(item.getKey()).addAll(item.getValue());
            } else {
                missingMixins.put(item.getKey(), item.getValue());
            }
        }
    }

    /**
     * @return a Map with missing mixin types as keys and as value a list of element paths having that mixin type in the import
     */
    public Map<String, Set<String>> getMissingMixins() {
        return missingMixins;
    }

    /**
     * @return a Map with missing nodetypes as keys and as value a list of element paths having it as primary type
     */
    public Map<String, Set<String>> getMissingNodetypes() {
        return missingNodetypes;
    }

    /**
     * Returns <code>true</code> if the current validation result is successful, meaning no missing nodetypes and mixins were detected.
     *
     * @return <code>true</code> if the current validation result is successful, meaning no missing nodetypes and mixins were detected
     */
    public boolean isSuccessful() {
        return missingNodetypes.isEmpty() && missingMixins.isEmpty();
    }

    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith == null || toBeMergedWith.isSuccessful()
                || !(toBeMergedWith instanceof MissingNodetypesValidationResult) ? this
                : new MissingNodetypesValidationResult(this,
                        (MissingNodetypesValidationResult) toBeMergedWith);
    }

    /**
     * @param missingMixins
     */
    public void setMissingMixins(Map<String, Set<String>> missingMixins) {
        this.missingMixins = missingMixins;
    }

    /**
     * @param missingNodetypes
     */
    public void setMissingNodetypes(Map<String, Set<String>> missingNodetypes) {
        this.missingNodetypes = missingNodetypes;
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
            out.append(", missingNodetypes=").append(missingNodetypes);
            out.append(", missingMixins=").append(missingMixins);
        }
        out.append("]");

        return out.toString();
    }
}
