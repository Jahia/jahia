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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.slf4j.Logger;
import org.apache.commons.lang.time.DateUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRAutoSplitUtils;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.test.utils.TestHelper;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.*;
import static org.junit.Assert.*;

import javax.jcr.RepositoryException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A unit test to validate the proper behavior of the auto-splitting algorithm when creating nodes through the API
 *
 * @author loom Date: Jul 15, 2010 Time: 12:30:11 PM
 */
public class AutoSplittingIT extends AbstractJUnitTest {

    private static Logger logger = org.slf4j.LoggerFactory
            .getLogger(AutoSplittingIT.class);

    private final static String TESTSITE_NAME = "autoSplittingTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/"
            + TESTSITE_NAME;
    private final static int TEST_NODE_COUNT = 100;

    private static String DEFAULT_LANGUAGE = "en";

    private static final String AUTO_SPLIT_CONFIG = "constant,testNodes;date,date,yyyy;date,date,MM;date,date,ss";
    private static final String AUTO_SPLIT_NODETYPE = Constants.JAHIANT_CONTENTLIST;

    private static final String STRING_SPLIT_CONFIG = "constant,test;substring,location,3-6;firstChars,jcr:title,4;property,eventsType";
    private static final String DATE_SPLIT_CONFIG = "date,startDate,yyyy;date,startDate,MM;date,startDate,dd";
    private static final String NODENAME_SPLIT_CONFIG = "firstChars,j:nodename,1";

    private static final SimpleDateFormat yearFormatter = new SimpleDateFormat(
            "yyyy");
    private static final SimpleDateFormat monthFormatter = new SimpleDateFormat(
            "MM");
    private static final SimpleDateFormat dayFormatter = new SimpleDateFormat(
            "dd");

    private static final String MEETING = "meeting";
    private static final String CONSUMER_SHOW = "consumerShow";
    private static final String ROAD_SHOW = "roadShow";
    private static final String CONFERENCE = "conference";
    private static final String SHOW = "show";
    private static final String PRESS_CONFERENCE = "pressConference";

    private static final String PARIS = "01-PAR-paris";
    private static final String GENEVA = "02-GVA-geneva";
    private static final Date BASE_DATE = new GregorianCalendar(2000, 0, 1, 12,
            0).getTime();

    private static final EventBean[] EVENTS = new EventBean[] {
            new EventBean(BASE_DATE, MEETING, PARIS),
            new EventBean(BASE_DATE, MEETING, GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 5), CONSUMER_SHOW, PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 5), CONSUMER_SHOW, PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 10), CONSUMER_SHOW,
                    GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 10), ROAD_SHOW, PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 15), ROAD_SHOW, PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 15), ROAD_SHOW, GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 20), ROAD_SHOW, GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 20), CONFERENCE, PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 25), CONFERENCE, PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 25), CONFERENCE, PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 30), CONFERENCE, GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 30), CONFERENCE, GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 35), SHOW, PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 35), SHOW, PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), SHOW, PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), SHOW, GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), SHOW, GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), SHOW, GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), SHOW, GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), PRESS_CONFERENCE,
                    PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), PRESS_CONFERENCE,
                    PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), PRESS_CONFERENCE,
                    PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), PRESS_CONFERENCE,
                    PARIS),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), PRESS_CONFERENCE,
                    GENEVA),
            new EventBean(DateUtils.addDays(BASE_DATE, 40), PRESS_CONFERENCE,
                    GENEVA) };

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            try {
                                TestHelper.createSite(TESTSITE_NAME, null);
                            } catch (Exception e) {
                                logger.error("Cannot create or publish site", e);
                            }

                            session.save();
                            return null;
                        }
                    });

            JCRSessionWrapper session = JCRSessionFactory.getInstance()
                    .getCurrentUserSession(
                            Constants.EDIT_WORKSPACE,
                            LanguageCodeConverters
                                    .languageCodeToLocale(DEFAULT_LANGUAGE));

            initContent(session);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance()
                    .getCurrentUserSession();
            if (session.nodeExists(SITECONTENT_ROOT_NODE)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }
            session.save();
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

    public static final class EventBean {
        private Date date;
        private String location;
        private String eventsType;

        public EventBean(Date date, String eventsType, String location) {
            this.date = date;
            this.location = location;
            this.eventsType = eventsType;
        }

        public Date getDate() {
            return date;
        }

        public String getLocation() {
            return location;
        }

        public String getEventsType() {
            return eventsType;
        }
    }

    public final class ValueBean {
        private Date date;
        private String name;
        private String title;
        private String description;

        public ValueBean(Date date, String name, String title,
                String description) {
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
    public void testAddNodeWithAutoSplittingByAPI() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession();
        JCRNodeWrapper homeNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/home");

        for (int i = 0; i < TEST_NODE_COUNT; i++) {
            ValueBean valueBean = new ValueBean(new Date(), "name" + i, "title"
                    + i, "description" + i);
            JCRNodeWrapper newNode = JCRAutoSplitUtils
                    .addNodeWithAutoSplitting(homeNode, "testNodeName" + i,
                            "jnt:mainContent", AUTO_SPLIT_CONFIG,
                            AUTO_SPLIT_NODETYPE, valueBean);
            assertNotNull("Node was not created correctly", newNode);
        }
        session.save();
    }

    @Test
    public void testMoveSubnodesToSplitFolderByRule()
            throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters
                        .languageCodeToLocale(DEFAULT_LANGUAGE));
        JCRNodeWrapper stringSplitNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/contents/stringSplit");
        JCRNodeWrapper dateSplitNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/contents/dateSplit");

        int i = 0;
        for (EventBean eventsBean : EVENTS) {
            assertTrue("Folders not correctly split", stringSplitNode.hasNode("test/"
                    + eventsBean.getLocation().substring(3, 6) + "/"
                    + eventsBean.getEventsType().substring(0, 4) + "/"
                    + eventsBean.getEventsType() + "/"
                    + eventsBean.getEventsType() + i++));
        }

        i = 0;
        for (EventBean eventsBean : EVENTS) {
            assertTrue("Folders not correctly split", dateSplitNode.hasNode(yearFormatter.format(eventsBean.getDate())
                    + "/" + monthFormatter.format(eventsBean.getDate()) + "/"
                    + dayFormatter.format(eventsBean.getDate()) + "/"
                    + eventsBean.getEventsType() + i++));
        }

        String basePath = SITECONTENT_ROOT_NODE + "/contents/nodenameSplit";
        assertTrue("Folders not correctly split for j:nodename split config",
                session.nodeExists(basePath + "/a/andromeda"));
        assertTrue("Folders not correctly split for j:nodename split config",
                session.nodeExists(basePath + "/b/barbarella"));
        assertTrue("Folders not correctly split for j:nodename split config",
                session.nodeExists(basePath + "/c/crazy-jane"));
        assertTrue("Folders not correctly split for j:nodename split config",
                session.nodeExists(basePath + "/d/dumb-bunny"));
        assertTrue("Folders not correctly split for j:nodename split config",
                session.nodeExists(basePath + "/e/elektra"));
        assertTrue("Folders not correctly split for j:nodename split config",
                session.nodeExists(basePath + "/f/firestar"));
        assertTrue("Folders not correctly split for j:nodename split config",
                session.nodeExists(basePath + "/g/gaia"));
    }

    @Test
    public void testMoveNewNodeToSplitFolderByRule() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters
                        .languageCodeToLocale(DEFAULT_LANGUAGE));

        JCRNodeWrapper stringSplitNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/contents/stringSplit");

        Date newEventDate = new GregorianCalendar(2010, 0, 1, 12,
                0).getTime();
        String newEventLocation = PARIS;
        String newEventType = ROAD_SHOW;

        createEvent(stringSplitNode, newEventType,
                newEventLocation, newEventDate, EVENTS.length);

        JCRNodeWrapper dateSplitNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/contents/dateSplit");

        createEvent(dateSplitNode, newEventType,
                newEventLocation, newEventDate, EVENTS.length);

        createText(session.getNode(SITECONTENT_ROOT_NODE + "/contents/nodenameSplit"), "indigo");

        session.save();

        assertTrue("Folders not correctly split", stringSplitNode.hasNode("test/"
                + newEventLocation.substring(3, 6) + "/"
                + newEventType.substring(0, 4) + "/"
                + newEventType + "/"
                + newEventType + EVENTS.length));

        assertTrue("Folders not correctly split", dateSplitNode.hasNode(yearFormatter.format(newEventDate)
                + "/" + monthFormatter.format(newEventDate) + "/"
                + dayFormatter.format(newEventDate) + "/"
                + newEventType + EVENTS.length));

        assertTrue("Folders not correctly split for j:nodename split config",
                session.nodeExists(SITECONTENT_ROOT_NODE + "/contents/nodenameSplit/i/indigo"));
    }

    private static void initContent(JCRSessionWrapper session)
            throws RepositoryException {
        int i = 0;

        JCRNodeWrapper contentNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/contents");
        session.getWorkspace().getVersionManager()
                .checkout(contentNode.getPath());

        JCRNodeWrapper stringSplitNode = createList(contentNode, "stringSplit");
        for (EventBean eventBean : EVENTS) {
            createEvent(stringSplitNode, eventBean.getEventsType(),
                    eventBean.getLocation(), eventBean.getDate(), i++);
        }
        stringSplitNode.addMixin(Constants.JAHIAMIX_AUTOSPLITFOLDERS);
        stringSplitNode
                .setProperty(Constants.SPLIT_CONFIG, STRING_SPLIT_CONFIG);
        stringSplitNode.setProperty(Constants.SPLIT_NODETYPE, AUTO_SPLIT_NODETYPE);

        i = 0;
        JCRNodeWrapper dateSplitNode = createList(contentNode, "dateSplit");
        for (EventBean eventBean : EVENTS) {
            createEvent(dateSplitNode, eventBean.getEventsType(),
                    eventBean.getLocation(), eventBean.getDate(), i++);
        }
        dateSplitNode.addMixin(Constants.JAHIAMIX_AUTOSPLITFOLDERS);
        dateSplitNode.setProperty(Constants.SPLIT_CONFIG, DATE_SPLIT_CONFIG);
        dateSplitNode.setProperty(Constants.SPLIT_NODETYPE, AUTO_SPLIT_NODETYPE);

        JCRNodeWrapper nodenameSplitNode = createList(contentNode, "nodenameSplit");
        createText(nodenameSplitNode, "andromeda");
        createText(nodenameSplitNode, "barbarella");
        createText(nodenameSplitNode, "crazy-jane");
        createText(nodenameSplitNode, "dumb-bunny");
        createText(nodenameSplitNode, "elektra");
        createText(nodenameSplitNode, "firestar");
        createText(nodenameSplitNode, "gaia");
        nodenameSplitNode.addMixin(Constants.JAHIAMIX_AUTOSPLITFOLDERS);
        nodenameSplitNode.setProperty(Constants.SPLIT_CONFIG, NODENAME_SPLIT_CONFIG);
        nodenameSplitNode.setProperty(Constants.SPLIT_NODETYPE, AUTO_SPLIT_NODETYPE);

        session.save();
    }

    private static JCRNodeWrapper createList(JCRNodeWrapper node, String name)
            throws RepositoryException {
        final JCRNodeWrapper list = node.addNode(name,
                Constants.JAHIANT_CONTENTLIST);
        list.setProperty("jcr:title", name);
        return list;
    }

    private static void createEvent(JCRNodeWrapper node,
            final String eventType, String location, Date date, int i)
            throws RepositoryException {
        final String name = eventType + i;
        final JCRNodeWrapper event = node.addNode(name, "jnt:event");
        event.setProperty("jcr:title", name);
        event.setProperty("eventsType", eventType);
        event.setProperty("location", location);
        event.setProperty("startDate", DateUtils.toCalendar(date));
    }

    private static void createText(JCRNodeWrapper node, final String name) throws RepositoryException {
        final JCRNodeWrapper txt = node.addNode(name, "jnt:text");
        txt.setProperty("text", name);
    }
}
