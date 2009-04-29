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
 package org.jahia.services.htmlparser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.jahia.utils.JahiaTools;

/**
 * <p>Title: Used to transform absolute url to relative url by removing the precised server URL </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Khue Nguyen
 * @version 1.0
 */

public class ActiveXServerURLCleanupDOMVisitor implements HtmlDOMVisitor {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger( ActiveXServerURLCleanupDOMVisitor.class);

    private String serverURL = "";

    private String[][] tagAndAttributesWithLinks = {
        /* This list is based on the HTML 4.01 DTD, available here :
           http://www.w3.org/TR/html401/sgml/dtd.html
         */
        { "a", "href" },
        { "img", "src" },
        { "img", "longdesc" },
        { "img", "usemap" },
        { "area", "href" },
        { "link", "href" },
        { "object", "classid" },
        { "object", "codebase" },
        { "object", "data" },
        { "object", "usemap" },
        { "q", "cite" },
        { "blockquote", "cite" },
        { "ins", "cite" },
        { "del", "cite" },
        { "form", "action" },
        { "input", "src" },
        { "input", "usemap" },
        { "head", "profile" },
        { "base", "href" },
        { "script", "src" },
        { "script", "for" }
    };

    private Map linkAttributesByTagName = new HashMap();

    /**
     *
     * @param serverURL String
     */
    public ActiveXServerURLCleanupDOMVisitor(String serverURL) {
        this.serverURL = serverURL;
        if ( this.serverURL == null ){
            this.serverURL = "";
        } else {
            this.serverURL = serverURL.trim();
        }
        if ( !"".equals(this.serverURL) ){
            this.serverURL = JahiaTools.replacePatternIgnoreCase(this.serverURL,"&",TidyHtmlParser.AMPERSAND);
            for (int i = 0; i < tagAndAttributesWithLinks.length; i++) {
                String tagName = tagAndAttributesWithLinks[i][0];
                String attributeName = tagAndAttributesWithLinks[i][1];
                Set tagAttributes = null;
                if (!linkAttributesByTagName.containsKey(tagName)) {
                    tagAttributes = new HashSet();
                    linkAttributesByTagName.put(tagName, tagAttributes);
                }
                else {
                    tagAttributes = (Set) linkAttributesByTagName.get(tagName);
                }
                tagAttributes.add(attributeName);
            }
        }
    }

    public void init(int siteId) {
    }

    public Document parseDOM(Document doc) {
        if ( doc != null && !"".equals(serverURL) ){
            removeServerNameFromURLs(doc.getDocumentElement());
        }
        return doc;
    }

    private void removeServerNameFromURLs(Node node){

        String servletPath = "";
        int pos = serverURL.lastIndexOf("/");
        if (  pos != -1 ){
            servletPath = serverURL.substring(0,pos+1);
        }

        String serverName = "";
        int pos2 = serverURL.indexOf("//");
        if (  pos2 != -1 ){
            pos = serverURL.substring(pos2+2,serverURL.length()).indexOf("/");
            if ( pos != -1 ){
                serverName = serverURL.substring(0, pos + pos2+2);
            }
        }

        if ( node == null ){
            return;
        }
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element curElement = (Element) node;
            Set linkAttributes = (Set) linkAttributesByTagName.get(curElement.getTagName().toLowerCase());
            if (linkAttributes != null) {
                Iterator attributeIter = linkAttributes.iterator();
                while (attributeIter.hasNext()) {
                    String curLinkAttribute = (String) attributeIter.next();
                    if ( curElement.hasAttribute(curLinkAttribute) ){
                        String curLink = curElement.getAttribute(
                            curLinkAttribute);
                        if (curLink != null) {
                            logger.debug("Found link [" + curLink +
                                         "] on tag [" + curElement.getTagName() +
                                         "] with attribute [" +
                                         curLinkAttribute + "]");
                            curLink = JahiaTools.replacePattern(curLink,
                                this.serverURL, "");
                            if (!"".equals(servletPath)) {
                                curLink = JahiaTools.replacePattern(
                                    curLink, servletPath, "");
                            }
                            if (!"".equals(serverName)) {
                                curLink = JahiaTools.replacePattern(
                                    curLink, serverName, "");
                            }
                            curElement.setAttribute(curLinkAttribute, curLink);
                        }
                    }
                }
            }
        }

        NodeList childNodes = node.getChildNodes();
        for (int i=0; i < childNodes.getLength(); i++) {
            removeServerNameFromURLs(childNodes.item(i));
        }
    }
}
