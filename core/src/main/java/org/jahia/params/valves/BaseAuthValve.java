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
package org.jahia.params.valves;

import org.apache.commons.lang.StringUtils;
import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.valves.Valve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Common class for authentication valves.
 *
 * @author Sergiy Shyrkov
 */
public abstract class BaseAuthValve implements Valve, BeanNameAware {
    private static final Logger logger = LoggerFactory.getLogger(BaseAuthValve.class);

    private String beanName;

    private boolean enabled = true;

    private String id;

    /**
     * Returns the position of the specified valve by ID.
     *
     * @param id
     *            the ID of the valve to search for
     * @return the position of the specified valve by ID or <code>-1</code> if the valve is not found
     */
    protected static int indexOf(String id, Pipeline authPipeline) {
        Valve[] registeredValves = authPipeline.getValves();
        for (int i = 0; i < registeredValves.length; i++) {
            Valve v = registeredValves[i];
            if (v instanceof BaseAuthValve && StringUtils.equals(((BaseAuthValve) v).getId(), id)) {
                return i;
            }

        }

        return -1;
    }

    protected void addValve(Pipeline authPipeline, Integer position, String positionAfter, String positionBefore) {
        int index = -1;
        if (position >= 0) {
            index = position;
        } else if (positionBefore != null) {
            index = indexOf(positionBefore, authPipeline);
        } else if (positionAfter != null) {
            index = indexOf(positionAfter, authPipeline);
            if (index != -1) {
                index++;
            }
            if (index >= authPipeline.getValves().length) {
                index = -1;
            }
        }
        if (index != -1) {
            authPipeline.addValve(index, this);
            logger.info("Registered authentication valve {} at position {}", getId(), index);
        } else {
            authPipeline.addValve(this);
            logger.info("Registered authentication valve {}", getId());
        }

    }

    /**
     * Removes the valve by its ID.
     *
     *            the ID of the valve to be removed from the pipeline
     */
    protected void removeValve(Pipeline authPipeline) {
        Valve[] registeredValves = authPipeline.getValves();
        for (Valve v : registeredValves) {
            if (v instanceof BaseAuthValve && StringUtils.equals(((BaseAuthValve) v).getId(), id)) {
                authPipeline.removeValve(v);
                // do not stop: remove all for that ID
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj != null && this.getClass() == obj.getClass()) {
            BaseAuthValve other = (BaseAuthValve) obj;
            return getId() != null ? other.getId() != null && getId().equals(other.getId()) : other
                    .getId() == null;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String getId() {
        return id;
    }

    public void initialize() {
        if (StringUtils.isEmpty(id)) {
            id = beanName;
        }
    }

    protected boolean isEnabled() {
        return enabled;
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setId(String id) {
        this.id = id;
    }
}
