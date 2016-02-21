/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.workflow;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
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
