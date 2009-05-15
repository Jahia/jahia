package org.jahia.taglibs.template.include;

import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.jahia.services.htmlparser.HtmlDOMVisitor;
import org.jahia.registries.ServicesRegistry;
import org.jahia.bin.Jahia;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.ServletRequest;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 15, 2009
 * Time: 3:09:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileIncludeTag extends ImportSupport {
    private boolean process = true;

    public void setPath(String path) {
        ServletRequest r = pageContext.getRequest();

        String ctx;
        if ("/".equals(Jahia.getContextPath())) {
            ctx = "/files";
        } else {
            ctx = Jahia.getContextPath() + "/files";
        }

        url = r.getScheme()+"://"+ r.getServerName()+":"+ r.getServerPort()+ ctx + path;
    }

    public void setProcessHtml(boolean bool) throws JspTagException {
        this.process = bool;
    }

    @Override
    public int doStartTag() throws JspException {
        if (process) {
            setVar("internalContent");
            setScope("page");
        }

        return super.doStartTag();

    }

    @Override
    public int doEndTag() throws JspException {
        int res = super.doEndTag();

        String result = (String) pageContext.getAttribute("internalContent", PageContext.PAGE_SCOPE);

        try {
            pageContext.getOut().print(processContent(result));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return res;
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
