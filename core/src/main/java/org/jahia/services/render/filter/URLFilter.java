/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Traverses the content and searches for URLs in the configured elements.
 * Executes the list of configured visitors to modify the URL value.
 */
public class URLFilter extends AbstractFilter {
    private final static String TEMP_START_TAG = "<!-- jahia:temp value=\"URLParserStart";
    private final static String TEMP_FULL_START_TAG = TEMP_START_TAG + "XXXXXXXX\" -->";
    private final static String TEMP_END_TAG = "<!-- jahia:temp value=\"URLParserEnd";
    private final static String TEMP_CLOSING = "\" -->";
    private final static String REPLACED_START_TAG = "<jahia:URLParserParsedReplaced id=\"";

    /**
     * Initializes an instance of this class.
     *
     * @param urlTraverser the URL utility class to visit HTML tag attributes
     */
    public URLFilter(HtmlTagAttributeTraverser urlTraverser) {
        super();
        this.urlTraverser = urlTraverser;
    }

    private HtmlTagAttributeTraverser urlTraverser;

    private HtmlTagAttributeVisitor[] handlers;

    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (handlers != null && handlers.length > 0) {
            final String thisuuid = StringUtils.leftPad(Integer.toHexString(resource.hashCode()),8,"0");
            Map<String, String> alreadyParsedFragmentsMap = new HashMap<>();
            StringBuilder sb = null;
            int i;
            if (previousOut.indexOf(TEMP_START_TAG) > -1) {
                sb = new StringBuilder(previousOut);

                // store all the already processed url traverse
                while ((i = sb.indexOf(TEMP_START_TAG)) > -1) {
                    String uuid = sb.substring(i + TEMP_START_TAG.length(), i + TEMP_START_TAG.length() + 8);
                    final String endTag = TEMP_END_TAG + uuid + TEMP_CLOSING;
                    int j = sb.indexOf(endTag);
                    alreadyParsedFragmentsMap.put(uuid, sb.substring(i + TEMP_FULL_START_TAG.length(), j));
                    sb.delete(i, j + endTag.length());
                    sb.insert(i, "\"/>").insert(i, uuid).insert(i, REPLACED_START_TAG);
                }
            }

            // traverse the fragment and wrap it with a temp tag
            sb = new StringBuilder(urlTraverser.traverse(sb == null ? previousOut : sb.toString(), renderContext, resource, handlers));
            sb.insert(0, TEMP_CLOSING).insert(0, thisuuid).insert(0, TEMP_START_TAG);
            sb.append(TEMP_END_TAG).append(thisuuid).append(TEMP_CLOSING);

            // replace all the previous stored fragments
            while ((i = sb.indexOf(REPLACED_START_TAG)) > -1) {
                String uuid = sb.substring(i+REPLACED_START_TAG.length(), i+REPLACED_START_TAG.length() + 8);
                sb.replace(i, i+REPLACED_START_TAG.length()+ 8 + 3, alreadyParsedFragmentsMap.get(uuid));
            }
            return sb.toString();
        }

        return previousOut;
    }

    /**
     * @param visitors the visitors to set
     */
    public void setHandlers(HtmlTagAttributeVisitor... visitors) {
        this.handlers = visitors;
    }

}
