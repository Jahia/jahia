/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.channels.filters;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import java.util.Map;

/**
 * Adds channel device image decoration
 */
public class ChannelPreviewFilter extends AbstractFilter {

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ChannelPreviewFilter.class);
    public static final String ACTIVE_VARIANT_QUERY_PARAMETER = "variant";

    private ChannelService channelService;

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (StringUtils.isEmpty(renderContext.getRequest().getParameter(ChannelResolutionFilter.ACTIVE_CHANNEL_QUERY_PARAMETER)) || renderContext.getRequest().getParameter("noembed") != null) {
            return null;
        }

        Channel channel = channelService.getChannel(renderContext.getRequest().getParameter(ChannelResolutionFilter.ACTIVE_CHANNEL_QUERY_PARAMETER));
        if (channel.isGeneric()) {
            return null;
        }
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
            String imageUrl = renderContext.getRequest().getContextPath() + capabilities.get("decorator-images").split(",")[variantIndex];
            String[] position = capabilities.get("decorator-screen-positions").split(",")[variantIndex].split("x");
            String[] dimension = capabilities.get("usable-resolutions").split(",")[variantIndex].split("x");

            String start = "<div style=\"width:" + imageSize[0] + "px; height:" + imageSize[1] + "px;";
            start += " background-image:url(" + imageUrl + "); background-repeat:no-repeat;\">\n";
            start += "<div style=\"position:absolute; left:" + (Integer.parseInt(position[0])+8) + "px; top:" + (Integer.parseInt(position[1])+7) + "px;";
            String url = renderContext.getRequest().getContextPath();
            url += new URLGenerator(renderContext, resource).getCurrent();
            url += "?";
            if (renderContext.getRequest().getQueryString() != null) {
                url += renderContext.getRequest().getQueryString();
                url += "&";
            }
            url += "channel="+channel.getIdentifier()+"&noembed=true&variant=" + variant;

            start += " width:" +  (Integer.parseInt(dimension[0])+15) + "px; height:" + dimension[1] + "px; overflow:hidden;\">" +
                    "<div>\n" +
                    "<iframe height=\"" + dimension[1] +"\" width=\"" + dimension[0] +"\" src=\""+ url +"\"" +
                    " frameborder=\"0\" />\n" +
                    "</div>\n";
            return start;
//            out.insert(out.indexOf(">", out.indexOf("<body")) + 1, start);
//            out.insert(out.indexOf("</body>"), "</div>\n</div>\n");
        }
        return null;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

}
