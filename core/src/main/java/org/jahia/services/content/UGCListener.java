/*
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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.*;

/**
 * Listener that watches creation of User Generated Content on published nodes.
 *
 * When a node that was existing in default workspace is modified in live, this listener stores the list of properties
 * that are modified, and the list of mixins that are added.
 *
 * Node that are created in live workspace only are not touched by this listener.
 *
 */
public class UGCListener extends DefaultEventListener {
    private static Logger logger = LoggerFactory.getLogger(UGCListener.class);


    public int getEventTypes() {
        return Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED;
    }

    @Override
    public void onEvent(final EventIterator events) {
        final JCRSessionWrapper eventSession = ((JCREventIterator) events).getSession();

        final JahiaUser user = eventSession.getUser();

        final Map<String, Set<String>> propertiesByNode = new HashMap<>();
        try {
            while (events.hasNext()) {
                Event event = events.nextEvent();
                String nodePath = StringUtils.substringBeforeLast(event.getPath(), "/");
                String propertyName = StringUtils.substringAfterLast(event.getPath(), "/");
                JCRNodeWrapper node = eventSession.getNode(nodePath);
                if (node.hasProperty("j:originWS") && node.getProperty("j:originWS").getString().equals("default")) {
                    if (!propertiesByNode.containsKey(node.getIdentifier())) {
                        propertiesByNode.put(node.getIdentifier(), new HashSet<String>());
                    }
                    if (propertyName.equals(Constants.JCR_MIXINTYPES)) {
                        if (node instanceof JCRNodeDecorator) {
                            node = ((JCRNodeDecorator) node).getDecoratedNode();
                        }
                        if (node instanceof JCRNodeWrapperImpl) {
                            List<ExtendedNodeType> newMixins = new ArrayList<>(Arrays.asList(node.getMixinNodeTypes()));
                            newMixins.removeAll(Arrays.asList(((JCRNodeWrapperImpl) node).getOriginalMixinNodeTypes()));
                            for (ExtendedNodeType newMixin : newMixins) {
                                propertiesByNode.get(node.getIdentifier()).add(propertyName + "=" + newMixin.getName());
                            }
                        }
                    } else {
                        propertiesByNode.get(node.getIdentifier()).add(propertyName);
                    }
                }
            }

            if (!propertiesByNode.isEmpty()) {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, workspace, null, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper s) throws RepositoryException {
                        Set<Session> sessions = new HashSet<>();
                        for (Map.Entry<String, Set<String>> entry : propertiesByNode.entrySet()) {
                            JCRNodeWrapper node = s.getNodeByIdentifier(entry.getKey());
                            if (!node.isNodeType("jmix:liveProperties")) {
                                node.addMixin("jmix:liveProperties");
                            }
                            JCRPropertyWrapper property = node.hasProperty("j:liveProperties") ? node.getProperty("j:liveProperties") : node.setProperty("j:liveProperties", new Value[0]);
                            for (JCRValueWrapper valueWrapper : property.getValues()) {
                                entry.getValue().remove(valueWrapper.getString());
                            }
                            for (String value : entry.getValue()) {
                                property.addValue(value);
                            }
                            sessions.add(node.getRealNode().getSession());
                        }

                        for (Session session : sessions) {
                            if (session.hasPendingChanges()) {
                                session.save();
                            }
                        }
                        return null;
                    }
                });

            }
        } catch (RepositoryException e) {
            logger.error("Cannot store live properties changes",e);
        }
    }
}
