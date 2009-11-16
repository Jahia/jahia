/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Template;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.analytics.reports.ReportParams;

import javax.jcr.Value;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 10, 2008
 * Time: 11:09:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class Templates implements ValueInitializer {

    public Value[] getValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params) {
        if (jParams == null) {
            return new Value[0];
        }
        
        JCRNodeWrapper node = (JCRNodeWrapper) jParams.getAttribute("contextNode");

        if (node == null) {
            return new Value[0];
        }

        SortedSet<Template> templates;
        if ("j:template".equals(declaringPropertyDefinition.getName())) {
            templates = new TreeSet<Template>();
            try {
                for (String s : node.getNodeTypes()) {
                    templates.addAll(RenderService.getInstance().getTemplatesSet(NodeTypeRegistry.getInstance().getNodeType(s)));
                }
            } catch (RepositoryException e) {

            }
        } else if ("j:referenceTemplate".equals(declaringPropertyDefinition.getName())) {
            templates = new TreeSet<Template>();
            try {
                if (node.hasProperty("j:node")) {
                    JCRNodeWrapper ref = (JCRNodeWrapper) node.getProperty("j:node").getNode();

                    for (String s : ref.getNodeTypes()) {
                        templates.addAll(RenderService.getInstance().getTemplatesSet(NodeTypeRegistry.getInstance().getNodeType(s)));
                    }
                }
            } catch (RepositoryException e) {

            }
        } else {
            templates = RenderService.getInstance().getAllTemplatesSet();
        }

        List<Value> vs = new ArrayList<Value>();
        for (Template template : templates) {
            if (!template.getKey().startsWith("wrapper.") && !template.getKey().startsWith("skins.") && !template.getKey().startsWith("debug.")) {
                vs.add(new ValueImpl(template.getKey(), PropertyType.STRING, false));
            }
        }
        return vs.toArray(new Value[vs.size()]);
    }
}