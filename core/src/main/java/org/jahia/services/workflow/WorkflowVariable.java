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
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
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
package org.jahia.services.workflow;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.Serializable;
import java.util.*;

/**
 * Represents single workflow variable.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 29 avr. 2010
 */
public class WorkflowVariable implements Serializable {

    private static final long serialVersionUID = 942602985046632239l;

    private String value;
    private int type;

    public WorkflowVariable() {
    }

    public WorkflowVariable(String value, int type) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getValueAsDate() {
        if (null == value || "".equals(value.trim())) {
            return null;
        }
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.valueOf(value));
            return calendar.getTime();
        } catch (NumberFormatException e) {
            DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(value);
            return dateTime.toDate();
        }
    }

    /**
     * Generate a map of task parameters with values of type WorkflowVariable or List<WorkflowVariable>
     *
     * @param parameters
     * @param formNodeType
     * @param excludedParameters
     * @return a map of task parameters with values of type WorkflowVariable or List<WorkflowVariable>
     */
    public static HashMap<String, Object> getVariablesMap(Map<String, List<String>> parameters, String formNodeType,
                                                          List<String> excludedParameters)  {
        HashMap<String, Object> map = new HashMap<String, Object>();
        ExtendedNodeType type = null;
        if (formNodeType != null && NodeTypeRegistry.getInstance().hasNodeType(formNodeType)) {
            try {
                type = NodeTypeRegistry.getInstance().getNodeType(formNodeType);
            } catch (NoSuchNodeTypeException e) {
                // shouldn't happen as we tested the existence before
            }
        }
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            if (excludedParameters != null && excludedParameters.contains(paramName)) {
                continue;
            }
            List<String> paramValue = parameters.get(paramName);
            if (paramValue == null || paramValue.isEmpty()) {
                continue;
            }
            ExtendedPropertyDefinition definition = null;
            if (type != null) {
                definition = type.getPropertyDefinition(paramName);
            }
            if (definition != null) {
                if (definition.isMultiple()) {
                    List<WorkflowVariable> list = new ArrayList<WorkflowVariable>();
                    for (String s : paramValue) {
                        if (StringUtils.isNotBlank(s)) {
                            list.add(new WorkflowVariable(s, definition.getRequiredType()));
                        }
                    }
                    map.put(paramName, list);
                } else {
                    String s = paramValue.get(0);
                    if (StringUtils.isNotBlank(s)) {
                        map.put(paramName, new WorkflowVariable(s, definition.getRequiredType()));
                    }
                }
            } else {
                String s = paramValue.get(0);
                if (StringUtils.isNotBlank(s)) {
                    map.put(paramName, paramValue.get(0));
                }
            }
        }
        return map;
    }
}
