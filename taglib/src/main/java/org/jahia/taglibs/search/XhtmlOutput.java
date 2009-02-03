/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
