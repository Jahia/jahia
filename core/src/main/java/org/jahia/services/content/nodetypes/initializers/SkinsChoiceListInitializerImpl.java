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
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Template;

import javax.jcr.PropertyType;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;

/**
 * Choice list initializer to provide a selection of available skins.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 17 nov. 2009
 */
public class SkinsChoiceListInitializerImpl implements ChoiceListInitializer {

    public List<ChoiceListValue> getChoiceListValues(ProcessingContext jParams, ExtendedPropertyDefinition epd,
                                                     ExtendedNodeType realNodeType, String param, List<ChoiceListValue> values) {
        if (jParams == null) {
            return new ArrayList<ChoiceListValue>();
        }
        ExtendedNodeType nt = (ExtendedNodeType) jParams.getAttribute("contextDefinition");
        if (nt == null) {
            return new ArrayList<ChoiceListValue>();
        }
        try {
            nt = NodeTypeRegistry.getInstance().getNodeType("nt:base");
        } catch (NoSuchNodeTypeException e) {
            return new ArrayList<ChoiceListValue>();
        }
        SortedSet<Template> templates = RenderService.getInstance().getTemplatesSet(nt);

        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();
        for (Template template : templates) {
            if (template.getKey().startsWith("skins.")) {
                vs.add(new ChoiceListValue(template.getKey(),new HashMap<String, Object>(), new ValueImpl(template.getKey(), PropertyType.STRING, false)));
            }
        }
        return vs;
    }
}
