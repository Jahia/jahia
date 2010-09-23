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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.render.filter;

import net.htmlparser.jericho.*;
import org.apache.log4j.Logger;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 23 sept. 2010
 */
public class URLSystemAttributesAppenderFilter extends AbstractFilter {
    private transient static Logger logger = Logger.getLogger(URLSystemAttributesAppenderFilter.class);
    private List<String> attributesToKeep;
    private Map<String, Set<String>> tagsToCheck;

    public void setAttributesToKeep(List<String> attributesToKeep) {
        this.attributesToKeep = attributesToKeep;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (!Collections.disjoint(attributesToKeep, renderContext.getRequest().getParameterMap().keySet())) {
            Source source = new Source(previousOut);
            OutputDocument document = new OutputDocument(source);
            for (Map.Entry<String, Set<String>> entry : tagsToCheck.entrySet()) {
                List<StartTag> tags = source.getAllStartTags(entry.getKey());
                for (StartTag tag : tags) {
                    Attributes attributes = tag.getAttributes();
                    for (String attributeName : entry.getValue()) {
                        Attribute attribute = attributes.get(attributeName);
                        if (attribute != null) {
                            String value = attribute.getValue();
                            String oldValue = value;
                            String separateChar;
                            if (value.contains("?")) {
                                separateChar = "&";
                            } else {
                                separateChar = "?";
                            }
                            for (String s : attributesToKeep) {
                                if (!value.contains(s + "=") && value.startsWith(renderContext.getURLGenerator().getContext())) {
                                    String parameter = renderContext.getRequest().getParameter(s);
                                    if (parameter != null) {
                                        value += separateChar + s + "=" + parameter;
                                        separateChar = "&";
                                    }
                                }
                            }
                            if (!oldValue.equals(value)) {
                                document.replace(attribute.getValueSegment(), value);
                            }
                        }
                    }
                }
            }
            return document.toString();
        }
        return previousOut;
    }

    public void setTagsToCheck(Map<String, Set<String>> tagsToCheck) {
        this.tagsToCheck = tagsToCheck;
    }
}
