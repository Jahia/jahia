/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.area.impl;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.area.AreaService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * The default implementation of the {@link AreaService} which is using classic concurrent
 * library to handle the area nodes read/write operations in a multi threads environment.
 *
 * @author Kevan
 */
public class DefaultAreaService implements AreaService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAreaService.class);

    private static final Map<String, CountDownLatch> generatingAreas = new HashMap<>();

    @Override
    public JCRNodeWrapper getOrCreateAreaNode(String areaPath, String areaType, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper areaParentNode = session.getNode(StringUtils.substringBeforeLast(areaPath, "/"));
        String areaName = StringUtils.substringAfterLast(areaPath, "/");
        JCRNodeWrapper areaNode;

        try {
            areaNode = areaParentNode.getNode(areaName);
        } catch (PathNotFoundException pathNotFoundException) {
            // Avoid race condition due to parallel creation of area node, when multiple requests are trying to create the same area
            CountDownLatch latch;
            boolean mustWait = true;

            synchronized (generatingAreas) {
                latch = generatingAreas.get(areaPath);
                if (latch == null) {
                    latch = new CountDownLatch(1);
                    generatingAreas.put(areaPath, latch);
                    mustWait = false;
                    logger.debug("Latch added on area '{}' creation", areaPath);
                }
            }

            if(mustWait) {
                try {
                    logger.debug("One thread is already creating area '{}', will wait for latch to be release", areaPath);
                    latch.await();
                    logger.debug("Wait is finished for area '{}', get the node", areaPath);
                    areaNode = areaParentNode.getNode(areaName);
                } catch (InterruptedException e) {
                    logger.error("The waiting thread has been interrupted", e);
                    throw new JahiaRuntimeException(e);
                }
            } else {
                logger.debug("Create the area '{}' node", areaPath);
                try {
                    areaNode = areaParentNode.addNode(areaName, areaType);
                    session.save();
                } finally {
                    synchronized (generatingAreas) {
                        latch.countDown();
                        generatingAreas.remove(areaPath);
                        logger.info("Latch released for area '{}'", areaPath);
                    }
                }
            }
        }
        return areaNode;
    }
}
