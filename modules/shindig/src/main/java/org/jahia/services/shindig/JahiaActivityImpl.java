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

package org.jahia.services.shindig;

import org.apache.shindig.social.core.model.ActivityImpl;
import org.apache.shindig.social.opensocial.model.MediaItem;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Extension of Shindig's activity class to use a node reference internally.
 *
 * @author loom
 *         Date: Jun 24, 2010
 *         Time: 3:02:08 PM
 */
public class JahiaActivityImpl extends ActivityImpl {
    private JCRNodeWrapper activityNode;

    public JahiaActivityImpl(JCRNodeWrapper activityNode) throws RepositoryException {
        super();
        this.activityNode = activityNode;
        populateValues();
    }

    private double getPropertyAsDouble(JCRNodeWrapper node, String propertyName) {
        try {
            JCRPropertyWrapper propertyValue = node.getProperty(propertyName);
            if (propertyValue != null) {
                return propertyValue.getDouble();
            }
            return 0;
        } catch (RepositoryException re) {
            return 0;
        }
    }

    private long getPropertyAsTimeInMillis(JCRNodeWrapper node, String propertyName) {
        try {
            JCRPropertyWrapper propertyValue = node.getProperty(propertyName);
            if (propertyValue != null) {
                return propertyValue.getDate().getTimeInMillis();
            }
            return 0;
        } catch (RepositoryException re) {
            return 0;
        }
    }

    private void populateValues() throws RepositoryException {
        this.setId(activityNode.getIdentifier());
        this.setAppId(activityNode.getPropertyAsString("j:appID"));
        this.setBody(activityNode.getPropertyAsString("j:message"));
        this.setBodyId("");
        this.setExternalId("");
        this.setId(activityNode.getIdentifier());
        List<MediaItem> mediaItems = new ArrayList<MediaItem>();
        this.setMediaItems(mediaItems);
        this.setPostedTime(getPropertyAsTimeInMillis(activityNode, "jcr:created"));
        this.setPriority(Float.valueOf((float)getPropertyAsDouble(activityNode, "j:priority")));
        this.setStreamFaviconUrl("");
        this.setStreamSourceUrl(activityNode.getParent().getUrl());
        this.setStreamTitle(activityNode.getParent().getPropertyAsString("jcr:title"));
        this.setStreamUrl(activityNode.getParent().getUrl());
    }

}
