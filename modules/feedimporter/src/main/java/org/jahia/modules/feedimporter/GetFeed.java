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

package org.jahia.modules.feedimporter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
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
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(GetFeed.class);
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

        InputStream categoryMappingsInputStream = this.getClass().getClassLoader().getResourceAsStream("category_mappings.properties");
        Properties categoryMappings = new Properties();
        categoryMappings.load(categoryMappingsInputStream);
        NewsMLImporter newsMLImporter = new NewsMLImporter(ServicesRegistry.getInstance().getCategoryService(), categoryMappings);
        newsMLImporter.importFeed(remoteUrl, remoteUser, remotePassword, feedNode, jcrSessionWrapper);
    }
}
