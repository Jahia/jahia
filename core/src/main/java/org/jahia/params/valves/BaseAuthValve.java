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
