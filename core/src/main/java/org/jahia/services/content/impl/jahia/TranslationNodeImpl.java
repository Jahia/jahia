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
package org.jahia.services.content.impl.jahia;

import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentField;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.content.ContentObject;

import javax.jcr.*;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Locale;

/**
 * TODO Comment me
 *
 * @author toto
 */
public class TranslationNodeImpl extends NodeImpl {
    private JahiaContentNodeImpl parent;

    protected ContentObject object;
    protected Locale locale;
    protected List<PropertyImpl> parentI18nProperties;

    public TranslationNodeImpl(SessionImpl session, JahiaContentNodeImpl parent, List<PropertyImpl> parentI18nProperties, Locale locale) throws RepositoryException {
        super(session);
        setDefinition(NodeTypeRegistry.getInstance().getNodeType("jmix:i18n").getChildNodeDefinitionsAsMap().get("j:translation"));
        setNodetype(NodeTypeRegistry.getInstance().getNodeType("jnt:translation"));
        this.parent = parent;
        this.object = parent.getContentObject();
        this.locale = locale;
        this.parentI18nProperties = parentI18nProperties;
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return parent;
    }


    @Override
    protected void initProperties() throws RepositoryException {
        super.initProperties();

        initProperty(new PropertyImpl(getSession(), this,
                nodetype.getPropertyDefinitionsAsMap().get("jcr:language"),null,
                new ValueImpl(locale.toString(),PropertyType.STRING)));

        parent.initProperties();


        for (PropertyImpl i18nProperty : parentI18nProperties) {
            if (locale.equals(i18nProperty.getLocale())) {
                System.out.println("-------"+i18nProperty);
                properties.put(i18nProperty.getName(), i18nProperty);
            }
        }

    }
}
