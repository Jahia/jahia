/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
        OutputDocument document = null;

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
                            // only create output document if we determined that we need to modify it
                            if(document == null) {
                                document = new OutputDocument(source);
                            }
                            document.replace(attribute.getValueSegment(), value);
                        }
                    }
                }
            }
        }

        return document != null ? document.toString() : htmlContent;
    }
}
