/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.DynamicValueImpl;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.initializers.I15dValueInitializer;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jahia.api.Constants.JCR_LASTMODIFIED;
import static org.jahia.api.Constants.JCR_LASTMODIFIEDBY;

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
            final JahiaUser user = eventSession.getUser();
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, workspace, null, new JCRCallback<Object>() {
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
                            try {
                                if (event.getType() == Event.NODE_ADDED) {
                                    n = (JCRNodeWrapper) s.getItem(eventPath);
                                }
                                if (eventPath.endsWith(Constants.JCR_MIXINTYPES)) {
                                    String path = eventPath.substring(0, eventPath.lastIndexOf('/'));
                                    n = (JCRNodeWrapper) s.getItem(path.length() == 0 ? "/" : path);
                                }
                            } catch (PathNotFoundException e) {
                                continue;
                            }
                            if (n != null) {
                                boolean anythingChanged = false;
                                List<NodeType> l = new ArrayList<NodeType>();
                                NodeType nt = n.getPrimaryNodeType();
                                l.add(nt);
                                NodeType mixin[] = n.getMixinNodeTypes();
                                l.addAll(Arrays.asList(mixin));
                                for (Iterator<NodeType> iterator = l.iterator(); iterator.hasNext(); ) {
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
                                                Node autoCreated = n.addNode(definition.getName(), definition.getDefaultPrimaryTypeName());
                                                if (autoCreated.isNodeType("jmix:originWS")) {
                                                    autoCreated.setProperty("j:originWS", workspace);
                                                }
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
        boolean doCreateI18n = sessionLocale != null && pd.isInternationalized();
        if (!pd.isAutoCreated() || (!doCreateI18n && !pd.hasDynamicDefaultValues())) {
            // no handling needed
            return false;
        }

        Node targetNode = doCreateI18n ? n.getOrCreateI18N(sessionLocale) : n;

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
