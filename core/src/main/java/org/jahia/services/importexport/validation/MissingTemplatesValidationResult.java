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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a validation result, containing missing templates in the content to be imported.
 * 
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class MissingTemplatesValidationResult implements ValidationResult {

    private Map<String, Set<String>> missing = new TreeMap<String, Set<String>>();

    /**
     * Initializes an instance of this class.
     * 
     * @param missing
     *            missing templates information
     */
    public MissingTemplatesValidationResult(Map<String, Set<String>> missing) {
        super();
        this.missing = missing;
    }

    /**
     * Initializes an instance of this class, merging the two validation results into one.
     * 
     * @param result1
     *            the first validation result instance to be merged
     * @param result2
     *            the second validation result instance to be merged
     */
    protected MissingTemplatesValidationResult(MissingTemplatesValidationResult result1,
            MissingTemplatesValidationResult result2) {
        super();
        missing.putAll(result1.getMissingTemplates());
        for (Map.Entry<String, Set<String>> item : result2.getMissingTemplates().entrySet()) {
            if (missing.containsKey(item.getKey())) {
                missing.get(item.getKey()).addAll(item.getValue());
            } else {
                missing.put(item.getKey(), item.getValue());
            }
        }
    }

    /**
     * Returns a Map with missing templates as keys and as value a list of element paths having that template in the import.
     * 
     * @return a Map with missing templates as keys and as value a list of element paths having that template in the import
     */
    public Map<String, Set<String>> getMissingTemplates() {
        return missing;
    }

    /**
     * Returns <code>true</code> if the current validation result is successful, meaning no missing templates were detected.
     * 
     * @return <code>true</code> if the current validation result is successful, meaning no missing templates were detected
     */
    public boolean isSuccessful() {
        return missing.isEmpty();
    }

    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith == null || toBeMergedWith.isSuccessful()
                || !(toBeMergedWith instanceof MissingTemplatesValidationResult) ? this
                : new MissingTemplatesValidationResult(this,
                        (MissingTemplatesValidationResult) toBeMergedWith);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(128);
        out.append("[").append(StringUtils.substringAfterLast(getClass().getName(), "."))
                .append("=").append(isSuccessful() ? "successful" : "failure");
        if (!isSuccessful()) {
            out.append(", missingTemplates=").append(missing);
        }
        out.append("]");

        return out.toString();
    }
}
