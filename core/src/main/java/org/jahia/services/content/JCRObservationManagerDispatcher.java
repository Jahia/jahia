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

import org.apache.jackrabbit.core.observation.EventImpl;
import org.apache.jackrabbit.core.observation.SynchronousEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.Event;

import java.util.ArrayList;
import java.util.List;


/**
 * This listener gets all event from the repository synchronously and store them in the observation manager.
 * Events will be consumed by all listeners when JCRObservationManager.consume() is called.
 *
 * @author toto
 */
public class JCRObservationManagerDispatcher implements SynchronousEventListener {

    private static final Logger logger = LoggerFactory.getLogger(JCRObservationManagerDispatcher.class);

    private String workspace;
    private String mountPoint;
    private String relativeRoot;

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public String getRelativeRoot() {
        return relativeRoot;
    }

    public void setRelativeRoot(String relativeRoot) {
        this.relativeRoot = relativeRoot;
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     *
     *
     * The workspace-write methods are: •	Workspace.move, copy, clone, restore, importXML, createActivity,
     * merge.
     • Methods of org.xml.sax.ContentHandler acquired through Workspace.getContentHandler.
     • Node.checkin, checkout, checkpoint, restore, restoreByLabel, update, merge, cancelMerge, doneMerge,
     * createConfiguration, and followLifecycleTransition.
     • LockManager.lock, and unlock. •	VersionHistory.addVersionLabel, removeVersionLabel and
     * removeVersion.
     • Session.save.
     • Workspace.createWorkspace and deleteWorkspace (these create or delete another workspace, though they do not affect this workspace).
     *
     * @param events The event set received.
     */
    @Override
    public void onEvent(EventIterator events) {
        // Local events are stored in thread local observation manager and will be consumed after saving the session
        List<Event> external = null;
        while (events.hasNext()) {
            Event event = events.nextEvent();
            if (event instanceof EventImpl && !((EventImpl)event).isExternal()) {
                JCRObservationManager.addEvent(event, mountPoint, relativeRoot);
            } else {
                if (external == null) {
                    external = new ArrayList<>();
                }
                external.add(event);
            }
        }

        // External events are consumed immediately, and we need to use a system session to process them.
        if (external != null) {
            final List<Event> fexternal = external;

            try {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, session -> {
                    List<JCRObservationManager.EventWrapper> eventWrappers = new ArrayList<>();
                    for (Event event : fexternal) {
                        if (event.getPath().equals(relativeRoot) || event.getPath().startsWith(relativeRoot + '/')) {
                            eventWrappers.add(JCRObservationManager.getEventWrapper(event, session, mountPoint, relativeRoot));
                        }
                    }
                    JCRObservationManager.consume(eventWrappers, session, JCRObservationManager.EXTERNAL_SYNC, JCRObservationManager.EXTERNAL_SYNC);
                    return null;
                });
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        };
    }
}
