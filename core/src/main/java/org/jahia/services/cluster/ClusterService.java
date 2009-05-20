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
 package org.jahia.services.cluster;

import org.jahia.services.JahiaService;

/**
 * Clustering service to communicate with all nodes in the cluster.
 * User: Serge Huber
 * Date: Jul 12, 2005
 * Time: 6:01:22 PM
 */
public abstract class ClusterService extends JahiaService {

    private boolean activated = true;

    public abstract void sendMessage(ClusterMessage message);

    public abstract void addListener(ClusterListener listener);

    public abstract void removeListener(ClusterListener listener);

    public abstract int getNbNodes();

    public abstract String getServerId();

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}
