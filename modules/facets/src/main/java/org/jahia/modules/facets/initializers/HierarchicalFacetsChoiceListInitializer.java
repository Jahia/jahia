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

package org.jahia.modules.facets.initializers;

import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.slf4j.Logger;

import javax.jcr.PropertyType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.*;

/**
 * User: qlamerand
 * Date: Feb 2, 2012
 * Time: 14:23
 */
public class HierarchicalFacetsChoiceListInitializer implements ModuleChoiceListInitializer {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(HierarchicalFacetsChoiceListInitializer.class);

    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values,
                                                     Locale locale, Map<String, Object> context) {
        final Set<ChoiceListValue> propertyNames = new HashSet<ChoiceListValue>();
        try {
            NodeTypeIterator ntr = NodeTypeRegistry.getInstance().getAllNodeTypes();
            while (ntr.hasNext()) {
                ExtendedNodeType nt =(ExtendedNodeType) ntr.nextNodeType();
                for (PropertyDefinition def : nt.getPropertyDefinitions()) {
                    ExtendedPropertyDefinition ep = (ExtendedPropertyDefinition) def;
                    if (ep.isFacetable() && ep.isHierarchical()) {
                        String displayName = ep.getLabel(locale,nt);
                        displayName += nt.isMixin()?"":" (" + nt.getLabel(locale) + ")";
                        String value = ep.getDeclaringNodeType().getName() + ";" + ep.getName();
                        propertyNames.add(new ChoiceListValue(displayName, new HashMap<String, Object>(),
                                new ValueImpl(value, PropertyType.STRING , false)));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        List<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>(propertyNames);
        Collections.sort(listValues);
        return listValues;
    }
}
