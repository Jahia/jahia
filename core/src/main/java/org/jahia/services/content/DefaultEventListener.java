/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.jackrabbit.core.observation.EventImpl;
import org.jahia.api.Constants;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 14 janv. 2008
 * Time: 15:56:43
 * To change this template use File | Settings | File Templates.
 */
public abstract class DefaultEventListener implements EventListener {

    protected Set<String> propertiesToIgnore;
    protected String workspace;

    protected DefaultEventListener() {
        propertiesToIgnore = new HashSet<String>();
        propertiesToIgnore.add(Constants.JCR_PRIMARYTYPE);
        propertiesToIgnore.add(Constants.JCR_UUID);
        propertiesToIgnore.add(Constants.JCR_CREATED);
        propertiesToIgnore.add(Constants.JCR_CREATEDBY);
        propertiesToIgnore.add(Constants.JCR_LASTMODIFIED);
        propertiesToIgnore.add(Constants.JCR_LASTMODIFIEDBY);
        propertiesToIgnore.add(Constants.LASTPUBLISHED);
        propertiesToIgnore.add(Constants.LASTPUBLISHEDBY);        
        propertiesToIgnore.add(Constants.PUBLISHED);
        propertiesToIgnore.add(Constants.JCR_LOCKOWNER);
        propertiesToIgnore.add(Constants.JCR_LOCKISDEEP);
        propertiesToIgnore.add(Constants.LOCKTOKEN);
        propertiesToIgnore.add(Constants.JCR_ISCHECKEDOUT);
        propertiesToIgnore.add(Constants.JCR_VERSIONHISTORY);
        propertiesToIgnore.add(Constants.JCR_PREDECESSORS);
        propertiesToIgnore.add(Constants.JCR_SUCCESSORS);
        propertiesToIgnore.add(Constants.JCR_BASEVERSION);
        propertiesToIgnore.add(Constants.JCR_FROZENUUID);
        propertiesToIgnore.add(Constants.FULLPATH);
        propertiesToIgnore.add(Constants.NODENAME);
        propertiesToIgnore.add(Constants.PROCESSID);
        propertiesToIgnore.add(Constants.JCR_MERGEFAILED);
        propertiesToIgnore.add(Constants.REVISION_NUMBER);

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

    public abstract String getPath();

    public abstract String[] getNodeTypes();

}
