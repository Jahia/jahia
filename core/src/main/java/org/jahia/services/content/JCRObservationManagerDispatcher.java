/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
        List<Event> external = null;
        while (events.hasNext()) {
            Event event = events.nextEvent();
            if (event instanceof EventImpl && !((EventImpl)event).isExternal()) {
                JCRObservationManager.addEvent(event, mountPoint, relativeRoot);
            } else {
                if (external == null) {
                    external = new ArrayList<Event>();
                }
                external.add(event);
            }
        }
        if (external != null) {
            final List<Event> fexternal = external;

            try {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        List<JCRObservationManager.EventWrapper> eventWrappers = new ArrayList<JCRObservationManager.EventWrapper>();
                        for (Event event : fexternal) {
                            if (event.getPath().equals(relativeRoot) || event.getPath().startsWith(relativeRoot + '/')) {
                                eventWrappers.add(JCRObservationManager.getEventWrapper(event, session, mountPoint, relativeRoot));
                            }
                        }
                        JCRObservationManager.consume(eventWrappers, session, JCRObservationManager.EXTERNAL_SYNC, JCRObservationManager.EXTERNAL_SYNC);
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        };
    }
}
