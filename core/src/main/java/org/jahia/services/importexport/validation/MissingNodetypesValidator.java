/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.xml.sax.Attributes;

/**
 * Helper class for performing a validation for missing node types and mixins in the imported content.
 *
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class MissingNodetypesValidator implements ImportValidator {

    private Set<String> existingNodetypes = new HashSet<String>();
    private Map<String, Set<String>> missingMixins = new TreeMap<String, Set<String>>();
    private Map<String, Set<String>> missingNodetypes = new TreeMap<String, Set<String>>();

    private boolean isTypeExisting(String type, boolean mixin) {
        if (existingNodetypes.contains(type)) {
            return true;
        } else if (!mixin && missingNodetypes.containsKey(type) || mixin
                && missingMixins.containsKey(type)) {
            return false;
        } else {
            try {
                NodeTypeRegistry.getInstance().getNodeType(type);
                existingNodetypes.add(type);
                return true;
            } catch (NoSuchNodeTypeException e) {
                if (!mixin) {
                    missingNodetypes.put(type, new TreeSet<String>());
                } else {
                    missingMixins.put(type, new TreeSet<String>());
                }
                return false;
            }
        }
    }

    public ValidationResult getResult() {
        return new MissingNodetypesValidationResult(missingNodetypes, missingMixins);
    }

    public void validate(String decodedLocalName, String decodedQName, String currentPath,
                         Attributes atts) {
        String pt = atts.getValue(Constants.JCR_PRIMARYTYPE);
        if (pt != null && !isTypeExisting(pt, false)) {
            missingNodetypes.get(pt).add(currentPath);
        }
        String m = atts.getValue(Constants.JCR_MIXINTYPES);
        if (m != null) {
            StringTokenizer st = new StringTokenizer(m, " ,");
            while (st.hasMoreTokens()) {

                String mixin = st.nextToken();
                if (!isTypeExisting(mixin, true)) {
                    missingMixins.get(mixin).add(currentPath);
                }
            }
        }
    }
}
