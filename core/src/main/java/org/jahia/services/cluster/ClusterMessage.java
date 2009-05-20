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

import java.io.Serializable;

/**
 * A simple cluster message interface.
 * User: Serge Huber
 * Date: Jul 12, 2005
 * Time: 6:30:22 PM
 */
public class ClusterMessage implements Serializable {

    protected Serializable object;

    public ClusterMessage() {
    }

    public ClusterMessage(Serializable object) {
        this.object = object;
    }

    public Serializable getObject() {
        return object;
    }
    
    public void setObject(Serializable object) {
        this.object = object;
    }

}
