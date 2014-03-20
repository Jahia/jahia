/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.modules.channels.filters;

import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.slf4j.Logger;

import javax.jcr.Property;
import javax.jcr.Value;

/**
 * This filter will exclude a module rendering from a node if it has the associated configuration.
 */
public class ChannelExclusionFilter extends AbstractFilter {

    protected transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ChannelExclusionFilter.class);

    private ChannelService channelService;

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (resource.getNode().isNodeType("jmix:channelSelection")) {
            // we have a mixin applied, let's test if we must exclude it from the current channel.
            if (!resource.getNode().hasProperty("j:channelSelection")) {
                logger.warn("Channel selection activated on resource "+resource.getPath()+" but no channels selected, please select channels for this function to be activated properly.");
                return null;
            }
            Property channelExclusionProperty = resource.getNode().getProperty("j:channelSelection");
            String includeOrExclude = resource.getNode().hasProperty("j:channelIncludeOrExclude") ? resource.getNode().getProperty("j:channelIncludeOrExclude").getString() : "exclude";
            Value[] channelExclusionValues = channelExclusionProperty.getValues();
            Channel currentChannel = renderContext.getChannel();
            for (Value channelExclusionValue : channelExclusionValues) {
                if (channelExclusionValue.getString() != null) {
                    boolean inList = isInExclusionList(channelExclusionValue.getString(), currentChannel);
                    if (inList && includeOrExclude.equals("exclude")) {
                        return "";
                    }
                    if (inList && includeOrExclude.equals("include")) {
                        return null;
                    }
                }
            }
            if (includeOrExclude.equals("include")) {
                return "";
            }
        }
        return null;
    }

    private boolean isInExclusionList(String v, Channel channel) {
        if (v.equals(channel.getIdentifier())) {
            return true;
        } else {
            if (channel.getFallBack() != null && !channel.getFallBack().equals("root")) {
                return isInExclusionList(v, channelService.getChannel(channel.getFallBack()));
            }
        }
        return false;
    }

}
