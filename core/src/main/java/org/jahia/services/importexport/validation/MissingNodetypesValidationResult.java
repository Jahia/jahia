/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
