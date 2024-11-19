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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
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

    /**
     * List of properties that do not need to be checked by the UGCListener even if there are create in live.
     */
    private Set<String> excludePropertiesFromUGCCheck;

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

                if (CollectionUtils.isNotEmpty(excludePropertiesFromUGCCheck) && excludePropertiesFromUGCCheck.contains(propertyName)) {
                    continue;
                }

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

    public Set<String> getExcludePropertiesFromUGCCheck() {
        return excludePropertiesFromUGCCheck;
    }

    public void setExcludePropertiesFromUGCCheck(String excludePropertiesFromUGCCheck) {
        if (StringUtils.isNotEmpty(excludePropertiesFromUGCCheck)) {
            this.excludePropertiesFromUGCCheck = Sets.newHashSet(Splitter.on(",").trimResults().omitEmptyStrings().split(excludePropertiesFromUGCCheck));
        }
    }
}
