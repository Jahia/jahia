/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.test.services.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;

import org.apache.jackrabbit.core.query.lucene.join.JahiaQueryEngine;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.query.QueryResultWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

public class NativeSortTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(NativeSortTest.class);

    private static String DEFAULT_LANGUAGE = "en";

    private final static String TESTSITE_NAME = "jcrQueryTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String NEWS_NAME_PREFIX = "news_";

    private static final String MEETING = "meeting";
    private static final String CONSUMER_SHOW = "consumerShow";
    private static final String ROAD_SHOW = "roadShow";
    private static final String CONFERENCE = "conference";
    private static final String SHOW = "show";
    private static final String PRESS_CONFERENCE = "pressConference";
    private static final String PARIS = "paris";
    private static final String GENEVA = "geneva";

    private static final String FIRST_PHONE_SENTENCE = "The horse doesn't eat cucumber salad";
    private static final String FIRST_TELEGRAPH_SENTENCE = "What hath God wrought?";

    private JCRSessionWrapper session;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
        assertNotNull(site);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(
                Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        initContent(session);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
    }

    @Before
    public void setUp() throws RepositoryException {
        session = JCRSessionFactory.getInstance().getCurrentUserSession(
                Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));
    }

    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testChildNodeQueries() throws Exception {
        // check one childnode
        doQuery("SELECT * FROM [jnt:news] as news WHERE ISCHILDNODE(news, ["
                + SITECONTENT_ROOT_NODE + "/contents/news]) ORDER BY news.[jcr:title]", 16, false);
        
        // check two childnodes with or
        doQuery(
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/news]) OR ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/events]) ORDER BY content.[jcr:title]",
                43, false);

        // check childnodes with descendant and not child
        doQuery(
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents]) AND NOT ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/events]) ORDER BY content.[jcr:title]",
                23, false);

        // check childnodes with comparison
        doQuery(
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/news]) AND content.[jcr:title] LIKE 'news_1%'", 7, false);

        // check childnodes with freetext
        doQuery(
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/news]) AND contains(content.*, 'cucumber')", 10, false);
    }
    
    @Test
    public void testDescendantNodeQueries() throws Exception {

        // check one descendantnode
        doQuery("SELECT * FROM [jnt:news] as news WHERE ISDESCENDANTNODE(news, ["
                + SITECONTENT_ROOT_NODE + "/contents/news]) ORDER BY news.[jcr:title]",
                23, false);

        // check two descendantnodes with or
        doQuery("SELECT * FROM [jmix:editorialContent] as content WHERE ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/news]) OR ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/events]) ORDER BY content.[jcr:title]",
                50, false);

        // check descendantnodes with descendant and not descendant
        doQuery("SELECT * FROM [jmix:editorialContent] as content WHERE ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents]) AND NOT ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/news]) ORDER BY content.[jcr:title]",
                27, false);

        // check descendantnodes with comparison
        doQuery("SELECT * FROM [jmix:editorialContent] as content WHERE ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/news]) AND content.[jcr:title] LIKE 'news_1%'", 11, false);

        // check descendantnodes with freetext
        doQuery("SELECT * FROM [jmix:editorialContent] as content WHERE ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/news]) AND contains(content.*, 'cucumber')", 13, false);

        // check descendantnodes or childnodes with comparison
        doQuery("SELECT * FROM [jmix:editorialContent] as content WHERE (ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/news]) AND content.[jcr:title] LIKE 'news_1%') OR (ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/events]) AND content.[location] = '"
                        + GENEVA + "')", 7 + 12, false);
    }
    
    @Test
    public void testSortByTitle() throws Exception {
        JCRNodeWrapper newsList = createList(session.getNode(SITECONTENT_ROOT_NODE + "/contents"), "news-title-test");
        createNews(newsList, "D news", null, null, null);
        createNews(newsList, "c news", null, null, null);
        createNews(newsList, "Z news", null, null, null);
        createNews(newsList, "A news", null, null, null);
        createNews(newsList, "z news", null, null, null);
        createNews(newsList, "C news", null, null, null);
        createNews(newsList, "b news", null, null, null);
        createNews(newsList, "B news", null, null, null);
        createNews(newsList, "d news", null, null, null);
        createNews(newsList, "a news", null, null, null);
        session.save();
        
        try {
            doQuery("SELECT * FROM [jnt:news] as news WHERE ISCHILDNODE(news, ["
                    + SITECONTENT_ROOT_NODE + "/contents/news-title-test]) ORDER BY news.[jcr:title]", 10, false);
        } finally {
            newsList.remove();
            session.save();
        }
    }
    
    @Test
    public void testSortByTitleNonLatin() throws Exception {
        JCRNodeWrapper newsList = createList(session.getNode(SITECONTENT_ROOT_NODE + "/contents"), "news-title-non-latin-test");
        createNews(newsList, "z test news", null, null, null);
        createNews(newsList, "A test news", null, null, null);
        createNews(newsList, "Z test news", null, null, null);
        createNews(newsList, "Test news", null, null, null);
        createNews(newsList, "a test news", null, null, null);
        createNews(newsList, "Тест новость", null, null, null);
        createNews(newsList, "01 test news", null, null, null);
        createNews(newsList, "Ä test news", null, null, null);
        createNews(newsList, "Å test news", null, null, null);
        createNews(newsList, "test news", null, null, null);
        createNews(newsList, "täst news", null, null, null);
        createNews(newsList, "tàst news", null, null, null);
        createNews(newsList, "tast news", null, null, null);
        createNews(newsList, "11 test news", null, null, null);
        createNews(newsList, "0 test news", null, null, null);
        createNews(newsList, "21 test news", null, null, null);
        createNews(newsList, "1 test news", null, null, null);
        session.save();
        
        try {
            doQuery("SELECT * FROM [jnt:news] as news WHERE ISCHILDNODE(news, ["
                    + SITECONTENT_ROOT_NODE + "/contents/news-title-non-latin-test]) ORDER BY news.[jcr:title]", 17, false);
        } finally {
            newsList.remove();
            session.save();
        }
    }
    
    private void checkSame(List<JCRNodeWrapper> nodes, List<JCRNodeWrapper> nodesNative,
            String query) throws RepositoryException {
        assertEquals("Result number for native and non-native sort are different for query: "
                + query, nodes.size(), nodesNative.size());
        for (int i = 0; i < nodes.size(); i++) {
            assertEquals("Results have a different order for query: " + query, nodes.get(i)
                    .getIdentifier(), nodesNative.get(i).getIdentifier());
        }
    }

    private static void initContent(JCRSessionWrapper session) throws RepositoryException {
        int i = 0;
        Calendar calendar = new GregorianCalendar(2000, 0, 1, 12, 0);

        JCRNodeWrapper cats = session.getNode("/sites/systemsite/categories");
        if (!cats.hasNode("cat1"))
            cats.addNode("cat1", "jnt:category");
        if (!cats.hasNode("cat2"))
            cats.addNode("cat2", "jnt:category");
        if (!cats.hasNode("cat3"))
            cats.addNode("cat3", "jnt:category");
        JCRNodeWrapper cat1 = cats.getNode("cat1");
        JCRNodeWrapper cat2 = cats.getNode("cat2");
        JCRNodeWrapper cat3 = cats.getNode("cat3");

        JCRNodeWrapper contentNode = session.getNode(SITECONTENT_ROOT_NODE + "/contents");
        session.getWorkspace().getVersionManager().checkout(contentNode.getPath());

        JCRNodeWrapper node = createList(contentNode, "events");

        createEvent(node, MEETING, PARIS, calendar, cat1, i++);
        createEvent(node, MEETING, GENEVA, calendar, cat1, i++);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONSUMER_SHOW, PARIS, calendar, cat1, i++);
        createEvent(node, CONSUMER_SHOW, PARIS, calendar, cat1, i++);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONSUMER_SHOW, GENEVA, calendar, cat1, i++);
        createEvent(node, ROAD_SHOW, PARIS, calendar, cat2, i++);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, ROAD_SHOW, PARIS, calendar, cat2, i++);
        createEvent(node, ROAD_SHOW, GENEVA, calendar, cat2, i++);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, ROAD_SHOW, GENEVA, calendar, cat2, i++);
        createEvent(node, CONFERENCE, PARIS, calendar, cat2, i++);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONFERENCE, PARIS, calendar, cat2, i++);
        createEvent(node, CONFERENCE, PARIS, calendar, cat3, i++);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONFERENCE, GENEVA, calendar, cat3, i++);
        createEvent(node, CONFERENCE, GENEVA, calendar, cat3, i++);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, SHOW, PARIS, calendar, cat3, i++);
        createEvent(node, SHOW, PARIS, calendar, cat3, i++);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, SHOW, PARIS, calendar, cat3, i++);
        createEvent(node, SHOW, GENEVA, calendar, cat3, i++);
        createEvent(node, SHOW, GENEVA, calendar, cat3, i++);
        createEvent(node, SHOW, GENEVA, calendar, cat3, i++);
        createEvent(node, SHOW, GENEVA, calendar, cat3, i++);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar, cat3, i++);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar, cat3, i++);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar, cat3, i++);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar, cat3, i++);
        createEvent(node, PRESS_CONFERENCE, GENEVA, calendar, cat3, i++);
        createEvent(node, PRESS_CONFERENCE, GENEVA, calendar, cat3, i++);

        JCRNodeWrapper newsListNode = createList(contentNode, "news");

        calendar = new GregorianCalendar(2000, 0, 1, 12, 0);
        i = 0;
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat1);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_TELEGRAPH_SENTENCE, calendar, cat1);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat1);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat1);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_TELEGRAPH_SENTENCE, calendar, cat1);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat2);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat2);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_TELEGRAPH_SENTENCE, calendar, cat2);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_TELEGRAPH_SENTENCE, calendar, cat2);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat2);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat2);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat3);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_TELEGRAPH_SENTENCE, calendar, cat3);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_TELEGRAPH_SENTENCE, calendar, cat3);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat3);
        createNews(newsListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat3);

        JCRNodeWrapper newsSubListNode = createList(newsListNode, "news");
        
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createNews(newsSubListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat3);
        createNews(newsSubListNode, NEWS_NAME_PREFIX + i++, FIRST_TELEGRAPH_SENTENCE, calendar, cat3);
        createNews(newsSubListNode, NEWS_NAME_PREFIX + i++, FIRST_TELEGRAPH_SENTENCE, calendar, cat3);
        createNews(newsSubListNode, NEWS_NAME_PREFIX + i++, FIRST_TELEGRAPH_SENTENCE, calendar, cat3);
        
        calendar = new GregorianCalendar(2000, 0, 1, 12, 0);        
        createNews(newsSubListNode, NEWS_NAME_PREFIX + i++, FIRST_TELEGRAPH_SENTENCE, calendar, cat3);
        createNews(newsSubListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat3);
        createNews(newsSubListNode, NEWS_NAME_PREFIX + i++, FIRST_PHONE_SENTENCE, calendar, cat3);

        session.save();
    }

    private void doQuery(final String statement, int expectedResultCount, boolean compareTimes) throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Query: {}", statement);                    
        }
        
        setNativeSort(true);
        long timerNative = System.currentTimeMillis();
        List<JCRNodeWrapper> nodesNative = null;
        try {
            Query query = session.getWorkspace().getQueryManager().createQuery(statement, Query.JCR_SQL2);
            QueryResultWrapper res = (QueryResultWrapper) query.execute();
            nodesNative = checkResultSize(res, expectedResultCount);
        } finally {
            timerNative = System.currentTimeMillis() - timerNative;
            if (logger.isDebugEnabled()) {
                logger.debug("Query with native sort took {} ms", timerNative);
            }
        }
        
        setNativeSort(false);
        long timer = System.currentTimeMillis();
        List<JCRNodeWrapper> nodes = null;
        try {
            Query query = session.getWorkspace().getQueryManager().createQuery(statement, Query.JCR_SQL2);
            QueryResultWrapper res = (QueryResultWrapper) query.execute();
            nodes = checkResultSize(res, expectedResultCount);
        } finally {
            timer = System.currentTimeMillis() - timer;
            if (logger.isDebugEnabled()) {
                logger.debug("Query took {} ms", timer);
            }
        }
        checkSame(nodes, nodesNative, statement);
        
        if (compareTimes) {
            if (timerNative > timer) {
                logger.warn(
                        "Query with native sort is {}% slower ({} ms) than the standard query ({} ms): {}",
                        new Object[] {
                                Math.round((float) timerNative / (float) timer * 100f) - 100,
                                timerNative, timer, statement });
            } else {
                logger.info(
                        "Query with native sort is {}% faster ({} ms) than the standard query ({} ms)",
                        new Object[] {
                                Math.round((float) timer / (float) timerNative * 100f) - 100,
                                timerNative, timer });
            }
        }
    }

    private List<JCRNodeWrapper> checkResultSize(QueryResultWrapper res, final int expected)
            throws RepositoryException {
        // check total results
        NodeIterator ni = res.getNodes();
        List<JCRNodeWrapper> results = new ArrayList<JCRNodeWrapper>();
        while (ni.hasNext()) {
            results.add((JCRNodeWrapper) ni.next());
        }
        if (logger.isDebugEnabled()) {
            String newLine = System.getProperty("line.separator");
            StringBuffer debugMessage = new StringBuffer("Results: ").append(newLine);
            for (JCRNodeWrapper result : results) {
                debugMessage.append(result.getPath()).append(newLine);
            }
            logger.debug(debugMessage.toString());
        }
        assertEquals("", expected, results.size());
        return results;
    }

    private static JCRNodeWrapper createList(JCRNodeWrapper node, String name)
            throws RepositoryException {
        final JCRNodeWrapper list = node.addNode(name, Constants.JAHIANT_CONTENTLIST);
        list.setProperty("jcr:title", name);
        return list;
    }

    private static JCRNodeWrapper createEvent(JCRNodeWrapper node, final String eventType,
            String location, Calendar calendar, JCRNodeWrapper category, int i)
            throws RepositoryException {
        final String name = eventType + i;
        final JCRNodeWrapper event = node.addNode(JCRContentUtils.generateNodeName(name, 100), "jnt:event");
        event.setProperty("jcr:title", name);
        event.setProperty("eventsType", eventType);
        event.setProperty("location", location);
        event.setProperty("startDate", calendar);
        event.addMixin("jmix:categorized");
        event.setProperty("j:defaultCategory", new Value[] { event.getSession().getValueFactory()
                .createValue(category) });
        return event;
    }

    private static JCRNodeWrapper createNews(JCRNodeWrapper node, String name, String desc,
            Calendar calendar, JCRNodeWrapper category) throws RepositoryException {
        final JCRNodeWrapper event = node.addNode(JCRContentUtils.findAvailableNodeName(node, JCRContentUtils.generateNodeName(name, 100)), "jnt:news");
        event.setProperty("jcr:title", name);
        if (desc != null) {
            event.setProperty("desc", desc);
        }
        event.setProperty("date", calendar != null ? calendar : Calendar.getInstance());
        if (category != null) {
            event.addMixin("jmix:categorized");
            event.setProperty("j:defaultCategory", new Value[] { event.getSession().getValueFactory()
                    .createValue(category) });
        }
        return event;
    }
    
    private static void setNativeSort(boolean enabled) {
        JahiaQueryEngine.NATIVE_SORT = enabled;
    }
}
