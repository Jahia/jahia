/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
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
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
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
 */
package org.jahia.test.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CacheFilterCheckFilter extends AbstractFilter {

    private static Map<String,RequestData> data = new HashMap<String, RequestData>();

    public static void clear() {
        data.clear();
    }

    public static RequestData getData(String id) {
        return data.get(id);
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String key = renderContext.getRequest().getHeader("request-id");
        if (key != null) {
            if (resource.getContextConfiguration().equals(Resource.CONFIGURATION_PAGE)) {
                RequestData requestData = new RequestData();
                data.put(key, requestData);
            }

            RequestData requestData = data.get(key);
            requestData.getRenderCalled().add(resource.getPath());
            requestData.setCount(requestData.getCount() + 1);
        }
        return super.prepare(renderContext, resource, chain);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String key = renderContext.getRequest().getHeader("request-id");
        if (key != null) {
            if (resource.getContextConfiguration().equals(Resource.CONFIGURATION_PAGE)) {
                RequestData requestData = data.get(key);
                requestData.setServedFromCache((Set<String>) renderContext.getRequest().getAttribute("servedFromCache"));
                requestData.setTime(System.currentTimeMillis() - requestData.getTime());
            }
        }
        return super.execute(previousOut, renderContext, resource, chain);
    }

    class RequestData {
        private Set<String> renderCalled = new HashSet<String>();
        private Set<String> servedFromCache;
        private int count = 0;
        private long time = System.currentTimeMillis();

        Set<String> getRenderCalled() {
            return renderCalled;
        }

        void setRenderCalled(Set<String> renderCalled) {
            this.renderCalled = renderCalled;
        }

        Set<String> getServedFromCache() {
            return servedFromCache;
        }

        void setServedFromCache(Set<String> servedFromCache) {
            this.servedFromCache = servedFromCache;
        }

        int getCount() {
            return count;
        }

        void setCount(int count) {
            this.count = count;
        }

        long getTime() {
            return time;
        }

        void setTime(long time) {
            this.time = time;
        }
    }
}
