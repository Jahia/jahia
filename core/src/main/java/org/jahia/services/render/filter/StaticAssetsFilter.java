/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.filter;

import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;

/**
 * Render filter that "injects" the static assets into the HEAD section of the
 * rendered HTML document.
 * 
 * @author Sergiy Shyrkov
 */
public class StaticAssetsFilter extends AbstractFilter {

    private static Logger logger = Logger.getLogger(StaticAssetsFilter.class);

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {

        Source source = new Source(previousOut);
        OutputDocument outputDocument = new OutputDocument(source);
        if (renderContext.isAjaxRequest()) {
            Element element = source.getFirstElement();
            final EndTag tag = element.getEndTag();
            if (tag == null) {
                logger.error("Couldn't find end tag for element " + element.getName() + " debugInfo="
                        + element.getDebugInfo() + " in markup [" + previousOut + "]");
            } else {
                final String staticsAsset = service.render(new Resource(resource.getNode(), "html",
                        "html.statics.assets", Resource.CONFIGURATION_INCLUDE), renderContext);
                outputDocument.replace(tag.getBegin(), tag.getBegin() + 1,
                        "\n" + AggregateCacheFilter.removeEsiTags(staticsAsset) + "\n<");
            }
        } else {
            final List<Element> elementList = source.getAllElements(HTMLElementName.HEAD);
            for (Element element : elementList) {
                final EndTag tag = element.getEndTag();
                final String staticsAsset = service.render(new Resource(resource.getNode(), "html",
                        "html.statics.assets", Resource.CONFIGURATION_INCLUDE), renderContext);
                outputDocument.replace(tag.getBegin(), tag.getBegin() + 1,
                        "\n" + AggregateCacheFilter.removeEsiTags(staticsAsset) + "\n<");
            }
        }

        String out = outputDocument.toString();

        return out;
    }
}
