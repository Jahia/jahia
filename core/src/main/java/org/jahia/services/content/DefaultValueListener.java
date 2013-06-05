/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.DynamicValueImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.initializers.I15dValueInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCR listener that automatically populates node properties with default values (in case of dynamic values or i18n properties) and creates
 * mandatory sub-nodes.
 * 
 * @author Thomas Draier
 */
public class DefaultValueListener extends DefaultEventListener {
    private static Logger logger = LoggerFactory.getLogger(DefaultValueListener.class);

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED;
    }

    public void onEvent(final EventIterator eventIterator) {
        try {
            // todo : may need to move the dynamic default values generation to JahiaNodeTypeInstanceHandler
            JCRSessionWrapper eventSession = ((JCREventIterator)eventIterator).getSession();
            final Locale sessionLocale = eventSession.getLocale();
            String userId = eventSession.getUserID();
            if (userId.startsWith(JahiaLoginModule.SYSTEM)) {
                userId = userId.substring(JahiaLoginModule.SYSTEM.length());
            }
            JCRTemplate.getInstance().doExecuteWithSystemSession(userId, workspace, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                    Set<Session> sessions = null;
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
                                boolean anythingChanged = false;
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
                                            Value[] defValues = pd.getDefaultValuesAsUnexpandedValue();
                                            if (defValues.length > 0) {
                                                boolean handled = handlePropertyDefaultValues(n, pd, defValues, sessionLocale);
                                                anythingChanged = anythingChanged || handled;
                                            }
                                        }
                                        ExtendedNodeDefinition[] nodes = ent.getChildNodeDefinitions();
                                        for (ExtendedNodeDefinition definition : nodes) {
                                            if (definition.isAutoCreated() && !n.hasNode(definition.getName())) {
                                                n.addNode(definition.getName(), definition.getDefaultPrimaryTypeName());
                                                anythingChanged = true;
                                            }
                                        }
                                    }
                                }
                                if (anythingChanged) {
                                    n.getRealNode().getSession().save();
                                    if (sessions == null) {
                                        sessions = new HashSet<Session>();
                                    }
                                    sessions.add(n.getRealNode().getSession());
                                }
                            }
                        } catch (NoSuchNodeTypeException e) {
                            // ignore
                        } catch (Exception e) {
                            logger.error("Error when executing event", e);
                        }
                    }
                    if (sessions != null && !sessions.isEmpty()) {
                        for (Session jcrsession : sessions) {
                            jcrsession.save();
                        }
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

    protected boolean handlePropertyDefaultValues(JCRNodeWrapper n, ExtendedPropertyDefinition pd, Value[] values,
            Locale sessionLocale) throws RepositoryException {
        boolean doCreateI10n = sessionLocale != null && pd.isInternationalized();
        if (!pd.isAutoCreated() || (!doCreateI10n && !pd.hasDynamicDefaultValues())) {
            // no handling needed
            return false;
        }

        Node targetNode = doCreateI10n ? n.getOrCreateI18N(sessionLocale) : n;

        String propertyName = pd.getName();

        if (targetNode.hasProperty(propertyName)
                && !I15dValueInitializer.DEFAULT_VALUE.equals(targetNode.getProperty(propertyName).getString())) {
            // node already has the property -> return
            return false;
        }

        boolean valuesSet = false;

        Value[] expandedValues = expandValues(values, sessionLocale);
        if (expandedValues.length > 0) {
            if (pd.isMultiple()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting default values for property [{}].[{}]: {}", new String[] {
                            pd.getDeclaringNodeType().getName(), propertyName, asString(expandedValues) });
                }
                targetNode.setProperty(propertyName, expandedValues);
            } else {
                if (expandedValues.length == 1) {
                    targetNode.setProperty(propertyName, expandedValues[0]);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting default value for property [{}].[{}]: {}", new String[] {
                                pd.getDeclaringNodeType().getName(), propertyName, expandedValues[0].getString() });
                    }
                } else {
                    throw new ValueFormatException("Property [" + pd.getDeclaringNodeType().getName() + "].["
                            + propertyName + "] cannot accept multiple values");
                }
            }
            valuesSet = true;
        }

        return valuesSet;
    }

    private String asString(Value[] expandedValues) throws ValueFormatException, IllegalStateException,
            RepositoryException {
        List<String> values = new LinkedList<String>();
        for (Value v : expandedValues) {
            values.add(v.getString());
        }

        return StringUtils.join(values, ", ");
    }

    private Value[] expandValues(Value[] values, Locale sessionLocale) {
        List<Value> expanded = new LinkedList<Value>();
        for (Value v : values) {
            if (v instanceof DynamicValueImpl) {
                Value[] expandedValues = ((DynamicValueImpl) v).expand(sessionLocale);
                if (expandedValues != null && expandedValues.length > 0) {
                    for (Value ev : expandedValues) {
                        expanded.add(ev);
                    }
                }
            } else {
                expanded.add(v);
            }
        }

        return expanded.toArray(new Value[] {});
    }
}
