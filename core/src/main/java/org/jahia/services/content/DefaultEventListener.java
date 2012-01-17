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

import static org.jahia.api.Constants.*;

import org.apache.jackrabbit.core.observation.EventImpl;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import java.util.HashSet;
import java.util.Set;

/**
 * User: toto
 * Date: 14 janv. 2008
 * Time: 15:56:43
 * 
 */
public abstract class DefaultEventListener implements EventListener {

    protected Set<String> propertiesToIgnore;
    protected String workspace;
    protected boolean availableDuringPublish = false;

    protected DefaultEventListener() {
        propertiesToIgnore = new HashSet<String>();
        propertiesToIgnore.add(JCR_PRIMARYTYPE);
        propertiesToIgnore.add(JCR_UUID);
        propertiesToIgnore.add(JCR_CREATED);
        propertiesToIgnore.add(JCR_CREATEDBY);
        propertiesToIgnore.add(JCR_LASTMODIFIED);
        propertiesToIgnore.add(JCR_LASTMODIFIEDBY);
        propertiesToIgnore.add(LASTPUBLISHED);
        propertiesToIgnore.add(LASTPUBLISHEDBY);        
        propertiesToIgnore.add(PUBLISHED);
        propertiesToIgnore.add(JCR_LOCKOWNER);
        propertiesToIgnore.add(JCR_LOCKISDEEP);
        propertiesToIgnore.add(LOCKTOKEN);
        propertiesToIgnore.add(LOCKTYPES);
        propertiesToIgnore.add(JCR_ISCHECKEDOUT);
        propertiesToIgnore.add(JCR_VERSIONHISTORY);
        propertiesToIgnore.add(JCR_PREDECESSORS);
        propertiesToIgnore.add(JCR_SUCCESSORS);
        propertiesToIgnore.add(JCR_BASEVERSION);
        propertiesToIgnore.add(JCR_FROZENUUID);
        propertiesToIgnore.add(FULLPATH);
        propertiesToIgnore.add(NODENAME);
        propertiesToIgnore.add(PROCESSID);
        propertiesToIgnore.add(JCR_MERGEFAILED);
        propertiesToIgnore.add(REVISION_NUMBER);
        propertiesToIgnore.add(CHECKIN_DATE);
        propertiesToIgnore.add(JCR_LASTLOGINDATE);
        propertiesToIgnore.add(ORIGIN_WORKSPACE);
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    protected boolean isExternal(Event event) {
        // Jackrabbit / cluster workaround
        if (event instanceof EventImpl) {
            return ((EventImpl)event).isExternal();
        }
        return false;
    }

    public abstract int getEventTypes();

    public String getPath() {
        return "/";
    }

    public boolean isDeep() {
        return true;
    }

    public String[] getNodeTypes() {
        return null;
    }

    public String[] getUuids() {
        return null;
    }

    public boolean isAvailableDuringPublish() {
        return availableDuringPublish;
    }

    public void setAvailableDuringPublish(boolean availableDuringPublish) {
        this.availableDuringPublish = availableDuringPublish;
    }

    public String getWorkspace() {
        return workspace;
    }
}
