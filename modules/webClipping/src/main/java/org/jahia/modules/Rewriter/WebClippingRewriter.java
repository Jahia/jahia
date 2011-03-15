/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.modules.Rewriter;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.*;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * User: Dorth
 * Date: 30 d�c. 2010
 * Time: 12:07:04
 */
public final class WebClippingRewriter {
    // ------------------------------ FIELDS ------------------------------

    public static final String URL_PATH_PARAM_NAME = "jahia_url_web_clipping";
    private boolean isTrunked = false;
    private Pattern pattern = null;
    private final String regex = "((jpg)|(jpeg)|(gif)|(png)|(xls)|(doc)|(pdf))$";
    private String url;
    private URL urlProperties;
// -------------------------- STATIC METHODS --------------------------

    private static OutputDocument moveScriptInNonTrunkedHtml(OutputDocument outputDocument, String tag) {
        OutputDocument document = outputDocument;
        // Manage the javascript
        Source source = new Source(document.toString());
        List scripts = source.getAllStartTags(tag);
        StartTag body;
        for (int i = scripts.size() - 1; i >= 0; i--) {
            StartTag startTag = (StartTag) scripts.get(i);
            int begin = startTag.getBegin();
            int end;
            if (startTag.getElement().getEndTag() != null) {
                end = startTag.getElement().getEndTag().getEnd();
            } else {
                end = startTag.getEnd();
            }
            final List tags = source.getAllStartTags(HTMLElementName.BODY);
            if (tags != null && tags.size() > 0) {
                body = (StartTag) tags.get(0);
                if (end < body.getBegin()) {
                    document.insert(body.getElement().getContent().getBegin(), startTag.getElement().toString());
                    document.remove(startTag);
                    document = moveScriptInNonTrunkedHtml(new OutputDocument(new Source(document.toString())), tag);
                    break;
                }
            }
        }
        return document;
    }

    private static OutputDocument moveScriptInTrunkedHtml(Source origin, OutputDocument outputDocument,
                                                          final String text, String tag, String attributName, String attributValue) {
        OutputDocument document = outputDocument;
        // Manage the javascript
        Source source = new Source(document.toString());
        List scripts = source.getAllStartTags(text);
        for (int i = scripts.size() - 1; i >= 0; i--) {
            StartTag startTag = (StartTag) scripts.get(i);
            int begin = startTag.getBegin();
            int end;
            if (startTag.getElement().getEndTag() != null) {
                end = startTag.getElement().getEndTag().getEnd();
            } else {
                end = startTag.getEnd();
            }
            int startPos = 0;
            int endPos = 0;
            StartTag trunkTag;
            if (!"".equals(tag)) {
                List tags = source.getAllStartTags(tag);
                for (int j = 0; j < tags.size(); j++) {
                    trunkTag = (StartTag) tags.get(j);
                    if (!"".equals(attributName)) {
                        // Get the list of attributes to ensure that is the right tag
                        Attributes attributes = trunkTag.getAttributes();
                        Attribute attribute = attributes.get(attributName);
                        if (attribute != null && attribute.getValue().equalsIgnoreCase(attributValue)) {
                            startPos = trunkTag.getBegin();
                            endPos = trunkTag.getElement().getEndTag().getBegin();
                            break;
                        }
                    } else {
                        // Trunk and get only th econtent of the specified tag.
                        startPos = trunkTag.getBegin();
                        endPos = trunkTag.getElement().getEndTag().getBegin();
                        break;
                    }
                }
            }
            if (end < startPos) {
                // This script isn't enclose in the trunked tag so we must move it to ensure everything working
                StartTag body = ((StartTag) new Source(outputDocument.toString()).getAllStartTags().get(0));
                outputDocument.insert(body.getElement().getEnd(), startTag.getElement().toString());
                outputDocument.remove(startTag);
                document = moveScriptInTrunkedHtml(origin, outputDocument, text, tag, attributName, attributValue);
            } else if (begin > endPos) {
                // This script isn't enclose in the trunked tag so we must move it to ensure everything working
                EndTag body = ((StartTag) new Source(outputDocument.toString()).getAllStartTags().get(0)).getElement().getEndTag();
                outputDocument.insert(body.getElement().getBegin() - 1, startTag.getElement().toString());
                outputDocument.remove(startTag);
                document = moveScriptInTrunkedHtml(origin, outputDocument, text, tag, attributName, attributValue);
            }
        }
        return outputDocument;
    }

    // --------------------------- CONSTRUCTORS ---------------------------

    protected WebClippingRewriter() {
    }

    public WebClippingRewriter(String url) throws MalformedURLException {
        this.url = url;
        urlProperties = new URL(url);
        pattern = Pattern.compile(regex);
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * This method rewrite the url of an html document.
     *
     * @param responseBody Html content of the targeted url
     * @return outpuDocument
     * @throws java.net.MalformedURLException when we encouter a malformed URL
     */
    public OutputDocument rewriteBody(String responseBody, Resource resource, RenderContext context) throws MalformedURLException {
        long start = 0;
        Source source = new Source(responseBody);
        OutputDocument document = new OutputDocument(source);
        StringBuffer stringBuffer = new StringBuffer(responseBody.length());
        StartTag baseTag = source.getNextStartTag(0, HTMLElementName.BASE);
        if (baseTag != null) {
            final Attribute attribute = baseTag.getAttributes().get("href");
            if (attribute != null) {
                url = attribute.getValue();
                urlProperties = new URL(url);
            }
        }
        rewriteBackgroundAttribute(source, stringBuffer, document);
        rewriteFormTag(source, stringBuffer, document, context);
        rewriteATag(source, stringBuffer, document, context);
        rewriteAreaTag(source, stringBuffer, document, context);
        rewriteImgTag(source, stringBuffer, document);
        rewriteInputImageTag(source, stringBuffer, document);
        rewriteLinkTag(source, stringBuffer, document, resource);
        rewriteStyleTag(source, stringBuffer, document, resource);
        rewriteFrameTag(source, stringBuffer, document);
        rewriteStyleAttribute(source, stringBuffer, document);
        rewriteIFrameTag(source, stringBuffer, document);
        rewriteScriptTag(source, stringBuffer, document);
        rewriteObjectTag(source, stringBuffer, document);
        document = new OutputDocument(new Source(trunkDocument(document, resource).toString()));
        return document;
    }

    private String getAbsoluteURL(String hrefUrl) throws MalformedURLException {
        final String s = urlProperties.getProtocol() + "://" + urlProperties.getHost() +
                ((urlProperties.getPort() >=
                        0) ? ":" +
                        urlProperties.getPort() : "");
        int endIndex = urlProperties.getPath().lastIndexOf("/");
        if (endIndex < 0) {
            final int i = urlProperties.getPath().length();
            endIndex = (i > 0) ? i : 0;
        }
        String absoluteUrl = s + urlProperties.getPath().substring(0, endIndex) + '/' +
                hrefUrl;
        if (hrefUrl.trim().length() > 0) {
            if (hrefUrl.startsWith("//")) {
                // We have a net_path accordind to RFC_2396 definig URI
                absoluteUrl = urlProperties.getProtocol() + ":" + hrefUrl;
            } else if (hrefUrl.charAt(0) == '/') {
                // We have an aboslute url
                absoluteUrl = s + hrefUrl;
            } else if (hrefUrl.startsWith("http")) {
                URL tmp = new URL(hrefUrl);
                if (tmp.getHost().equalsIgnoreCase(urlProperties.getHost())) {
                    absoluteUrl = hrefUrl;
                }
            }
        }
        return absoluteUrl;
    }

    /**
     * Rewrite the specified url to be a webclipping url.
     *
     * @param sourceUrl The URL to be rewrited and encoded.
     * @return string
     * @throws java.net.MalformedURLException when we can not rewrite the URL.
     */
    private String getRewritedUrl(String sourceUrl, RenderContext renderContext)
            throws MalformedURLException {
        try {
            return renderContext.getURLGenerator().getContext() + renderContext.getURLGenerator().getCurrent() + "?" + WebClippingRewriter.URL_PATH_PARAM_NAME + "=" + URLEncoder.encode(getAbsoluteURL(sourceUrl), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    private OutputDocument getTrunkedDocument(String tag, String attributeName, String attributeValue,
                                              String content) {
        OutputDocument outputDocument = new OutputDocument(new Source(content));
        Source origin = new Source(content);
        StartTag startTag;
        if (!"".equals(tag)) {
            // Get the list of the specified tag
            List tags = origin.getAllStartTags(tag.toLowerCase());
            for (int i = 0; i < tags.size(); i++) {
                startTag = (StartTag) tags.get(i);
                if (!"".equals(attributeName)) {
                    // Get the list of attributes to ensure that is the right tag
                    Attributes attributes = startTag.getAttributes();
                    Attribute attribute = attributes.get(attributeName);
                    if (attribute != null && attribute.getValue().equalsIgnoreCase(attributeValue)) {
                        outputDocument = trunk(content, startTag);
                        break;
                    }
                } else {
                    // Trunk and get only th econtent of the specified tag.
                    outputDocument = trunk(content, startTag);
                    break;
                }
            }
        }
        outputDocument = new OutputDocument(new Source(outputDocument.toString()));
        if (!isTrunked) {
            outputDocument = WebClippingRewriter.moveScriptInNonTrunkedHtml(outputDocument, HTMLElementName.SCRIPT);
            outputDocument = WebClippingRewriter.moveScriptInNonTrunkedHtml(outputDocument, HTMLElementName.LINK);
            outputDocument = WebClippingRewriter.moveScriptInNonTrunkedHtml(outputDocument, HTMLElementName.STYLE);
        } else {
            outputDocument = WebClippingRewriter.moveScriptInTrunkedHtml(origin, outputDocument, HTMLElementName.SCRIPT, tag, attributeName, attributeValue);
            outputDocument = WebClippingRewriter.moveScriptInTrunkedHtml(origin, outputDocument, HTMLElementName.LINK,  tag, attributeName, attributeValue);
            outputDocument = WebClippingRewriter.moveScriptInTrunkedHtml(origin, outputDocument, HTMLElementName.STYLE,  tag, attributeName, attributeValue);
        }
        Source source = new Source(outputDocument.toString());
        OutputDocument document = new OutputDocument(source);
        List tags = source.getAllStartTags(HTMLElementName.BODY);
        if (tags != null && tags.size() > 0) {
            StartTag body = (StartTag) tags.get(0);
            Attributes attributes = body.getAttributes();
            if (attributes != null && attributes.size() > 0) {
                Attribute attribute = attributes.get("onload");
                if (attribute != null) {
                    String value = attribute.getValue();
                    EndTag endTag = body.getElement().getEndTag();
                    if (endTag != null) {
                        int start = endTag.getBegin();
                        document.replace(start, start, "<script>" + value + "</script>");
                    }
                }
            }
        }
        final String s = document.toString();
        source = new Source(s);
        tags = source.getAllStartTags(HTMLElementName.BODY);
        StartTag body;
        if (tags != null && tags.size() > 0)
            body = (StartTag) tags.get(0);
        else body = null;
        return trunk(s, body);
    }

    private void rewriteATag(Source source, StringBuffer stringBuffer, OutputDocument document, RenderContext context) {
        List aStartTag = source.getAllStartTags(HTMLElementName.A);
        for (int i = 0; i < aStartTag.size(); i++) {
            try {
                StartTag startTag = (StartTag) aStartTag.get(i);
                Attributes attributes = startTag.getAttributes();
                Attribute href = attributes.get("href");
                if (href != null && href.getValue().length() > 0) {
                    String hrefUrl = href.getValue().trim();
                    stringBuffer.setLength(0);
                    if (!hrefUrl.toLowerCase().startsWith("http") && !hrefUrl.toLowerCase().startsWith("javascript")
                            && !hrefUrl.toLowerCase().startsWith("mailto") && !hrefUrl.toLowerCase().startsWith("ftp")
                            && !hrefUrl.toLowerCase().startsWith("news") && !hrefUrl.toLowerCase().startsWith("wais")
                            && !hrefUrl.toLowerCase().startsWith("gopher") && !hrefUrl.startsWith("#")) {
                        Matcher matcher = pattern.matcher(hrefUrl);
                        if (!matcher.find()) {
                            String rewritedUrl = getRewritedUrl(hrefUrl, context);
                            final Attribute target = attributes.get("target");
                            if (target != null && !target.getValue().equals("_self") && !target.getValue().equals("_parent")) {
                                rewritedUrl = getAbsoluteURL(hrefUrl);
                            }
                            stringBuffer.append("<a href=\"").append(rewritedUrl).append("\" ");
                            Iterator atList = attributes.iterator();
                            while (atList.hasNext()) {
                                Attribute attribute = (Attribute) atList.next();
                                if (!"href".equalsIgnoreCase(attribute.getName())) {
                                    stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                                }
                            }
                        } else {
                            stringBuffer.append("<a target=\"new\" href=\"").append(getAbsoluteURL(hrefUrl)).append("\" ");
                            Iterator atList = attributes.iterator();
                            while (atList.hasNext()) {
                                Attribute attribute = (Attribute) atList.next();
                                if (!"href".equalsIgnoreCase(attribute.getKey()) &&
                                        !"target".equalsIgnoreCase(attribute.getKey())) {
                                    stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                                }
                            }
                        }
                        stringBuffer.append('>');
                        document.replace(startTag, stringBuffer.toString());
                    } else if (hrefUrl.toLowerCase().startsWith("http")) {
                        Matcher matcher = pattern.matcher(hrefUrl);
                        URL tmp = new URL(hrefUrl);
                        if (!matcher.find() && tmp.getHost().equalsIgnoreCase(urlProperties.getHost())) {
                            stringBuffer.append("<a href=\"").append(getRewritedUrl(hrefUrl, context)).append("\" ");
                            Iterator atList = attributes.iterator();
                            while (atList.hasNext()) {
                                Attribute attribute = (Attribute) atList.next();
                                if (!"href".equalsIgnoreCase(attribute.getName())) {
                                    stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                                }
                            }
                            stringBuffer.append('>');
                            document.replace(startTag, stringBuffer.toString());
                        } else {
                            stringBuffer.append("<a target=\"new\" ");
                            Iterator atList = attributes.iterator();
                            while (atList.hasNext()) {
                                Attribute attribute = (Attribute) atList.next();
                                if (!"target".equalsIgnoreCase(attribute.getName())) {
                                    stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                                }
                            }
                            stringBuffer.append('>');
                            document.replace(startTag, stringBuffer.toString());
                        }
                    }
                }
            } catch (MalformedURLException e) {

                //log.warn("We detect a malformed url in an HREF attribute of an A tag", e);
            }
        }
    }

    private void rewriteAreaTag(Source source, StringBuffer stringBuffer, OutputDocument document, RenderContext context) throws MalformedURLException {
        List aStartTag = source.getAllStartTags(HTMLElementName.AREA);
        for (int i = 0; i < aStartTag.size(); i++) {
            StartTag startTag = (StartTag) aStartTag.get(i);
            Attributes attributes = startTag.getAttributes();
            Attribute href = attributes.get("href");
            if (href != null && href.getValue().length() > 0) {
                String hrefUrl = href.getValue().trim();
                stringBuffer.setLength(0);
                if (!hrefUrl.toLowerCase().startsWith("http") && !hrefUrl.toLowerCase().startsWith("javascript")
                        && !hrefUrl.toLowerCase().startsWith("mailto") && !hrefUrl.toLowerCase().startsWith("ftp")
                        && !hrefUrl.toLowerCase().startsWith("news") && !hrefUrl.toLowerCase().startsWith("wais")
                        && !hrefUrl.toLowerCase().startsWith("gopher")) {
                    Matcher matcher = pattern.matcher(hrefUrl);
                    if (!matcher.find()) {
                        String rewritedUrl = getRewritedUrl(hrefUrl.replaceAll("\\?", "&"), context);
                        final Attribute target = attributes.get("target");
                        if (target != null && !target.getValue().equals("_self")) {
                            rewritedUrl = getAbsoluteURL(hrefUrl);
                        }
                        stringBuffer.append("<area href=\"").append(rewritedUrl).append("\" ");
                        Iterator atList = attributes.iterator();
                        while (atList.hasNext()) {
                            Attribute attribute = (Attribute) atList.next();
                            if (!"href".equalsIgnoreCase(attribute.getName())) {
                                stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                            }
                        }
                    } else {
                        stringBuffer.append("<area target=\"new\" href=\"").append(getAbsoluteURL(hrefUrl)).append("\" ");
                        Iterator atList = attributes.iterator();
                        while (atList.hasNext()) {
                            Attribute attribute = (Attribute) atList.next();
                            if (!"href".equalsIgnoreCase(attribute.getKey()) &&
                                    !"target".equalsIgnoreCase(attribute.getKey())) {
                                stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                            }
                        }
                    }
                    stringBuffer.append('>');
                    document.replace(startTag, stringBuffer.toString());
                } else if (hrefUrl.toLowerCase().startsWith("http")) {
                    Matcher matcher = pattern.matcher(hrefUrl);
                    URL tmp = new URL(hrefUrl);
                    if (!matcher.find() && tmp.getHost().equalsIgnoreCase(urlProperties.getHost())) {
                        stringBuffer.append("<area href=\"").append(getRewritedUrl(hrefUrl, context)).append("\" ");
                        Iterator atList = attributes.iterator();
                        while (atList.hasNext()) {
                            Attribute attribute = (Attribute) atList.next();
                            if (!"href".equalsIgnoreCase(attribute.getName())) {
                                stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                            }
                        }
                        stringBuffer.append('>');
                        document.replace(startTag, stringBuffer.toString());
                    } else {
                        stringBuffer.append("<area target=\"new\" ");
                        Iterator atList = attributes.iterator();
                        while (atList.hasNext()) {
                            Attribute attribute = (Attribute) atList.next();
                            if (!"target".equalsIgnoreCase(attribute.getName())) {
                                stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                            }
                        }
                        stringBuffer.append('>');
                        document.replace(startTag, stringBuffer.toString());
                    }
                }
            }
        }
    }

    private void rewriteBackgroundAttribute(Source source, StringBuffer stringBuffer, OutputDocument document)
            throws MalformedURLException {
        List aStartTag = source.getAllStartTags();
        for (int i = 0; i < aStartTag.size(); i++) {
            StartTag startTag = (StartTag) aStartTag.get(i);
            Attributes attributes = startTag.getAttributes();
            if (attributes != null && attributes.size() > 0) {
                Attribute href = attributes.get("background");
                if (href != null && href.getValue().length() > 0) {
                    String hrefUrl = href.getValue().trim();
                    stringBuffer.setLength(0);
                    if (!hrefUrl.startsWith("http")) {
                        stringBuffer.append("<").append(startTag.getName()).append(" background=\"").append(getAbsoluteURL(hrefUrl)).append("\" ");
                        Iterator atList = attributes.iterator();
                        while (atList.hasNext()) {
                            Attribute attribute = (Attribute) atList.next();
                            if (!"background".equalsIgnoreCase(attribute.getName())) {
                                stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                            }
                        }
                        stringBuffer.append('>');
                        document.replace(startTag, stringBuffer.toString());
                    }
                }
            }
        }
    }

    private void rewriteFormTag(Source source, StringBuffer stringBuffer, OutputDocument document, RenderContext context) throws MalformedURLException {
        List formStartTag = source.getAllStartTags(HTMLElementName.FORM);
        for (int i = 0; i < formStartTag.size(); i++) {
            StartTag startTag = (StartTag) formStartTag.get(i);
            Attributes attributes = startTag.getAttributes();
            Attribute action = attributes.get("action");
            if (action != null) {
                String actionUrl = action.getValue();
                stringBuffer.setLength(0);
                stringBuffer.append("<form action=\"").append(getRewritedUrl(actionUrl, context)).append("\" ");
                Iterator atList = attributes.iterator();
                while (atList.hasNext()) {
                    Attribute attribute = (Attribute) atList.next();
                    final String name = attribute.getName();
                    if (!"action".equalsIgnoreCase(name) && !"method".equalsIgnoreCase(name)) {
                        stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                    }
                }
                Attribute method = attributes.get("method");
                stringBuffer.append("method=\"get\">\n");
                stringBuffer.append("<input type=\"hidden\" name=\"original_method\" value=\"").append((method != null) ? method.getValue() : "get").append("\">\n");
                stringBuffer.append("<input type=\"hidden\" name=\""+WebClippingRewriter.URL_PATH_PARAM_NAME+"\" value=\"").append(getAbsoluteURL(actionUrl)).append("\">");
                document.replace(startTag, stringBuffer.toString());
            }
        }
    }

    private void rewriteFrameTag(Source source, StringBuffer stringBuffer, OutputDocument document)
            throws MalformedURLException {
        List aStartTag = source.getAllStartTags(HTMLElementName.FRAME);
        for (int i = 0; i < aStartTag.size(); i++) {
            StartTag startTag = (StartTag) aStartTag.get(i);
            Attributes attributes = startTag.getAttributes();
            Attribute href = attributes.get("src");
            if (href != null) {
                String hrefUrl = href.getValue().trim();
                stringBuffer.setLength(0);
                if (!hrefUrl.startsWith("http")) {
                    stringBuffer.append("<frame src=\"").append(getAbsoluteURL(hrefUrl)).append("\" ");
                    Iterator atList = attributes.iterator();
                    while (atList.hasNext()) {
                        Attribute attribute = (Attribute) atList.next();
                        if (!"src".equalsIgnoreCase(attribute.getName())) {
                            stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                        }
                    }
                    stringBuffer.append('>');
                    document.replace(startTag, stringBuffer.toString());
                }
            }
        }
    }

    private void rewriteIFrameTag(Source source, StringBuffer stringBuffer, OutputDocument document)
            throws MalformedURLException {
        List aStartTag = source.getAllStartTags(HTMLElementName.IFRAME);
        for (int i = 0; i < aStartTag.size(); i++) {
            StartTag startTag = (StartTag) aStartTag.get(i);
            Attributes attributes = startTag.getAttributes();
            Attribute href = attributes.get("src");
            if (href != null) {
                String hrefUrl = href.getValue().trim();
                stringBuffer.setLength(0);
                if (!hrefUrl.startsWith("http")) {
                    stringBuffer.append("<iframe src=\"").append(getAbsoluteURL(hrefUrl)).append("\" ");
                    Iterator atList = attributes.iterator();
                    while (atList.hasNext()) {
                        Attribute attribute = (Attribute) atList.next();
                        if (!"src".equalsIgnoreCase(attribute.getName())) {
                            stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                        }
                    }
                    stringBuffer.append('>');
                    document.replace(startTag, stringBuffer.toString());
                }
            }
        }
    }

    private void rewriteImgTag(Source source, StringBuffer stringBuffer, OutputDocument document)
            throws MalformedURLException {
        List aStartTag = source.getAllStartTags(HTMLElementName.IMG);
        for (int i = 0; i < aStartTag.size(); i++) {
            StartTag startTag = (StartTag) aStartTag.get(i);
            Attributes attributes = startTag.getAttributes();
            Attribute href = attributes.get("src");
            if (href != null) {
                String hrefUrl = href.getValue().trim();
                stringBuffer.setLength(0);
                if (!hrefUrl.startsWith("http")) {
                    stringBuffer.append("<img src=\"").append(getAbsoluteURL(hrefUrl)).append("\" ");
                    Iterator atList = attributes.iterator();
                    while (atList.hasNext()) {
                        Attribute attribute = (Attribute) atList.next();
                        if (!"src".equalsIgnoreCase(attribute.getName())) {
                            stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                        }
                    }
                    stringBuffer.append('>');
                    document.replace(startTag, stringBuffer.toString());
                }
            }
        }
    }

    private void rewriteStyleAttribute(Source source, StringBuffer stringBuffer, OutputDocument document)
            throws MalformedURLException {
        List aStartTag = source.getAllStartTags();
        for (Object tag : aStartTag) {
            StartTag startTag = (StartTag) tag;
            Attributes attributes = startTag.getAttributes();
            if (attributes != null && attributes.size() > 0) {
                Attribute style = attributes.get("style");
                if (style != null && style.length() > 0 && style.getValue().trim().contains("url")) {
                    String styleContent = style.getValue().trim();
                    int start = styleContent.indexOf("url");
                    char c = ')';
                    int j = start;
                    while (styleContent.charAt(j) != c) {
                        j++;
                    }
                    String subUrl = styleContent.substring(start, j + 1);
                    stringBuffer.setLength(0);
                    stringBuffer.append(startTag.toString());
                    try {
                        String urlToAbsolute = subUrl.substring(subUrl.indexOf("(") + 1, subUrl.indexOf(")"));
                        if (!urlToAbsolute.startsWith("http")) {
                            URL newUrl = new URL(getAbsoluteURL(urlToAbsolute));
                            stringBuffer.replace(stringBuffer.indexOf("url(") + 4, stringBuffer.indexOf(")"), newUrl.toString());
                            document.replace(startTag, stringBuffer.toString());
                        } else {
                            return;
                        }
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        }
    }

    private void rewriteObjectTag(Source source, StringBuffer stringBuffer, OutputDocument document)
            throws MalformedURLException {
        List aStartTag = source.getAllStartTags(HTMLElementName.PARAM);
        for (int i = 0; i < aStartTag.size(); i++) {
            StartTag startTag = (StartTag) aStartTag.get(i);
            Attributes attributes = startTag.getAttributes();
            Attribute href = attributes.get("name");
            if (href != null && href.getValue().equalsIgnoreCase("src")) {
                href = attributes.get("value");
                if (href != null) {
                    String hrefUrl = href.getValue().trim();
                    stringBuffer.setLength(0);
                    if (!hrefUrl.startsWith("http")) {
                        stringBuffer.append("<param value=\"").append(getAbsoluteURL(hrefUrl)).append("\" ");
                        Iterator atList = attributes.iterator();
                        while (atList.hasNext()) {
                            Attribute attribute = (Attribute) atList.next();
                            if (!"value".equalsIgnoreCase(attribute.getName())) {
                                stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                            }
                        }
                        stringBuffer.append('>');
                        document.replace(startTag, stringBuffer.toString());
                    }
                }
            }
        }
    }

    private void rewriteInputImageTag(Source source, StringBuffer stringBuffer, OutputDocument document)
            throws MalformedURLException {
        List aStartTag = source.getAllStartTags(HTMLElementName.INPUT);
        for (int i = 0; i < aStartTag.size(); i++) {
            StartTag startTag = (StartTag) aStartTag.get(i);
            Attributes attributes = startTag.getAttributes();
            Attribute href = attributes.get("src");
            Attribute type = attributes.get("type");
            if (type != null && "image".equalsIgnoreCase(type.getValue()) && href != null) {
                String hrefUrl = href.getValue().trim();
                stringBuffer.setLength(0);
                if (!hrefUrl.startsWith("http")) {
                    stringBuffer.append("<input src=\"").append(getAbsoluteURL(hrefUrl)).append("\" ");
                    Iterator atList = attributes.iterator();
                    while (atList.hasNext()) {
                        Attribute attribute = (Attribute) atList.next();
                        if (!"src".equalsIgnoreCase(attribute.getName())) {
                            stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                        }
                    }
                    stringBuffer.append('>');
                    document.replace(startTag, stringBuffer.toString());
                }
            }
        }
    }

    private void rewriteLinkTag(Source source, StringBuffer stringBuffer, OutputDocument document, Resource resource)
            throws MalformedURLException {
        boolean includeCss = Boolean.valueOf(resource.getNode().getPropertyAsString("includeCss"));
        List aStartTag = source.getAllStartTags(HTMLElementName.LINK);
        for (int i = 0; i < aStartTag.size(); i++) {
            StartTag startTag = (StartTag) aStartTag.get(i);
            Attributes attributes = startTag.getAttributes();
            Attribute href = attributes.get("href");
            if (href != null && (includeCss || (!includeCss && !"stylesheet".equals(attributes.get("rel").getValue().toLowerCase())))) {
                String hrefUrl = href.getValue().trim();
                stringBuffer.setLength(0);
                if (!hrefUrl.startsWith("http")) {
                    stringBuffer.append("<link href=\"").append(getAbsoluteURL(hrefUrl)).append("\" ");
                    Iterator atList = attributes.iterator();
                    while (atList.hasNext()) {
                        Attribute attribute = (Attribute) atList.next();
                        if (!"href".equalsIgnoreCase(attribute.getName())) {
                            stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                        }
                    }
                    stringBuffer.append('>');
                    document.replace(startTag, stringBuffer.toString());
                }
            } else {
                document.replace(startTag, "");
            }
        }
    }

    private void rewriteStyleTag(Source source, StringBuffer stringBuffer, OutputDocument document, Resource resource)
            throws MalformedURLException {
        boolean includeCss = Boolean.valueOf(resource.getNode().getPropertyAsString("includeCss"));
        List aStartTag = source.getAllStartTags(HTMLElementName.STYLE);
        for (int i = 0; i < aStartTag.size(); i++) {
            StartTag startTag = (StartTag) aStartTag.get(i);
            if (!includeCss) {
                document.replace(startTag, "");
            }
        }
    }

    private void rewriteScriptTag(Source source, StringBuffer stringBuffer, OutputDocument document)
            throws MalformedURLException {
        List aStartTag = source.getAllStartTags(HTMLElementName.SCRIPT);
        for (int i = 0; i < aStartTag.size(); i++) {
            StartTag startTag = (StartTag) aStartTag.get(i);
            Attributes attributes = startTag.getAttributes();
            Attribute href = attributes.get("src");
            if (href != null) {
                String hrefUrl = href.getValue().trim();
                stringBuffer.setLength(0);
                if (!hrefUrl.startsWith("http")) {
                    stringBuffer.append("<script src=\"").append(getAbsoluteURL(hrefUrl)).append("\" ");
                    Iterator atList = attributes.iterator();
                    while (atList.hasNext()) {
                        Attribute attribute = (Attribute) atList.next();
                        if (!"src".equalsIgnoreCase(attribute.getName())) {
                            stringBuffer.append(attribute.getName()).append("=").append(attribute.getQuoteChar()).append(attribute.getValue()).append(attribute.getQuoteChar()).append(' ');
                        }
                    }
                    stringBuffer.append('>');
                    document.replace(startTag, stringBuffer.toString());
                }
            }
        }
    }

    private OutputDocument trunk(String document, StartTag startTag) {
        StringBuffer buffer = new StringBuffer(document.length());
        if (startTag != null) {
            buffer.append(startTag.getElement().toString());
            isTrunked = true;
        } else {
            buffer.append(document);
        }
        OutputDocument outputDocument = new OutputDocument(new Source(buffer.toString()));
        return outputDocument;
    }

    /**
     * Trunk the document if we have to.
     * Get the content of a specified tag.
     *
     * @param document The document to be trunked if needed
     * @param resource The current resource
     * @return ouputDocument
     */
    private OutputDocument trunkDocument(OutputDocument document, Resource resource) {
        // Get the specified tag
        String tag = "";
        if (resource.getNode().getPropertyAsString("specificTag") != null) {
            tag = resource.getNode().getPropertyAsString("specificTag");
        }
        // Get the specific attribute name
        String attributeName = "";
        if (resource.getNode().getPropertyAsString("specificAttr") != null) {
            attributeName = resource.getNode().getPropertyAsString("specificAttr");
        }
        // Get the specific attribute value
        String attributeValue = "";
        if (resource.getNode().getPropertyAsString("specificAttrValue") != null) {
            attributeValue = resource.getNode().getPropertyAsString("specificAttrValue");
        }
        OutputDocument outputDocument = document;
        outputDocument = getTrunkedDocument(tag, attributeName, attributeValue, outputDocument.toString());
        return outputDocument;
    }
}
