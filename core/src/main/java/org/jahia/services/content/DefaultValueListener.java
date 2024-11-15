/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.touk.throwing.ThrowingFunction;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JCR listener that automatically populates node properties with default values (in case of i18n properties or override properties) and creates
 * mandatory sub-nodes.
 *
 * BE CAREFUL: JahiaNodeTypeInstanceHandler is already handling the default values, but not for i18n and overides, so current listener is here to do the rest.
 * Basically only autocreated i18n(default, dynamic or override)
 *
 * @author Thomas Draier
 */
public class DefaultValueListener extends DefaultEventListener {
    private static final Logger logger = LoggerFactory.getLogger(DefaultValueListener.class);

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED;
    }

    @Override
    public void onEvent(final EventIterator eventIterator) {
        try {

            JCRSessionWrapper eventSession = ((JCREventIterator)eventIterator).getSession();
            final JahiaUser user = eventSession.getUser();
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, workspace, null, new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Set<Session> sessions = null;
                    while (eventIterator.hasNext()) {
                        Event event = eventIterator.nextEvent();
                        try {
                            JCRNodeWrapper node = null;
                            String eventPath = event.getPath();
                            try {
                                // check in case of node added
                                if (event.getType() == Event.NODE_ADDED) {
                                    node = (JCRNodeWrapper) session.getItem(eventPath);
                                }
                                // check if mixin will bring new autocreated props
                                if (eventPath.endsWith(Constants.JCR_MIXINTYPES)) {
                                    String path = eventPath.substring(0, eventPath.lastIndexOf('/'));
                                    node = (JCRNodeWrapper) session.getItem(path.length() == 0 ? "/" : path);
                                }
                            } catch (PathNotFoundException e) {
                                continue;
                            }

                            if (node != null && handleNode(node)) {
                                node.getRealNode().getSession().save();
                                if (sessions == null) {
                                    sessions = new HashSet<Session>();
                                }
                                sessions.add(node.getRealNode().getSession());
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

    private boolean handleNode(JCRNodeWrapper node) throws RepositoryException {
        boolean anythingChanged = false;

        // No site resolved, no need to process autocreated i18n/overides props
        JCRSiteNode resolvedSite = node.getResolveSite();

        List<NodeType> nodeTypes = new ArrayList<>();
        NodeType primaryNodeType = node.getPrimaryNodeType();
        nodeTypes.add(primaryNodeType);
        NodeType[] mixin = node.getMixinNodeTypes();
        nodeTypes.addAll(Arrays.asList(mixin));
        List<Locale> locales = resolvedSite == null ? null : resolvedSite.getLanguagesAsLocales();

        for (NodeType nodeType : nodeTypes) {
            // Handle autoCreated properties
            ExtendedNodeType extendedNodeType = NodeTypeRegistry.getInstance().getNodeType(nodeType.getName());
            if (extendedNodeType != null && locales != null) {
                Collection<ExtendedPropertyDefinition> propertyDefinitions = extendedNodeType.getPropertyDefinitionsAsMap().values();
                    for (ExtendedPropertyDefinition propertyDefinition : propertyDefinitions) {

                        // Condition is:
                        // - Auto created
                        // - i18n or override
                        if (propertyDefinition.isAutoCreated() &&
                                (propertyDefinition.isInternationalized() || propertyDefinition.isOverride())) {
                            Value[] defValues = propertyDefinition.getDefaultValues();
                            for (Locale locale : locales) {
                                defValues = propertyDefinition.hasDynamicDefaultValues() ? propertyDefinition.getDefaultValues(locale) : defValues;
                                if (defValues.length > 0) {
                                    boolean handled = handlePropertyDefaultValues(node, propertyDefinition, defValues, locale);
                                    anythingChanged = anythingChanged || handled;
                                }
                            }
                    }
                }
            }

            // Handle autoCreated child nodes
            if (extendedNodeType != null) {
                Collection<ExtendedNodeDefinition> childNodeDefinitions = extendedNodeType.getChildNodeDefinitionsAsMap().values();
                for (ExtendedNodeDefinition definition : childNodeDefinitions) {
                    if (definition.isAutoCreated() && !node.hasNode(definition.getName())) {
                        JCRNodeWrapper autoCreated = node.addNode(definition.getName(), definition.getDefaultPrimaryTypeName());
                        if (autoCreated.isNodeType("jmix:originWS")) {
                            autoCreated.setProperty("j:originWS", workspace);
                        }
                        handleNode(autoCreated);
                        anythingChanged = true;
                    }
                }
            }
        }

        return anythingChanged;
    }

    protected boolean handlePropertyDefaultValues(JCRNodeWrapper n, ExtendedPropertyDefinition pd, Value[] values, Locale locale) throws RepositoryException {

        Node targetNode = pd.isInternationalized() ? n.getOrCreateI18N(locale) : n;
        String propertyName = pd.getName();

        if (targetNode.hasProperty(propertyName)) {
            // node already has the property -> return
            return false;
        }

        boolean valuesSet = false;
        if (values.length > 0) {
            if (pd.isMultiple()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting default values for property [{}].[{}]: {}", pd.getDeclaringNodeType().getName(), propertyName,
                            Arrays.stream(values).map(ThrowingFunction.unchecked(Value::getString)).collect(Collectors.joining( ", " )));
                }
                targetNode.setProperty(propertyName, values);
            } else {
                if (values.length == 1) {
                    targetNode.setProperty(propertyName, values[0]);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting default value for property [{}].[{}]: {}", pd.getDeclaringNodeType().getName(), propertyName, values[0].getString());
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
}
