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

package org.jahia.services.importexport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An instance of this class is being returned when validating JCR document view import files and contains information about expected import
 * failures.
 */
public class ValidationResult {

    private Map<String, List<String>> missingMixins = new HashMap<String, List<String>>();

    private Map<String, List<String>> missingNodetypes = new HashMap<String, List<String>>();

    public ValidationResult() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param missingNodetypes
     * @param missingMixins
     */
    public ValidationResult(Map<String, List<String>> missingNodetypes,
            Map<String, List<String>> missingMixins) {
        this();
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
    public ValidationResult(ValidationResult result1, ValidationResult result2) {
        this();
        missingNodetypes.putAll(result1.getMissingNodetypes());
        missingMixins.putAll(result1.getMissingMixins());
        for (Map.Entry<String, List<String>> item : result2.getMissingNodetypes().entrySet()) {
            if (missingNodetypes.containsKey(item.getKey())) {
                missingNodetypes.get(item.getKey()).addAll(item.getValue());
            } else {
                missingNodetypes.put(item.getKey(), item.getValue());
            }
        }
        for (Map.Entry<String, List<String>> item : result2.getMissingMixins().entrySet()) {
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
    public Map<String, List<String>> getMissingMixins() {
        return missingMixins;
    }

    /**
     * @return a Map with missing nodetypes as keys and as value a list of element paths having it as primary type
     */
    public Map<String, List<String>> getMissingNodetypes() {
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

    /**
     * @param missingMixins
     */
    public void setMissingMixins(Map<String, List<String>> missingMixins) {
        this.missingMixins = missingMixins;
    }

    /**
     * @param missingNodetypes
     */
    public void setMissingNodetypes(Map<String, List<String>> missingNodetypes) {
        this.missingNodetypes = missingNodetypes;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(128);
        out.append("[result=").append(isSuccessful() ? "successful" : "failure");
        if (!isSuccessful()) {
            out.append(", missingNodetypes=").append(missingNodetypes);
            out.append(", missingMixins=").append(missingMixins);
        }
        out.append("]");

        return out.toString();
    }
}
