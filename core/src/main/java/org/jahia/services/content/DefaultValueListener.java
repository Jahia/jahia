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

package org.jahia.services.content;

import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.*;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.*;

/**
 * User: toto
 * Date: Apr 30, 2008
 * Time: 11:56:02 AM
 */
public class DefaultValueListener extends DefaultEventListener {
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DefaultValueListener.class);

    public DefaultValueListener() {
    }


    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED;
    }

    public void onEvent(final EventIterator eventIterator) {
        try {
            // todo : may need to move the dynamic default values generation to JahiaNodeTypeInstanceHandler
            String userId = ((JCREventIterator)eventIterator).getSession().getUserID();
            if (userId.startsWith(JahiaLoginModule.SYSTEM)) {
                userId = userId.substring(JahiaLoginModule.SYSTEM.length());
            }
            JCRTemplate.getInstance().doExecuteWithSystemSession(userId, workspace, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                    final Set<Session> sessions = new HashSet<Session>();
                    while (eventIterator.hasNext()) {
                        Event event = eventIterator.nextEvent();
                        if (isExternal(event)) {
                            continue;
                        }
                        try {
                            JCRNodeWrapper n = null;
                            String eventPath = event.getPath();
                            if (event.getType() == Event.NODE_ADDED) {
                                try {
                                    n = (JCRNodeWrapper) s.getItem(eventPath);
                                } catch (PathNotFoundException e) {
                                    continue;
                                }
                            }
                            if (eventPath.endsWith(Constants.JCR_MIXINTYPES)) {
                                String path = eventPath.substring(0, eventPath.lastIndexOf('/'));
                                n = (JCRNodeWrapper) s.getItem(path.length() == 0 ? "/" : path);
                            }
                            if (n != null) {
                                sessions.add(n.getRealNode().getSession());
                                List<NodeType> l = new ArrayList<NodeType>();
                                NodeType nt = n.getPrimaryNodeType();
                                l.add(nt);
                                NodeType mixin[] = n.getMixinNodeTypes();
                                l.addAll(Arrays.asList(mixin));
                                for (Iterator<NodeType> iterator = l.iterator(); iterator.hasNext();) {
                                    NodeType nodeType = iterator.next();
                                    ExtendedNodeType ent = NodeTypeRegistry.getInstance().getNodeType(nodeType.getName());
                                    if (ent != null) {
                                        ExtendedPropertyDefinition[] pds = ent.getPropertyDefinitions();
                                        for (int i = 0; i < pds.length; i++) {
                                            ExtendedPropertyDefinition pd = pds[i];
                                            Value[] v = pd.getDefaultValues();
                                            for (int j = 0; j < v.length; j++) {
                                                Value value = v[j];
                                                if (value instanceof DynamicValueImpl) {
                                                    if (!n.hasProperty(pd.getName())) {
                                                        n.setProperty(pd.getName(), value.getString());
                                                    }
                                                }
                                            }
                                        }
                                        ExtendedNodeDefinition[] nodes = ent.getChildNodeDefinitions();
                                        for (ExtendedNodeDefinition definition : nodes) {
                                            if (definition.isAutoCreated() && !n.hasNode(definition.getName())) {
                                                n.addNode(definition.getName(), definition.getDefaultPrimaryTypeName());
                                            }
                                        }
                                    }
                                }
                                n.getRealNode().getSession().save();
                            }
                        } catch (NoSuchNodeTypeException e) {
                        } catch (Exception e) {
                            logger.error("Error when executing event", e);
                        }
                    }
                    for (Session jcrsession : sessions) {
                        jcrsession.save();
                    }
                    return null;
                }

            });

        } catch (NoSuchNodeTypeException e) {
            // silent ignore
        } catch (Exception e) {
            logger.error("Error when executing event", e);
        }

    }
}
