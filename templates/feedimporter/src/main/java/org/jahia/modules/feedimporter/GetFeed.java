package org.jahia.modules.feedimporter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.*;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Update the feed from the url
 */
public class GetFeed implements Action, BackgroundAction {
    private static Logger logger = Logger.getLogger(GetFeed.class);
    private String name;

    public String getName() {
        return name;
    }

    public void executeBackgroundAction(JCRNodeWrapper node) {
        try {
            getFeed(node.getSession(),node);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (JDOMException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(
                resource.getWorkspace(), resource.getLocale());

        final JCRNodeWrapper node = resource.getNode();

        getFeed(jcrSessionWrapper, node);

        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
    }

    private void getFeed(JCRSessionWrapper jcrSessionWrapper, JCRNodeWrapper node)
            throws RepositoryException, IOException, JDOMException {
        String remoteUrl = node.getProperty("url").getString();
        String remoteUser = null;
        if (node.hasProperty("user")) {
            remoteUser = node.getProperty("user").getString();
        }
        String remotePassword = null;
        if (node.hasProperty("password")) {
            remotePassword = node.getProperty("password").getString();
        }

        /*
        HttpClient client = new HttpClient();
        client.getParams().setAuthenticationPreemptive(true);

        final URL url = new URL(remoteUrl);
        client.getHostConfiguration().setHost(url.getHost(), url.getPort(), url.getProtocol());

        if (remoteUser != null) {
            org.apache.commons.httpclient.Credentials defaultcreds = new UsernamePasswordCredentials(remoteUser, remotePassword);
            client.getState()
                    .setCredentials(new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), defaultcreds);
        }

        GetMethod get = new GetMethod(remoteUrl);

        client.executeMethod(get);
        if (get.getStatusCode() != HttpServletResponse.SC_OK) {
            logger.error("Error received on prepare : "+get.getStatusCode());
            return new ActionResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, new JSONObject(new HashMap()));
        }

        InputStream feed = get.getResponseBodyAsStream();
        */

        NewsMLImporter newsMLImporter = new NewsMLImporter();
        newsMLImporter.importFeed(remoteUrl, remoteUser, remotePassword, node, jcrSessionWrapper);
    }
}
