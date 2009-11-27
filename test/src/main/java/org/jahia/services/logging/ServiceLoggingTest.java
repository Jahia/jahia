/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.logging;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.jahia.bin.Jahia;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.tags.TaggingService;
import org.springframework.util.StopWatch;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 27 nov. 2009
 */
public class ServiceLoggingTest extends TestCase {
    private static transient Logger logger = Logger.getLogger(ServiceLoggingTest.class);
    private static final int TAGS_TO_CREATE = 1000;

	private int counter = 0;

	private TaggingService service;

	private String siteKey;

	private String tagPrefix;

	private String generateTagName() {
		return tagPrefix + counter++;
	}

	@Override
	protected void setUp() throws Exception {
		siteKey = Jahia.getThreadParamBean().getSiteKey();
		tagPrefix = "test-" + System.currentTimeMillis() + "-";
		service = (TaggingService) SpringContextSingleton.getBean("org.jahia.services.tags.TaggingService");
	}

	@Override
	protected void tearDown() throws Exception {
        deleteAllTags();
        tagPrefix = null;
		siteKey = null;
		counter = 0;
		service = null;
	}

    private void deleteAllTags() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                session.checkout("/content/sites/" + siteKey + "/tags");
                NodeIterator nodeIterator = session.getWorkspace().getQueryManager().createQuery(
                        "select * from [jnt:tag] " + "where ischildnode([/content/sites/" + siteKey
                                + "/tags]) and name() like '" + tagPrefix + "%'", Query.JCR_SQL2).execute().getNodes();
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
                session.checkout("/content/sites/" + siteKey);
                try {
                    session.getNode("/content/sites/" + siteKey + "/tags-content").remove();
                } catch (PathNotFoundException e) {
                    // ignore it
                }
                session.save();
                return null;
            }
        });
    }

    public void testCreateMultipleTags() throws RepositoryException {
		String tag;
        Logger metricsLogger = Logger.getLogger("loggingService");
        Logger profilerMetricsLogger = Logger.getLogger("profilerLoggingService");
        profilerMetricsLogger.setLevel(Level.OFF);
        StopWatch stopWatch = new StopWatch();
        metricsLogger.setLevel(Level.TRACE);
        stopWatch.start("Create "+TAGS_TO_CREATE+" with logs");
		for (int i = 0; i < TAGS_TO_CREATE; i++) {
			tag = generateTagName();
			service.createTag(tag, siteKey);
		}
        stopWatch.stop();
        final long withLogs = stopWatch.getTotalTimeMillis();
        deleteAllTags();
        metricsLogger.setLevel(Level.OFF);
        stopWatch.start("Create "+TAGS_TO_CREATE+" without logs");
		for (int i = 0; i < TAGS_TO_CREATE; i++) {
			tag = generateTagName();
			service.createTag(tag, siteKey);
		}
        stopWatch.stop();
        final long withoutLogs = stopWatch.getTotalTimeMillis();
        assertTrue("Logs as more than 5% impact on peformance",((Math.abs(withLogs-withoutLogs)/withoutLogs)*100)>5);
        logger.error(stopWatch.prettyPrint());
	}
}
