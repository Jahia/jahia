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
package org.jahia.params.valves;

import org.apache.commons.lang.StringUtils;
import org.jahia.pipelines.impl.GenericPipeline;
import org.jahia.pipelines.valves.Valve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Common class for authentication valves.
 * 
 * @author Sergiy Shyrkov
 * @since Jahia 6.6.0.0
 */
public abstract class AutoRegisteredBaseAuthValve extends BaseAuthValve implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(AutoRegisteredBaseAuthValve.class);

    private GenericPipeline authPipeline;

    private int position = -1;
    private String positionAfter;
    private String positionBefore;

    public void afterPropertiesSet() {
        initialize();
        
        removeValve(getId());
        
        int index = -1;
        if (position >= 0) {
            index = position;
        } else if (positionBefore != null) {
            index = indexOf(positionBefore);
        } else if (positionAfter != null) {
            index = indexOf(positionAfter);
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
     * Returns the position of the specified valve by ID.
     * 
     * @param id
     *            the ID of the valve to search for
     * @return the position of the specified valve by ID or <code>-1</code> if the valve is not found
     */
    private int indexOf(String id) {
        Valve[] registeredValves = authPipeline.getValves();
        for (int i = 0; i < registeredValves.length; i++) {
            Valve v = registeredValves[i];
            if (v instanceof BaseAuthValve && StringUtils.equals(((BaseAuthValve) v).getId(), id)) {
                return i;
            }

        }

        return -1;
    }

    /**
     * Removes the valve by its ID.
     * 
     * @param id
     *            the ID of the valve to be removed from the pipeline
     */
    private void removeValve(String id) {
        Valve[] registeredValves = authPipeline.getValves();
        for (Valve v : registeredValves) {
            if (v instanceof BaseAuthValve && StringUtils.equals(((BaseAuthValve) v).getId(), id)) {
                authPipeline.removeValve(v);
                // do not stop: remove all for that ID
            }
        }
    }

    public void setAuthPipeline(GenericPipeline authPipeline) {
        this.authPipeline = authPipeline;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setPositionAfter(String positionAfter) {
        this.positionAfter = positionAfter;
    }

    public void setPositionBefore(String positionBefore) {
        this.positionBefore = positionBefore;
    }
}
