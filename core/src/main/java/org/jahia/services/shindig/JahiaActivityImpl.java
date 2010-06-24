package org.jahia.services.shindig;

import org.apache.shindig.social.core.model.ActivityImpl;
import org.apache.shindig.social.opensocial.model.MediaItem;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
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

    private void populateValues() throws RepositoryException {
        this.setAppId(activityNode.getPropertyAsString("j:appID"));
        this.setBody(activityNode.getPropertyAsString("j:message"));
        this.setBodyId("");
        this.setExternalId("");
        this.setId(activityNode.getIdentifier());
        List<MediaItem> mediaItems = new ArrayList<MediaItem>();
        this.setMediaItems(mediaItems);
        this.setPostedTime(activityNode.getProperty("jcr:created").getDate().getTimeInMillis());
        this.setPriority(Float.valueOf((float)activityNode.getProperty("j:priority").getDouble()));
        this.setStreamFaviconUrl("");
        this.setStreamSourceUrl(activityNode.getParent().getUrl());
        this.setStreamTitle(activityNode.getParent().getPropertyAsString("jcr:title"));
        this.setStreamUrl(activityNode.getParent().getUrl());
    }

}
