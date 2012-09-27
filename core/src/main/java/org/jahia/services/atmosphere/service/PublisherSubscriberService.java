package org.jahia.services.atmosphere.service;

import org.apache.jackrabbit.util.Text;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.RepositoryException;
import java.util.List;
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
            final JCRSiteNode resolveSite = node.getResolveSite();
            final List<Locale> activeLanguagesAsLocales = resolveSite.getActiveLiveLanguagesAsLocales();
            for (Locale activeLanguagesAsLocale : activeLanguagesAsLocales) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("body", JahiaResourceBundle.getString(null,message,
                        activeLanguagesAsLocale, resolveSite.getTemplatePackageName()));
                // lookup for a parent of type page
                JCRNodeWrapper parentOfType = JCRContentUtils.getParentOfType(node, "jnt:page");
                String path = "/cms/render";
                final String url =
                        Jahia.getContextPath() + path + "/" + resolveSite.getSession().getWorkspace().getName() + "/" +
                        activeLanguagesAsLocale + Text.escapePath(
                                parentOfType != null ? parentOfType.getPath() : node.getPath()) + ".html";
                jsonObject.put("url", url);
                jsonObject.put("name", node.getDisplayableName());
                broadcast(resolveSite.getSiteKey() + "__" + activeLanguagesAsLocale.getDisplayName(Locale.ENGLISH),
                        jsonObject.toString(), true);
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void publishToNodeChannel(JCRNodeWrapper node, String message) {
        try {
            final JCRSiteNode resolveSite = node.getResolveSite();
            final List<Locale> activeLanguagesAsLocales = resolveSite.getActiveLiveLanguagesAsLocales();
            for (Locale activeLanguagesAsLocale : activeLanguagesAsLocales) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("body", JahiaResourceBundle.getString(null,message,
                        activeLanguagesAsLocale, resolveSite.getTemplatePackageName()));
                JCRNodeWrapper parentOfType = JCRContentUtils.getParentOfType(node, "jnt:page");
                String path = "/cms/render";
                final String url =
                        Jahia.getContextPath() + path + "/" + resolveSite.getSession().getWorkspace().getName() + "/" +
                        activeLanguagesAsLocale + Text.escapePath(
                                parentOfType != null ? parentOfType.getPath() : node.getPath()) + ".html";
                jsonObject.put("url", url);
                jsonObject.put("name", node.getDisplayableName());
                broadcast(node.getIdentifier() + "__" + activeLanguagesAsLocale.getDisplayName(Locale.ENGLISH),
                        jsonObject.toString(), false);
            }

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
        final BroadcasterFactory broadcasterFactory = BroadcasterFactory.getDefault();
        if (broadcasterFactory != null) {
            Broadcaster broadcaster = broadcasterFactory.lookup(broadcasterID, createIfNull);
            if (broadcaster != null) {
                broadcaster.broadcast(message);
            }
        }
    }


}
