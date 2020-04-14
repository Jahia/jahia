/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an object with the information about rendering times of a resource including filter call events.
 */
public class RenderInfo {

    private static final Logger logger = LoggerFactory.getLogger(RenderInfo.class);
    
    private static ThreadLocal<RenderInfo> threadLocal = new ThreadLocal<RenderInfo>();
    private static List<RenderInfo> all = new ArrayList<>();

    private static boolean enabled = false;

    /**
     * Enables or disables rendering data collection.
     * 
     * @param enabled <code>true</code> to enable data collection; <code>false</code> to disable it
     */
    public static void setEnabled(boolean enabled) {
        RenderInfo.enabled = enabled;
        logger.info(enabled ? "The rendering data collection is now enabled"
                : "The rendering data collection is now disabled");
    }

    /**
     * Checks if the rendering data collection is enabled or not.
     * 
     * @return <code>true</code> if data collection is enabled; <code>false</code> if is it disabled
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Called on a start resource rendering.
     * 
     * @param resource the resource, the recording is started for
     */
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

    /**
     * Called when the rendering of the resource is finished to measure the ellapsed time.
     */
    public static void popResource() {
        RenderInfo info = threadLocal.get();
        if (info != null) {
            info.endTime = System.currentTimeMillis();
            threadLocal.set(info.parent);
        }
    }

    /**
     * "Record" a filter call event of the specified type
     * 
     * @param filter the filter which is being called
     * @param type the filter call type
     */
    public static void addFilterEvent(RenderFilter filter, int type) {
        RenderInfo info = threadLocal.get();
        if (info != null) {
            info.filterEvents.add(new FilterEvent(filter.getClass().getName(), System.currentTimeMillis(), type));
        }
    }

    /**
     * Performs a dump of the collected data in JSON format and disables the collection of data.
     * 
     * @return a string representation of collected data in JSON format
     */
    public static String dump() {
        setEnabled(false);
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            new ObjectMapper().writeValue(writer, all);
            all.clear();
        } catch (IOException e) {
            logger.warn("Error dumping rendering data into JSON. Cause: " + e.getMessage(), e);
        }


        return writer.getBuffer().toString();

    }

    private Resource resource;
    private long startTime = System.currentTimeMillis();
    private long endTime;
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

    public List<FilterEvent> getFilterEvents() {
        return filterEvents;
    }

    public List<RenderInfo> getSubResources() {
        return subResources;
    }

    /**
     * Represents a single filter call event.
     */
    public static class FilterEvent {
        public static final int TYPE_PREPARE = 0;
        public static final int TYPE_EXECUTE = 1;
        public static final int TYPE_GET_ERROR_CONTENT = 2;
        public static final int TYPE_FINALIZE = 3;

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
