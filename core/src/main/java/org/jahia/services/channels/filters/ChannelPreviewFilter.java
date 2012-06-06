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

package org.jahia.services.channels.filters;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.channels.Channel;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import java.util.Map;

/**
 * Adds channel device image decoration
 */
public class ChannelPreviewFilter extends AbstractFilter {

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ChannelPreviewFilter.class);
    public static final String ACTIVE_VARIANT_QUERY_PARAMETER = "variant";

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (StringUtils.isEmpty(renderContext.getRequest().getParameter(ChannelResolutionFilter.ACTIVE_CHANNEL_QUERY_PARAMETER))) {
            return previousOut;
        }

        Channel channel = renderContext.getChannel();
        if (Channel.DEFAULT_CHANNEL.equals(channel)) {
            return previousOut;
        }

        StringBuffer out = new StringBuffer(previousOut);
        Map<String,String> capabilities = channel.getCapabilities();
        if (capabilities != null
                && capabilities.containsKey("variants")
                && capabilities.containsKey("decorator-image-sizes")
                && capabilities.containsKey("decorator-images")
                && capabilities.containsKey("decorator-screen-positions")
                && capabilities.containsKey("usable-resolutions")) {
            String[] variants = capabilities.get("variants").split(",");
            String variant = renderContext.getRequest().getParameter(ACTIVE_VARIANT_QUERY_PARAMETER);
            int variantIndex = 0;
            for (int i = 0; i < variants.length; i++) {
                if (variants[i].equals(variant)) {
                    variantIndex = i;
                    break;
                }
            }
            String[] imageSize = capabilities.get("decorator-image-sizes").split(",")[variantIndex].split("x");
            String imageUrl = capabilities.get("decorator-images").split(",")[variantIndex];
            String[] position = capabilities.get("decorator-screen-positions").split(",")[variantIndex].split("x");
            String[] dimension = capabilities.get("usable-resolutions").split(",")[variantIndex].split("x");

            String start = "<div style=\"width:" + imageSize[0] + "px; height:" + imageSize[1] + "px;";
            start += " background-image:url(" + imageUrl + "); background-repeat:no-repeat;\">\n";
            start += "<div style=\"position:absolute; left:" + position[0] + "px; top:" + position[1] + "px;";
            start += " width:" + dimension[0] + "px; height:" + dimension[1] + "px; overflow:scroll;\">\n";
            out.insert(out.indexOf(">", out.indexOf("<body")) + 1, start);
            out.insert(out.indexOf("</body>"), "</div>\n</div>\n");
        }
        return out.toString();
    }

}
