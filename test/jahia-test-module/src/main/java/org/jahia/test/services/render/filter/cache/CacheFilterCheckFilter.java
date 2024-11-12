/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
