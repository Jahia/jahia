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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;

/**
 * An instance of this class is being returned when validating JCR document view import files and contains information about expected import
 * failures.
 *
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class ValidationResults implements Serializable {

    private static final long serialVersionUID = -7969446907569423334L;

    private List<ValidationResult> results = new LinkedList<ValidationResult>();

    private Map<String, ValidationResult> resultsByClassName;

    protected ValidationResult getResultByClassName(String clazz) {
        return getResultsByClassName().get(clazz);
    }

    public List<ValidationResult> getResults() {
        return results;
    }

    public void addResult(ValidationResult result) {
        results.add(result);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, ValidationResult> getResultsByClassName() {
        if (resultsByClassName == null) {
            resultsByClassName = LazyMap.decorate(new HashMap<String, ValidationResult>(0),
                    new Transformer() {
                        public Object transform(Object clazz) {
                            for (ValidationResult result : results) {
                                if (clazz.equals(result.getClass().getName())) {
                                    return result;
                                }
                            }
                            return null;
                        }
                    });
        }

        return resultsByClassName;
    }

    /**
     * Returns <code>true</code> if the current validation result is successful.
     *
     * @return <code>true</code> if the current validation result is successful
     */
    public boolean isSuccessful() {
        for (ValidationResult result : results) {
            if (!result.isSuccessful()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the current validation blocks the import.
     *
     * @return <code>true</code> if the current validation result is blocking.
     */
    public boolean isBlocking() {
        for (ValidationResult result : results) {
            if (!result.isSuccessful() && result.isBlocking()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Merges the results with the provided and returns a new instance of the {@link ValidationResults} object having "merged" results.
     *
     * @param toBeMergedWith
     *            a {@link ValidationResults} to merge with
     * @return the results with the provided and returns a new instance of the {@link ValidationResults} object having "merged" results
     */
    public ValidationResults merge(ValidationResults toBeMergedWith) {
        ValidationResults merged = new ValidationResults();
        Set<String> typesMerged = new HashSet<String>();
        for (ValidationResult thisResult : results) {
            String clazz = thisResult.getClass().getName();
            typesMerged.add(clazz);
            ValidationResult other = toBeMergedWith.getResultByClassName(clazz);
            merged.addResult(other != null ? thisResult.merge(other) : thisResult);
        }

        for (ValidationResult otherResult : toBeMergedWith.getResults()) {
            if (!typesMerged.contains(otherResult.getClass().getName())) {
                merged.addResult(otherResult);
            }
        }

        return merged;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(128);
        final boolean successful = isSuccessful();
        out.append("[overall result=").append(successful ? "successful" : "failure");
        if (!successful) {
            out.append(", details=[");
            boolean first = true;
            for (ValidationResult result : results) {
                if (!first) {
                    out.append(", ");
                } else {
                    first = false;
                }
                out.append(result);
            }
            out.append("]");
        }
        out.append("]");

        return out.toString();
    }
}
