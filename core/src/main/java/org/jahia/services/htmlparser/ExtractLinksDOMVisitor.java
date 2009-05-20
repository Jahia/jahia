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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>Title: HTML DOM Visitor that extracts all the links in the document
 * based on a set of tags specified in the W3C HTML specification</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ExtractLinksDOMVisitor implements HtmlDOMVisitor {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ExtractLinksDOMVisitor.class);

    private List documentLinks = new ArrayList();

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

    public ExtractLinksDOMVisitor() {
        for (int i = 0; i < tagAndAttributesWithLinks.length; i++) {
            String tagName = tagAndAttributesWithLinks[i][0];
            String attributeName = tagAndAttributesWithLinks[i][1];
            Set tagAttributes = null;
            if (!linkAttributesByTagName.containsKey(tagName)) {
                tagAttributes = new HashSet();
                linkAttributesByTagName.put(tagName, tagAttributes);
            } else {
                tagAttributes = (Set) linkAttributesByTagName.get(tagName);
            }
            tagAttributes.add(attributeName);
        }
    }

    public void init(int siteId) {
    }

    public Document parseDOM(Document doc) {
        if ( doc != null ){
            extractNodeLinks(doc.getDocumentElement());
        }
        return doc;
    }

    private void extractNodeLinks(Node node){

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
                    String curLink = curElement.getAttribute(curLinkAttribute);
                    if (curLink != null) {
                        logger.debug("Found link [" + curLink + "] on tag [" + curElement.getTagName() + "] with attribute [" + curLinkAttribute + "]");
                        documentLinks.add(curLink);
                    }
                }
            }
        }

        NodeList childNodes = node.getChildNodes();
        for (int i=0; i < childNodes.getLength(); i++) {
            extractNodeLinks(childNodes.item(i));
        }
    }

    /**
     * @return an List of String objects that contain all the links
     * in the HTML DOM that we parsed.
     */
    public List getDocumentLinks() {
        return documentLinks;
    }

}