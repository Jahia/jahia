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

package org.jahia.services.content;

import org.slf4j.Logger;
import org.apache.commons.lang.time.DateUtils;
import org.jahia.api.Constants;
import org.jahia.test.TestHelper;
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
public class AutoSplittingTest {

    private static Logger logger = org.slf4j.LoggerFactory
            .getLogger(AutoSplittingTest.class);

    private final static String TESTSITE_NAME = "autoSplittingTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/"
            + TESTSITE_NAME;
    private final static int TEST_NODE_COUNT = 100;

    private static String DEFAULT_LANGUAGE = "en";

    private static final String AUTO_SPLIT_CONFIG = "constant,testNodes;date,date,yyyy;date,date,MM;date,date,ss";
    private static final String AUTO_SPLIT_NODETYPE = Constants.JAHIANT_CONTENTLIST;

    private static final String STRING_SPLIT_CONFIG = "constant,test;substring,location,3-6;firstChars,jcr:title,4;property,eventsType";
    private static final String DATE_SPLIT_CONFIG = "date,startDate,yyyy;date,startDate,MM;date,startDate,dd";

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

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            try {
                                TestHelper.createSite(TESTSITE_NAME);
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
        stringSplitNode.addMixin(Constants.JAHIAMIX_AUTOSPLITFOLDERS);
        stringSplitNode
                .setProperty(Constants.SPLIT_CONFIG, STRING_SPLIT_CONFIG);
        stringSplitNode.setProperty(Constants.SPLIT_NODETYPE, AUTO_SPLIT_NODETYPE);

        JCRNodeWrapper dateSplitNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/contents/dateSplit");
        dateSplitNode.addMixin(Constants.JAHIAMIX_AUTOSPLITFOLDERS);
        dateSplitNode.setProperty(Constants.SPLIT_CONFIG, DATE_SPLIT_CONFIG);
        dateSplitNode.setProperty(Constants.SPLIT_NODETYPE, AUTO_SPLIT_NODETYPE);

        session.save();

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
    }

    private static void initContent(JCRSessionWrapper session)
            throws RepositoryException {
        int i = 0;

        JCRNodeWrapper contentNode = session.getNode(SITECONTENT_ROOT_NODE
                + "/contents");
        session.getWorkspace().getVersionManager()
                .checkout(contentNode.getPath());

        JCRNodeWrapper node = createList(contentNode, "stringSplit");
        for (EventBean eventBean : EVENTS) {
            createEvent(node, eventBean.getEventsType(),
                    eventBean.getLocation(), eventBean.getDate(), i++);
        }

        i = 0;
        node = createList(contentNode, "dateSplit");
        for (EventBean eventBean : EVENTS) {
            createEvent(node, eventBean.getEventsType(),
                    eventBean.getLocation(), eventBean.getDate(), i++);
        }

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
}
