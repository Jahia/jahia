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

    @Override
    public String toString() {
        return "WorkflowVariable{" +
                "value='" + value + '\'' +
                ", type=" + type +
                '}';
    }
}
