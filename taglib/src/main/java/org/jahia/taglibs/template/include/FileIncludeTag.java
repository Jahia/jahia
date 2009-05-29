/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.template.include;

import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.jahia.bin.Jahia;
import org.jahia.engines.filemanager.URLUtil;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.htmlparser.HtmlDOMVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 15, 2009
 * Time: 3:09:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileIncludeTag extends ImportSupport {
    private boolean process = true;
    private boolean valid = false;
    public void setPath(String path) {
        ServletRequest r = pageContext.getRequest();

        if (path != null && path.length() > 0) {
            valid = true;
        }

        String ctx;
        if ("/".equals(Jahia.getContextPath())) {
            ctx = "/files";
        } else {
            ctx = Jahia.getContextPath() + "/files";
        }

        url = r.getScheme()+"://"+ r.getServerName()+":"+ r.getServerPort() + URLUtil.URLEncode( ctx + path, "UTF-8");
    }

    public void setProcessHtml(boolean bool) throws JspTagException {
        this.process = bool;
    }

    @Override
    public int doStartTag() throws JspException {
        if (valid) {
            if (process) {
                setVar("internalContent");
                setScope("page");
            }

            return super.doStartTag();
        } else {
            return EVAL_PAGE;
        }

    }

    @Override
    public int doEndTag() throws JspException {
        if (valid) {
            int res = super.doEndTag();

            String result = (String) pageContext.getAttribute("internalContent", PageContext.PAGE_SCOPE);

            try {
                pageContext.getOut().print(processContent(result));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return res;
        } else {
            return EVAL_PAGE;
        }
    }

    private String processContent(String html) {

        String result = "";
        try {
            Vector DOMVisitors = new Vector();
            DOMVisitors.add(new WordVisitor(new URL(url)));
            result = ServicesRegistry.getInstance().getHtmlParserService().parse(html, DOMVisitors);
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }


    class WordVisitor implements HtmlDOMVisitor {
        private URL baseUrl;
        private String stringurl;
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

        public WordVisitor(URL baseUrl) {
            this.baseUrl = baseUrl;
            this.stringurl = baseUrl.toString().substring(0,baseUrl.toString().lastIndexOf('/')) + "/" ;

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
            transformLink(doc.getDocumentElement());
            prepareStyles(doc.getDocumentElement());
            addTagStyle(doc.getDocumentElement());
            return doc;
        }


        private void transformLink(Node node){

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
                        if (curLink != null && curLink.length()>0) {
                            if (!curLink.startsWith("/") && (curLink.indexOf("://")==-1) && !curLink.startsWith("#")) {
                                curElement.setAttribute(curLinkAttribute, stringurl + curLink);
                            }
                        }
                    }
                }
            }

            NodeList childNodes = node.getChildNodes();
            for (int i=0; i < childNodes.getLength(); i++) {
                transformLink(childNodes.item(i));
            }
        }

        private ArrayList styledTags = new ArrayList();

        private String VALID_CHAR = ".-0123456789:@abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        private void prepareStyles(Node node){
            if ( node == null ){
                return;
            }

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("style".equalsIgnoreCase(node.getNodeName())) {
                    NodeList childNodes = node.getChildNodes();
                    for (int i=0; i < childNodes.getLength(); i++) {
                        if (childNodes.item(i).getNodeType() == Node.CDATA_SECTION_NODE || childNodes.item(i).getNodeType() == Node.TEXT_NODE || childNodes.item(i).getNodeType() == Node.COMMENT_NODE) {
                            String style = childNodes.item(i).getNodeValue();
                            StringBuffer blankedStyle = new StringBuffer(style);
                            StringBuffer modifiedStyle = new StringBuffer(style);

                            int index = 0;
                            while ((index = blankedStyle.indexOf("/*",index+1)) != -1) {
                                int index2 = blankedStyle.indexOf("*/",index+1);
                                for (int j=index; j<index2+2; j++) {
                                    blankedStyle.setCharAt(j,' ');
                                }
                            }
                            index = 0;
                            while ((index = blankedStyle.indexOf("{",index+1)) != -1) {
                                int index2 = blankedStyle.indexOf("}",index+1);
                                for (int j=index; j<index2+1; j++) {
                                    blankedStyle.setCharAt(j,' ');
                                }
                            }
                            childNodes.item(i).setNodeValue(blankedStyle.toString());
                            index = blankedStyle.length();
                            while (index > 0) {
                                index --;
                                if (VALID_CHAR.indexOf(blankedStyle.charAt(index))>-1) {
                                    int index2 = index - 1;
                                    while (index2 >=0 && VALID_CHAR.indexOf(blankedStyle.charAt(index2))>-1) {
                                        index2--;
                                    }
                                    index2++;
                                    int index3 = index2;
                                    String styleName = blankedStyle.substring(index2,index+1);
                                    if (styleName.startsWith("--")) {
                                        styleName = styleName.substring(2);
                                        index3 += 2;
                                    }
                                    if (styleName.indexOf('.')==-1 && !styleName.startsWith("@") && styleName.length()>0) {
                                        String tagName;
                                        if (styleName.indexOf(':') > -1) {
                                            tagName = styleName.substring(0,styleName.indexOf(':'));
                                        } else {
                                            tagName = styleName;
                                        }
                                        modifiedStyle.insert(index3 + tagName.length(),".word");
                                        styledTags.add(tagName);
                                    }
                                    index = index2 -1;
                                }
                            }
                            childNodes.item(i).setNodeValue(modifiedStyle.toString());
                        }
                    }
                }
            }

            NodeList childNodes = node.getChildNodes();
            for (int i=0; i < childNodes.getLength(); i++) {
                prepareStyles(childNodes.item(i));
            }
        }

        private void addTagStyle(Node node){

            if ( node == null ){
                return;
            }
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element curElement = (Element) node;
                if (styledTags.contains(node.getNodeName())) {
                    if (curElement.getAttribute("class").equals("")) {
                        curElement.setAttribute("class","word");
                    }
                }
            }

            NodeList childNodes = node.getChildNodes();
            for (int i=0; i < childNodes.getLength(); i++) {
                addTagStyle(childNodes.item(i));
            }
        }
    }
}
