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
package org.jahia.taglibs.search;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Helper class for writing XHTML content.
 * 
 * @author Sergiy Shyrkov
 */
class XhtmlOutput {

    private ArrayStack elements = new ArrayStack();

    private ArrayStack emptyElementInfos = new ArrayStack();

    private boolean opened;

    private Writer out;

    /**
     * Initializes an instance of this class.
     * 
     * @param out
     *            the JSP writer
     */
    public XhtmlOutput(Writer out) {
        super();
        this.out = out;
    }

    public XhtmlOutput attr(String name, String value) throws IOException {
        return attr(name, value, false);
    }

    public XhtmlOutput attr(String name, String value, boolean escapeXml)
            throws IOException {
        if (!opened) {
            throw new IllegalStateException("No opened element found");
        }
        out.append(" ").append(name).append("=\"").append(
                escapeXml ? StringEscapeUtils.escapeXml(value) : value).append(
                "\"");

        return this;
    }

    public void check() {
        if (elements.size() > 0) {
            throw new IllegalStateException("Not all elements were closed: "
                    + elements);
        }
    }

    public XhtmlOutput elem(String elementName) throws IOException {
        return elem(elementName, false);
    }

    private XhtmlOutput elem(String elementName, boolean isEmpty)
            throws IOException {
        if (opened) {
            out.append(">");
        }
        out.append("<").append(elementName);
        opened = true;
        elements.push(elementName);
        emptyElementInfos.push(isEmpty ? Boolean.TRUE : Boolean.FALSE);

        return this;
    }

    public XhtmlOutput emptyElem(String elementName) throws IOException {
        return elem(elementName, true);
    }

    public XhtmlOutput end() throws IOException {
        if (elements.isEmpty()) {
            throw new IllegalStateException("No openening element found");
        }
        boolean isEmpty = (Boolean) emptyElementInfos.pop();
        String elem = (String) elements.pop();

        if (opened) {
            out.append(isEmpty ? " />" : ">");
            opened = false;
        } else if (isEmpty) {
            throw new IllegalStateException("No opened element found");
        }
        if (!isEmpty) {
            out.append("</").append(elem).append(">");
        }

        return this;
    }

    public XhtmlOutput flush() throws IOException {
        if (elements.isEmpty()) {
            return this;
        }
        boolean isEmpty = (Boolean) emptyElementInfos.peek();

        if (opened && !isEmpty) {
            out.append(">");
            opened = false;
        }        return this;
    }

    public XhtmlOutput text(String text) throws IOException {
        return text(text, false);
    }

    public XhtmlOutput text(String text, boolean escapeXml) throws IOException {
        if (opened) {
            out.append(">");
            opened = false;
        }
        out.append(escapeXml ? StringEscapeUtils.escapeXml(text) : text);

        return this;
    }
}
