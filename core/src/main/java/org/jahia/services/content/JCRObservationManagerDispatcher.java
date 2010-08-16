/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content;

import org.apache.jackrabbit.core.observation.SynchronousEventListener;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.Event;


/**
 * This listener gets all event from the repository synchronously and store them in the observation manager.
 * Events will be consumed by all listeners when JCRObservationManager.consume() is called.
 *
 * User: toto
 * Date: Nov 25, 2009
 * Time: 1:59:20 PM
 */
public class JCRObservationManagerDispatcher implements SynchronousEventListener {

    protected JCRStoreProvider provider;

    public void setProvider(JCRStoreProvider provider) {
        this.provider = provider;
    }

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED + Event.NODE_MOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
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
    public void onEvent(EventIterator events) {
        while (events.hasNext()) {
            Event event = (Event) events.next();

            JCRObservationManager.addEvent(event);
        }
    }
}
