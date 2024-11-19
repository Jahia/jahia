/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.pipelines.impl.GenericPipeline;
import org.jahia.pipelines.valves.Valve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Common class for authentication valves.
 *
 * @author Sergiy Shyrkov
 * @since Jahia 6.6.0.0
 */
public abstract class AutoRegisteredBaseAuthValve extends BaseAuthValve implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(AutoRegisteredBaseAuthValve.class);

    private GenericPipeline authPipeline;

    private int position = -1;
    private String positionAfter;
    private String positionBefore;

    public void afterPropertiesSet() {
        initialize();

        removeValve(authPipeline);
        addValve(authPipeline, position, positionAfter, positionBefore);
    }

    @Override
    public void destroy() throws Exception {
        if (JahiaContextLoaderListener.isRunning()) {
            removeValve(authPipeline);
            logger.info("Unregistered authentication valve {}", getId());
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
