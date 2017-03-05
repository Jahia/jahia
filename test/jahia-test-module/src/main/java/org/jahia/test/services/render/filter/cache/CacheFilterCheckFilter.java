/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Render filter use for testing purpose, store data for render chains executions
 */
public class CacheFilterCheckFilter extends AbstractFilter {

    private Map<String,RequestData> data = new HashMap<String, RequestData>();

    public void clear() {
        data.clear();
    }

    public RequestData getData(String id) {
        return data.get(id);
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String key = renderContext.getRequest().getHeader("request-id");
        if (key != null) {
            if (!data.containsKey(key)) {
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

    public class RequestData {
        private Set<String> renderCalled = new HashSet<String>();
        private Set<String> servedFromCache;
        private int count = 0;
        private long time = System.currentTimeMillis();

        public Set<String> getRenderCalled() {
            return renderCalled;
        }

        public void setRenderCalled(Set<String> renderCalled) {
            this.renderCalled = renderCalled;
        }

        public Set<String> getServedFromCache() {
            return servedFromCache;
        }

        public void setServedFromCache(Set<String> servedFromCache) {
            this.servedFromCache = servedFromCache;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }
}
