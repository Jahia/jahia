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
