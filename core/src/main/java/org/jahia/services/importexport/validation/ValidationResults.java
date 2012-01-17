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
public class ValidationResults {

    private List<ValidationResult> results = new LinkedList<ValidationResult>();

    private Map<String, ValidationResult> resultsByClassName;

    protected ValidationResult getResultByClassName(String clazz) {
        return getResultsByClassName().get(clazz);
    }

    public List<ValidationResult> getResults() {
        return results;
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
            merged.getResults().add(other != null ? thisResult.merge(other) : thisResult);
        }

        for (ValidationResult otherResult : toBeMergedWith.getResults()) {
            if (!typesMerged.contains(otherResult.getClass().getName())) {
                merged.getResults().add(otherResult);
            }
        }

        return merged;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(128);
        out.append("[overall result=").append(isSuccessful() ? "successful" : "failure");
        if (!isSuccessful()) {
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
