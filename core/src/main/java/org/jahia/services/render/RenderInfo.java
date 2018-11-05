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
package org.jahia.services.render;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jahia.services.render.filter.RenderFilter;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderInfo {

    private static ThreadLocal<RenderInfo> threadLocal = new ThreadLocal<RenderInfo>();
    private static List<RenderInfo> all = new ArrayList<>();

    private static boolean enabled = false;

    public static void setEnabled(boolean enabled) {
        RenderInfo.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void pushResource(Resource resource) {
        if (enabled) {
            RenderInfo r = new RenderInfo(resource);

            if (threadLocal.get() == null) {
                all.add(r);
            } else {
                r.setParent(threadLocal.get());
                threadLocal.get().subResources.add(r);
            }
            threadLocal.set(r);
        }
    }

    public static void popResource(String output) {
        RenderInfo info = threadLocal.get();
        if (info != null) {
            info.endTime = System.currentTimeMillis();
            info.output = output;
            threadLocal.set(info.parent);
        }
    }

    public static void addFilterEvent(RenderFilter filter, int type) {
        if (threadLocal.get() != null) {
            threadLocal.get().filterEvents.add(new FilterEvent(filter.getClass().getName(), System.currentTimeMillis(), type));
        }
    }

    public static String dump() {
        enabled = false;
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            new ObjectMapper().writeValue(writer, all);
            all.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return writer.getBuffer().toString();

    }

    private Resource resource;
    private long startTime = System.currentTimeMillis();
    private long endTime;
    private String output;
    private List<FilterEvent> filterEvents = new ArrayList<>();

    private RenderInfo parent;
    private List<RenderInfo> subResources = new ArrayList<>();

    private RenderInfo(Resource resource) {
        this.resource = resource;
    }

    private void setParent(RenderInfo parent) {
        this.parent = parent;
    }

    public String getResourcePath() {
        return resource.getPath();
    }

    public String getResourceTemplate() {
        return resource.getResolvedTemplate();
    }

    public String getContextConfiguration() {
        return resource.getContextConfiguration();
    }

    public Map<String, Serializable> getModuleParams() {
        return resource.getModuleParams();
    }

    public long getTime() {
        return endTime - startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    private String getOutput() {
        return output;
    }

    public List<FilterEvent> getFilterEvents() {
        return filterEvents;
    }

    public List<RenderInfo> getSubResources() {
        return subResources;
    }

    public static class FilterEvent {
        private String filter;
        private long date;
        private int type;

        FilterEvent(String filter, long date, int type) {
            this.filter = filter;
            this.date = date;
            this.type = type;
        }

        public String getFilter() {
            return filter;
        }

        public long getDate() {
            return date;
        }

        public int getType() {
            return type;
        }
    }
}
