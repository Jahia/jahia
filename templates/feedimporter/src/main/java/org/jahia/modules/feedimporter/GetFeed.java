package org.jahia.modules.feedimporter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.registries.ServicesRegistry;
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

    private void getFeed(JCRSessionWrapper jcrSessionWrapper, JCRNodeWrapper feedNode)
            throws RepositoryException, IOException, JDOMException {
        String remoteUrl = feedNode.getProperty("url").getString();
        String remoteUser = null;
        if (feedNode.hasProperty("user")) {
            remoteUser = feedNode.getProperty("user").getString();
        }
        String remotePassword = null;
        if (feedNode.hasProperty("password")) {
            remotePassword = feedNode.getProperty("password").getString();
        }

        NewsMLImporter newsMLImporter = new NewsMLImporter(ServicesRegistry.getInstance().getCategoryService(), new HashMap<String,String>());
        newsMLImporter.importFeed(remoteUrl, remoteUser, remotePassword, feedNode, jcrSessionWrapper);
    }
}
