/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import org.slf4j.Logger;
import org.jahia.test.TestHelper;
import org.junit.*;
import static org.junit.Assert.*;

import javax.jcr.RepositoryException;
import java.util.Date;

/**
 * A unit test to validate the proper behavior of the auto-splitting algorithm when creating nodes through the API
 *
 * @todo can we expand this to also test auto-splitting using rules ?
 * 
 * @author loom
 *         Date: Jul 15, 2010
 *         Time: 12:30:11 PM
 */
public class AutoSplittingTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(AutoSplittingTest.class);

    private final static String TESTSITE_NAME = "findTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static int TEST_NODE_COUNT = 1000;

    private static final String AUTO_SPLIT_CONFIG = "constant,testNodes;date,date,yyyy;date,date,MM;date,date,ss";
    private static final String AUTO_SPLIT_NODETYPE = "jnt:contentList";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(TESTSITE_NAME);
                    } catch (Exception e) {
                        logger.error("Cannot create or publish site", e);
                    }

                    session.save();
                    return null;
                }
            });
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (session.nodeExists(SITECONTENT_ROOT_NODE)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }
            session.save();

            session.logout();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }


    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    public final class ValueBean {
        private Date date;
        private String name;
        private String title;
        private String description;

        public ValueBean(Date date, String name, String title, String description) {
            this.date = date;
            this.name = name;
            this.title = title;
            this.description = description;
        }

        public Date getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }
    }

    @Test
    public void testAddNodeWithAutoSplitting() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper homeNode = session.getNode(SITECONTENT_ROOT_NODE + "/home");

        for (int i=0; i < TEST_NODE_COUNT; i++) {
            ValueBean valueBean = new ValueBean(new Date(), "name" + i, "title" + i, "description" + i);
            JCRNodeWrapper newNode = JCRAutoSplitUtils.addNodeWithAutoSplitting(homeNode, "testNodeName" + i, "jnt:mainContent", AUTO_SPLIT_CONFIG, AUTO_SPLIT_NODETYPE, valueBean);
            assertNotNull("Node was not created correctly", newNode);
        }
        session.save();

    }

}
