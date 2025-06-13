/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.*;

import static org.jahia.api.Constants.*;

/**
 * This listener is responsible for two critical functions related to publication system:
 * <p>
 * 1. Content Modification Tracking:
 *    - Updates the lastModified and lastModifiedBy properties on nodes when content changes
 *    - This tracking is essential for publication status calculation, comparing lastModified
 *      date with lastPublished date to determine if content needs republication
 * <p>
 * 2. Auto-Publication Management:
 *    - Automatically publishes nodes marked with the jmix:autoPublish mixin
 *    - Ensures content changes in default workspace are immediately reflected in live workspace
 *    - Maintains deleted node tracking for published content
 * <p>
 * Special Cases:
 * - Import operations: By default, lastModified dates are not updated during imports unless the node
 *   is auto-published, preserving the original modification date from the imported content
 * - Resource/File handling: When file content changes (nt:resource), both the resource node and its
 *   parent file node get their lastModified properties updated
 * - Origin workspace tracking: For newly created nodes with the jmix:originWS mixin, the j:originWS
 *   property is automatically set to the current workspace name. This helps track where content was
 *   originally created, which is useful for cross-workspace operations.
 * - Deletion tracking:
 *      - When published nodes are deleted (bypassing mark-for-deletion, like direct delete in JCR default workspace),
 *      their identifiers are stored in j:deletedChildren on the parent to facilitate proper publication of the removal.
 *      - When a node is deleted, parent lastModified date is updated to reflect the change.
 *
 * @author toto
 * @since JAHIA 6.5
 */
public class LastModifiedListener extends DefaultEventListener {
    public static final String J_DELETED_CHILDREN = "j:deletedChildren";

    private static final Logger logger = LoggerFactory.getLogger(LastModifiedListener.class);

    private JCRPublicationService publicationService;

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_CHANGED + 
                Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED + Event.NODE_MOVED;
    }

    @SuppressWarnings("java:S3776")
    public void onEvent(final EventIterator eventIterator) {
        try {
            // Get user information and operation type from event context
            final JahiaUser user = ((JCREventIterator)eventIterator).getSession().getUser();
            final boolean isImport = ((JCREventIterator)eventIterator).getOperationType() == JCRObservationManager.IMPORT;

            // Track sessions that need to be saved
            final Set<Session> sessionsToSave = new LinkedHashSet<>();

            // Track different node paths based on operation type
            final Set<String> modifiedNodePaths = new LinkedHashSet<>();     // Modified nodes
            final Set<String> addedNodePaths = new LinkedHashSet<>();        // Newly added nodes
            final Set<String> reorderedNodePaths = new LinkedHashSet<>();    // Nodes that were just reordered

            // Only create auto-publish list for default workspace
            final List<String> autoPublishedIds;
            if (workspace.equals("default")) {
                autoPublishedIds = new ArrayList<>();
            } else {
                autoPublishedIds = null;
            }

            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, workspace, null, new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Calendar modificationTime = Calendar.getInstance();
                    while (eventIterator.hasNext()) {
                        final Event event = eventIterator.nextEvent();

                        // Special handling for node removal events
                        if (event.getType() == Event.NODE_REMOVED && event.getIdentifier() != null) {
                            processNodeRemovalEvent(event, session, user, sessionsToSave);
                        }

                        // Filter out events we don't need to process
                        String path = event.getPath();
                        if (path.startsWith("/jcr:system/") || shouldPropertyEventBeSkipped(event.getType(), path)) {
                            continue;
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("Receiving event for lastModified date for : {}", path);
                        }

                        // Categorize the event based on type
                        if (event.getType() == Event.NODE_ADDED) {
                            addedNodePaths.add(path);
                        } else if (Event.NODE_MOVED == event.getType()) {
                            // For move events, differentiate between real moves and reordering
                            if (event.getInfo().get("srcChildRelPath") != null) {
                                // Node reordering within the same parent
                                reorderedNodePaths.add(path);
                            }
                            // For both moves and reordering, track the parent
                            modifiedNodePaths.add(StringUtils.substringBeforeLast(path, "/"));
                        } else {
                            // For property changes and other events, track the parent node
                            modifiedNodePaths.add(StringUtils.substringBeforeLast(path, "/"));
                        }
                    }

                    // Remove reordered nodes from added nodes to avoid duplicate processing
                    if (!reorderedNodePaths.isEmpty()) {
                        addedNodePaths.removeAll(reorderedNodePaths);
                    }

                    // Remove added nodes from the modified nodes set to avoid duplicate processing
                    if (!addedNodePaths.isEmpty()) {
                        modifiedNodePaths.removeAll(addedNodePaths);
                    }

                    // If we have nodes to process, update their lastModified properties
                    if (!modifiedNodePaths.isEmpty() || !addedNodePaths.isEmpty()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Updating lastModified date for existing nodes : {}",
                                    Arrays.deepToString(modifiedNodePaths.toArray(new String[0])));
                            logger.debug("Updating lastModified date for added nodes : {}",
                                    Arrays.deepToString(addedNodePaths.toArray(new String[0])));
                        }

                        // Process existing modified nodes
                        for (String nodePath : modifiedNodePaths) {
                            try {
                                JCRNodeWrapper node = session.getNode(nodePath);
                                sessionsToSave.add(node.getRealNode().getSession());
                                updateLastModifiedProperties(node, modificationTime, user, autoPublishedIds, isImport);
                            } catch (UnsupportedRepositoryOperationException | PathNotFoundException e) {
                                // Cannot write property - node might be protected or
                                // Node is removed
                            }
                        }

                        // Process newly added nodes
                        for (String addedNodePath : addedNodePaths) {
                            try {
                                JCRNodeWrapper node = session.getNode(addedNodePath);
                                sessionsToSave.add(node.getRealNode().getSession());

                                // Set origin workspace for new nodes with the appropriate mixin
                                if (!node.hasProperty("j:originWS") && node.isNodeType("jmix:originWS")) {
                                    node.setProperty("j:originWS", workspace);
                                }

                                updateLastModifiedProperties(node, modificationTime, user, autoPublishedIds, isImport);
                            } catch (UnsupportedRepositoryOperationException | PathNotFoundException e) {
                                // Cannot write property - node might be protected or
                                // Node has been removed during processing
                            }
                        }

                        // Save all modified sessions
                        for (Session jcrsession : sessionsToSave) {
                            try {
                                jcrsession.save();
                            } catch (RepositoryException e) {
                                logger.debug("Cannot update lastModification properties");
                            }
                        }
                    }
                    return null;
                }
            });

            // If we found auto-published nodes in default workspace, publish them to live
            if (autoPublishedIds != null && !autoPublishedIds.isEmpty()) {
                synchronized (this) {
                    publicationService.publish(autoPublishedIds, "default", "live", false, null);
                }
            }

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Determines if a property-related event should be skipped during processing.
     * This method filters out events for specific properties that don't require
     * updating the lastModified status of a node (like technical properties).
     *
     * @param eventType The type of event (PROPERTY_CHANGED, PROPERTY_ADDED, PROPERTY_REMOVED)
     * @param path The path of the property that triggered the event
     * @return true if the event should be skipped, false if it should be processed
     */
    private boolean shouldPropertyEventBeSkipped(int eventType, String path) {
        return (eventType & Event.PROPERTY_CHANGED + Event.PROPERTY_ADDED + Event.PROPERTY_REMOVED) != 0 &&
                propertiesToIgnore.contains(StringUtils.substringAfterLast(path, "/"));
    }

    /**
     * Processes node removal events, handling special cases for published content.
     * When a published node is deleted, its identifier is stored in j:deletedChildren
     * property of the parent to track that it was published and now needs unpublication.
     *
     * @param event The removal event to process
     * @param session The JCR session to use
     * @param user The current user
     * @param sessions Collection to track sessions that need saving
     * @throws RepositoryException if a repository error occurs
     */
    private void processNodeRemovalEvent(final Event event, JCRSessionWrapper session,
                                        final JahiaUser user, Set<Session> sessions) throws RepositoryException {
        try {
            // First check if the node still exists (might be a move rather than delete)
            session.getNodeByIdentifier(event.getIdentifier());
        } catch (ItemNotFoundException infe) {
            try {
                // Get the parent of the deleted node
                final JCRNodeWrapper parent = session.getNode(StringUtils.substringBeforeLast(event.getPath(), "/"));

                // Only process if in default workspace and on the main provider
                if (!session.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE) &&
                    parent.getProvider().getMountPoint().equals("/")) {

                    // Check if the node was published by looking in live workspace
                    boolean lastPublished = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(
                        user, Constants.LIVE_WORKSPACE, null, liveSession -> {
                            try {
                                JCRNodeWrapper nodeByIdentifier = liveSession.getNodeByIdentifier(event.getIdentifier());
                                boolean hasLastPublished = nodeByIdentifier.hasProperty(Constants.LASTPUBLISHED);

                                // Handle auto-published nodes - delete them immediately from live
                                if (hasLastPublished && !parent.isNodeType(JAHIAMIX_AUTO_PUBLISH)) {
                                    List<String> nodeTypes = (event instanceof JCRObservationManager.EventWrapper) ?
                                            ((JCRObservationManager.EventWrapper) event).getNodeTypes() : null;

                                    if (nodeTypes != null) {
                                        for (String nodeType : nodeTypes) {
                                            ExtendedNodeType eventNodeType = NodeTypeRegistry.getInstance().getNodeType(nodeType);
                                            if (eventNodeType != null && eventNodeType.isNodeType(JAHIAMIX_AUTO_PUBLISH)) {
                                                // If the node itself was auto-published, remove it from live directly
                                                nodeByIdentifier.remove();
                                                liveSession.save();
                                                return false;
                                            }
                                        }
                                    }
                                }
                                return hasLastPublished;
                            } catch (RepositoryException e) {
                                // Node doesn't exist in live
                                return false;
                            }
                        });

                    // If the node was published, track it in parent's deletedChildren property
                    if (lastPublished) {
                        if (!parent.isNodeType("jmix:deletedChildren")) {
                            // Add the mixin if not already present
                            parent.addMixin("jmix:deletedChildren");
                            parent.setProperty(J_DELETED_CHILDREN, new String[]{event.getIdentifier()});
                        } else if (!parent.hasProperty(J_DELETED_CHILDREN)) {
                            // Initialize the property if mixin exists but property doesn't
                            parent.setProperty(J_DELETED_CHILDREN, new String[]{event.getIdentifier()});
                        } else {
                            // Add to existing property values
                            parent.getProperty(J_DELETED_CHILDREN).addValue(event.getIdentifier());
                        }
                        sessions.add(parent.getRealNode().getSession());
                    }
                }
            } catch (PathNotFoundException | ItemNotFoundException e) {
                // Parent node not found or
                // Live node not found
            }
        }
    }

    /**
     * Updates the lastModified properties on a node, and checks for auto-publication.
     * Traverses up the node hierarchy to find the first node with mix:lastModified mixin.
     *
     * @param node The node to process
     * @param modificationTime Calendar instance with current time
     * @param user Current user
     * @param autoPublishedIds List to collect auto-published node IDs (null for live workspace)
     * @param isImport Flag indicating if operation is an import
     * @throws RepositoryException if a repository error occurs
     */
    private void updateLastModifiedProperties(JCRNodeWrapper node, Calendar modificationTime, JahiaUser user, 
                                       List<String> autoPublishedIds, boolean isImport) throws RepositoryException {
        // Find the first node (or ancestor) that has the MIX_LAST_MODIFIED mixin
        while (!node.isNodeType(MIX_LAST_MODIFIED)) {
            // Check each node for auto-publish on the way up
            registerForAutoPublication(node, autoPublishedIds);
            try {
                node = node.getParent();
            } catch (ItemNotFoundException e) {
                // Reached the root without finding mix:lastModified
                return;
            }
        }

        // Check if this node should be auto-published
        boolean isAutoPublished = registerForAutoPublication(node, autoPublishedIds);

        // Only update properties if not an import or if node is auto-published
        // This preserves original modification dates on imported content unless auto-published
        if (!isImport || isAutoPublished) {
            node.getSession().checkout(node);
            node.setProperty(JCR_LASTMODIFIED, modificationTime);
            node.setProperty(JCR_LASTMODIFIEDBY, user != null ? user.getUsername() : "");

            // Special handling for nt:resource nodes - also update parent file node
            if (node.isNodeType("nt:resource")) {
                JCRNodeWrapper parent = node.getParent();
                if (parent.isNodeType(MIX_LAST_MODIFIED)) {
                    parent.setProperty(JCR_LASTMODIFIED, modificationTime);
                    parent.setProperty(JCR_LASTMODIFIEDBY, user != null ? user.getUsername() : "");
                }
            }
        }
    }

    /**
     * Checks if a node should be auto-published and adds it to the list if needed.
     *
     * @param node The node to check
     * @param autoPublishedIds List to collect auto-published node IDs (null for live workspace)
     * @return true if node was added to auto-publish list, false otherwise
     * @throws RepositoryException if a repository error occurs
     */
    private boolean registerForAutoPublication(JCRNodeWrapper node, List<String> autoPublishedIds) throws RepositoryException {
        if (autoPublishedIds != null &&
                !node.getPath().startsWith("/modules") &&
                !autoPublishedIds.contains(node.getIdentifier()) &&
                (node.isNodeType(JAHIAMIX_AUTO_PUBLISH) || (node.isNodeType(JAHIANT_TRANSLATION) && node.getParent().isNodeType(JAHIAMIX_AUTO_PUBLISH)))) {
            autoPublishedIds.add(node.getIdentifier());
            return true;
        }
        return false;
    }

    public synchronized void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }
}
