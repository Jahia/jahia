/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * Traverses the configured HTML tag attributes of the provided document and
 * executes visitors to modify attribute values.
 * 
 * @author Sergiy Shyrkov
 */
public class HtmlTagAttributeTraverser {

    /**
     * Defines visitor for modifying an HTML tag attribute, found in the
     * content.
     * 
     * @author Sergiy Shyrkov
     */
    public static interface HtmlTagAttributeVisitor {

        /**
         * Applies the required modifications to the specified attribute if
         * needed.
         * 
         *
         * @param value the attribute value to be modified
         * @param context current rendering context
         * @param tagName
         * @param attrName
         * @param resource current resource  @returns the modified attribute value
         */
        String visit(String value, RenderContext context, String tagName, String attrName, Resource resource);
    }

    private Map<String, Set<String>> attributesToVisit;

    /**
     * Initializes an instance of this class.
     * 
     * @param attributesToVisit a map of tags and attributes to visit
     */
    public HtmlTagAttributeTraverser(Map<String, Set<String>> attributesToVisit) {
        super();
        this.attributesToVisit = attributesToVisit;
    }

    /**
     * Parses the provided HTML content and calls specified visitors to modify
     * the value of matching tag attributes. Returns modified content as a
     * result.
     * 
     * @param htmlContent the source content to be modified
     * @param visitors visitors to be called for matching attributes
     * @return modified content as a result
     */
    public String traverse(String htmlContent, HtmlTagAttributeVisitor... visitors) {
        return traverse(htmlContent, null, null, visitors);
    }

    /**
     * Parses the provided HTML content and calls specified visitors to modify
     * the value of matching tag attributes. Returns modified content as a
     * result.
     * 
     * @param htmlContent the source content to be modified
     * @param context current rendering context
     * @param resource current resource
     * @param visitors visitors to be called for matching attributes
     * @return modified content as a result
     */
    public String traverse(String htmlContent, RenderContext context, Resource resource,
            HtmlTagAttributeVisitor... visitors) {
        if (attributesToVisit == null || attributesToVisit.isEmpty() || visitors == null || visitors.length == 0) {
            return htmlContent;
        }

        Source source = new Source(htmlContent);
        OutputDocument document = new OutputDocument(source);

        for (Map.Entry<String, Set<String>> tag : attributesToVisit.entrySet()) {
            List<StartTag> tags = source.getAllStartTags(tag.getKey());
            for (StartTag startTag : tags) {
                final Attributes attributes = startTag.getAttributes();
                for (String attrName : tag.getValue()) {
                    Attribute attribute = attributes.get(attrName);
                    if (attribute != null) {
                        String originalValue = attribute.getValue();
                        String value = originalValue;
                        for (HtmlTagAttributeVisitor visitor : visitors) {
                            value = visitor.visit(value, context, startTag.getName(), attrName, resource);
                        }
                        if (originalValue != value && originalValue != null && !originalValue.equals(value)) {
                            document.replace(attribute.getValueSegment(), value);
                        }
                    }
                }
            }
        }

        return document.toString();
    }
}
