/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.jackrabbit.core.observation.EventImpl;
import org.apache.jackrabbit.core.observation.SynchronousEventListener;
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
public abstract class DefaultEventListener implements SynchronousEventListener {

    protected Set<String> propertiesToIgnore;
    protected JCRStoreProvider provider;
    protected String workspace;

    protected DefaultEventListener() {
        propertiesToIgnore = new HashSet<String>();
        propertiesToIgnore.add(Constants.JCR_PRIMARYTYPE);
        propertiesToIgnore.add(Constants.JCR_UUID);
        propertiesToIgnore.add(Constants.JCR_CREATED);
        propertiesToIgnore.add(Constants.JCR_CREATEDBY);
        propertiesToIgnore.add(Constants.JCR_LASTMODIFIED);
        propertiesToIgnore.add(Constants.JCR_LASTMODIFIEDBY);
        propertiesToIgnore.add(Constants.JCR_LOCKOWNER);
        propertiesToIgnore.add(Constants.JCR_LOCKISDEEP);

    }

    public void setProvider(JCRStoreProvider provider) {
        this.provider = provider;
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
