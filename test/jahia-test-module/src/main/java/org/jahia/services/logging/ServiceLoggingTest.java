/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.hamcrest.Matcher;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.tags.TaggingService;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StopWatch;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * 
 * 
 * @author : rincevent
 * @since JAHIA 6.5 Created : 27 nov. 2009
 */
public class ServiceLoggingTest {
    private static transient Logger logger = Logger.getLogger(ServiceLoggingTest.class);
    private static final int TAGS_TO_CREATE = 10;

    private int counter = 0;

    private TaggingService service;
    private final static String TESTSITE_NAME = "serviceLoggingTest";

    private String tagPrefix;

    private String generateTagName() {
        return tagPrefix + counter++;
    }

    @Before
    public void setUp() {
        try {
            TestHelper.createSite(TESTSITE_NAME);
            tagPrefix = "test-" + System.currentTimeMillis() + "-";
            service = (TaggingService) SpringContextSingleton
                    .getBean("org.jahia.services.tags.TaggingService");
        } catch (Exception e) {
            logger.error("Error setting up ServiceLoggingTest environment", e);
        }
    }

    @After
    public void tearDown() {
        try {
            deleteAllTags();
        } catch (Exception e) {
            logger.error("Error tearing down ServiceLoggingTest environment", e);
        }
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception e) {
            logger.error("Error tearing down ServiceLoggingTest environment", e);
        }        
        tagPrefix = null;
        counter = 0;
        service = null;
    }

    private void deleteAllTags() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                session.getWorkspace().getVersionManager()
                        .checkout("/sites/" + TESTSITE_NAME + "/tags");
                NodeIterator nodeIterator = session
                        .getWorkspace()
                        .getQueryManager()
                        .createQuery(
                                "select * from [jnt:tag] " + "where ischildnode([/sites/"
                                        + TESTSITE_NAME + "/tags]) and name() like '" + tagPrefix
                                        + "%'", Query.JCR_SQL2).execute().getNodes();
                while (nodeIterator.hasNext()) {
                    Node node = nodeIterator.nextNode();
                    try {
                        session.checkout(node);
                        node.remove();
                    } catch (PathNotFoundException e) {
                        // ignore -> it is a bug in Jackrabbit that produces
                        // duplicate results
                    }
                }
                session.getWorkspace().getVersionManager().checkout("/sites/" + TESTSITE_NAME);
                try {
                    session.getNode("/sites/" + TESTSITE_NAME + "/tags-content").remove();
                } catch (PathNotFoundException e) {
                    // ignore it
                }
                session.save();
                return null;
            }
        });
    }
    
    @Test
    public void testCreateMultipleTags() throws RepositoryException {
        for (int i = 0; i < 3; i++) {
            service.createTag(generateTagName(), TESTSITE_NAME);
        }
        
        Logger metricsLogger = Logger.getLogger("loggingService");
        Logger profilerMetricsLogger = Logger.getLogger("profilerLoggingService");
        profilerMetricsLogger.setLevel(Level.OFF);
        
        metricsLogger.setLevel(Level.OFF);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("Create " + TAGS_TO_CREATE + " without logs");
        for (int i = 0; i < TAGS_TO_CREATE; i++) {
            service.createTag(generateTagName(), TESTSITE_NAME);
        }
        stopWatch.stop();
        final long withoutLogs = stopWatch.getLastTaskTimeMillis();
        
        deleteAllTags();
        
        for (int i = 0; i < 3; i++) {
            service.createTag(generateTagName(), TESTSITE_NAME);
        }
        
        metricsLogger.setLevel(Level.TRACE);
        stopWatch.start("Create " + TAGS_TO_CREATE + " with logs");
        for (int i = 0; i < TAGS_TO_CREATE; i++) {
            service.createTag(generateTagName(), TESTSITE_NAME);
        }
        stopWatch.stop();
        final long withLogs = stopWatch.getLastTaskTimeMillis();
        
        logger.info(stopWatch.prettyPrint());
        
        if (withLogs > withoutLogs) {
            assertThat("Logs has more than 8% impact on peformance",
                    ((Math.abs(withLogs - withoutLogs) / (float)withoutLogs) * 100), (Matcher<Object>) lessThan((float)8));
        }
    }
}
