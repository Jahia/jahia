/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.formbuilder.initializers;

import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.jahia.services.templates.JahiaTemplateManagerService;

import javax.jcr.PropertyType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 9 mars 2010
 */
public class ChoiceListFormBuilderActionInitializers implements ModuleChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(ChoiceListFormBuilderActionInitializers.class);
    private String key;
    private Map<String, String> actionsMap;
    private JahiaTemplateManagerService templateService;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Map<String, String> getActionsMap() {
        return actionsMap;
    }

    public void setActionsMap(Map<String, String> actionsMap) {
        this.actionsMap = actionsMap;
    }

    public void setTemplateService(JahiaTemplateManagerService templateService) {
        this.templateService = templateService;
    }

    public List<ChoiceListValue> getChoiceListValues(ProcessingContext context, ExtendedPropertyDefinition epd,
                                                     ExtendedNodeType realNodeType, String param,
                                                     List<ChoiceListValue> values) {
        Map<String, Action> map = templateService.getActions();
        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();
        for (Map.Entry<String, String> action : actionsMap.entrySet()) {
            if (map.containsKey(action.getValue()) || action.getKey().equals("default")) {
                vs.add(new ChoiceListValue(action.getKey(), new HashMap<String, Object>(), new ValueImpl(
                        action.getValue(), PropertyType.STRING, false)));
            }
        }
        return vs;
    }
}
