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
