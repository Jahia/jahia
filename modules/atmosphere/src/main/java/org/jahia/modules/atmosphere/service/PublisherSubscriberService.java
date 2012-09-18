package org.jahia.modules.atmosphere.service;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 22/11/11
 * Time: 10:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class PublisherSubscriberService {

    private JCRTemplate jcrTemplate;

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void publishToSite(JCRNodeWrapper node, String message) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("body", message);
            // lookup for a parent of type page
            JCRNodeWrapper parentOfType = JCRContentUtils.getParentOfType(node, "jnt:page");
            jsonObject.put("url", parentOfType!=null?parentOfType.getUrl():node.getUrl());
            jsonObject.put("name", node.getDisplayableName());
            broadcast(node.getResolveSite().getSiteKey(), jsonObject.toString(), true);
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void publishToNodeChannel(JCRNodeWrapper node, String message) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("body", message);
            jsonObject.put("url", node.getUrl());
            jsonObject.put("name", node.getDisplayableName());
            broadcast(node.getIdentifier(), jsonObject.toString(), false);

        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void publishToAbsoluteChannel(String absoluteChannelName, String message) {
        broadcast(absoluteChannelName, message, false);
    }

    private void broadcast(String broadcasterID, String message, boolean createIfNull) {
        Broadcaster broadcaster = BroadcasterFactory.getDefault().lookup(broadcasterID, createIfNull);
        if (broadcaster != null) {
            broadcaster.broadcast(message);
        }
    }


}
