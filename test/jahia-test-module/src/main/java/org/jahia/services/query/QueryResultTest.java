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

package org.jahia.services.query;

import org.apache.jackrabbit.util.ISO8601;
import org.slf4j.Logger;
import org.jahia.api.Constants;
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

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import java.util.*;

import static org.junit.Assert.*;

public class QueryResultTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(QueryResultTest.class);

    private static String DEFAULT_LANGUAGE = "en";

    private final static String TESTSITE_NAME = "jcrQueryTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String XPATH_SITECONTENT_ROOT_NODE = "/jcr:root/sites/" + TESTSITE_NAME;
    
    private final static String NEWS_NAME_PREFIX = "news_";

    private static final String MEETING = "meeting";
    private static final String CONSUMER_SHOW = "consumerShow";
    private static final String ROAD_SHOW = "roadShow";
    private static final String CONFERENCE = "conference";
    private static final String SHOW = "show";
    private static final String PRESS_CONFERENCE = "pressConference";
    private static final String PARIS = "paris";
    private static final String GENEVA = "geneva";

    private static final int NOT_CHILD_CHECK = 1;
    private static final int NOT_DESCENDANT_CHECK = 2;

    private static final String FIRST_PHONE_SENTENCE = "The horse doesn't eat cucumber salad";
    private static final String FIRST_TELEGRAPH_SENTENCE = "What hath God wrought?";

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
    public void setUp() {

    }

    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testChildNodeQueries() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(
                Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        QueryResultWrapper res;

        // check one childnode
        res = doQuery(session, "SELECT * FROM [jnt:news] as news WHERE ISCHILDNODE(news, ["
                + SITECONTENT_ROOT_NODE + "/contents/news]) ORDER BY news.[jcr:title]",
                Query.JCR_SQL2);
        checkResultSize(res, 16);

        // check two childnodes with or
        res = doQuery(
                session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/news]) OR ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/events]) ORDER BY content.[jcr:title]",
                Query.JCR_SQL2);
        checkResultSize(res, 43);

        // check childnodes with not
        res = doQuery(session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE NOT ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/news]) ORDER BY content.[jcr:title]",
                Query.JCR_SQL2);
        int size = checkHierarchy(res, NOT_CHILD_CHECK, SITECONTENT_ROOT_NODE + "/contents/news");

        // check childnodes with not and not
        res = doQuery(session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE NOT (ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/events]) OR ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/news])) ORDER BY content.[jcr:title]",
                Query.JCR_SQL2);
        int size2 = checkHierarchy(res, NOT_CHILD_CHECK,
                SITECONTENT_ROOT_NODE + "/contents/events", SITECONTENT_ROOT_NODE
                        + "/contents/news");
        assertEquals("Difference between result sizes is wrong.", 27, size - size2);

        // check childnodes with descendant and not child
        res = doQuery(
                session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents]) AND NOT ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/events]) ORDER BY content.[jcr:title]",
                Query.JCR_SQL2);
        checkResultSize(res, 23);

        // check childnodes with comparison
        res = doQuery(session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/news]) AND content.[jcr:title] LIKE 'news_1%'", Query.JCR_SQL2);
        checkResultSize(res, 7);

        // check childnodes with freetext
        res = doQuery(session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/news]) AND contains(content.*, 'cucumber')", Query.JCR_SQL2);
        checkResultSize(res, 10);
    }

    @Test
    public void testChildNodeXPathQueries() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(
                Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        QueryResultWrapper res;

        // check one childnode
        res = doQuery(session, XPATH_SITECONTENT_ROOT_NODE
                + "/contents/news/element(*, jnt:news) order by @jcr:title", Query.XPATH);
        checkResultSize(res, 16);

        // check two childnodes with or
        res = doQuery(
                session,
                XPATH_SITECONTENT_ROOT_NODE
                        + "/contents/*[fn:name() = 'news' or fn:name() = 'events']/element(*, jmix:editorialContent) order by @jcr:title",
                Query.XPATH);
        checkResultSize(res, 43);

        // check childnodes with comparison
        res = doQuery(
                session,
                XPATH_SITECONTENT_ROOT_NODE
                        + "/contents/news/element(*, jmix:editorialContent)[@date = '" + ISO8601.format(new GregorianCalendar(2000, 0, 1, 12, 0)) + "']",
                Query.XPATH);
        checkResultSize(res, 2);

        // check childnodes with freetext
        res = doQuery(session, XPATH_SITECONTENT_ROOT_NODE
                + "/contents/news/element(*, jmix:editorialContent)[jcr:contains(., 'cucumber') or jcr:contains(j:translation_en, 'cucumber')]",
                Query.XPATH);
        checkResultSize(res, 10);
    }

    @Test
    public void testDescendantNodeQueries() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(
                Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        QueryResultWrapper res;

        // check one descendantnode
        res = doQuery(session, "SELECT * FROM [jnt:news] as news WHERE ISDESCENDANTNODE(news, ["
                + SITECONTENT_ROOT_NODE + "/contents/news]) ORDER BY news.[jcr:title]",
                Query.JCR_SQL2);
        checkResultSize(res, 23);

        // check two descendantnodes with or
        res = doQuery(
                session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/news]) OR ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/events]) ORDER BY content.[jcr:title]",
                Query.JCR_SQL2);
        checkResultSize(res, 50);

        // check descendantnodes with not
        res = doQuery(session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE NOT ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/news]) ORDER BY content.[jcr:title]",
                Query.JCR_SQL2);
        int size = checkHierarchy(res, NOT_DESCENDANT_CHECK, SITECONTENT_ROOT_NODE
                + "/contents/news");

        // check descendantnodes with not and not
        res = doQuery(session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE NOT (ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/events]) OR ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/news])) ORDER BY content.[jcr:title]",
                Query.JCR_SQL2);
        int size2 = checkHierarchy(res, NOT_DESCENDANT_CHECK, SITECONTENT_ROOT_NODE
                + "/contents/events", SITECONTENT_ROOT_NODE + "/contents/news");
        assertEquals("Difference between result sizes is wrong.", 27, size - size2);

        // check descendantnodes with descendant and not descendant
        res = doQuery(session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents]) AND NOT ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/news]) ORDER BY content.[jcr:title]",
                Query.JCR_SQL2);
        checkResultSize(res, 27);

        // check descendantnodes with comparison
        res = doQuery(session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/news]) AND content.[jcr:title] LIKE 'news_1%'", Query.JCR_SQL2);
        checkResultSize(res, 11);

        // check descendantnodes with freetext
        res = doQuery(session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/news]) AND contains(content.*, 'cucumber')", Query.JCR_SQL2);
        checkResultSize(res, 13);

        // check descendantnodes or childnodes with comparison
        res = doQuery(
                session,
                "SELECT * FROM [jmix:editorialContent] as content WHERE (ISCHILDNODE(content, ["
                        + SITECONTENT_ROOT_NODE
                        + "/contents/news]) AND content.[jcr:title] LIKE 'news_1%') OR (ISDESCENDANTNODE(content, ["
                        + SITECONTENT_ROOT_NODE + "/contents/events]) AND content.[location] = '"
                        + GENEVA + "')", Query.JCR_SQL2);
        checkResultSize(res, 7 + 12);
    }

    @Test
    public void testDescendantNodeXPathQueries() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(
                Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        QueryResultWrapper res;

        // check one descendantnode
        res = doQuery(session, XPATH_SITECONTENT_ROOT_NODE
                + "/contents/news//element(*, jnt:news) [@jcr:language = 'en'] order by @jcr:title", Query.XPATH);
        checkResultSize(res, 23);

        // check two descendantnodes with or
        res = doQuery(
                session,
                XPATH_SITECONTENT_ROOT_NODE
                        + "/contents/*[fn:name() = 'news' or fn:name() = 'events']//element(*, jmix:editorialContent) [@jcr:language = 'en'] order by @jcr:title",
                Query.XPATH);        
        checkResultSize(res, 50);

        // check descendantnodes with comparison
        res = doQuery(
                session,
                XPATH_SITECONTENT_ROOT_NODE
                        + "/contents/news//element(*, jmix:editorialContent)[not(@jcr:language) and @date = '" + ISO8601.format(new GregorianCalendar(2000, 0, 1, 12, 0)) + "']",
                Query.XPATH);        
        checkResultSize(res, 5);

        // check descendantnodes with freetext
        res = doQuery(session, XPATH_SITECONTENT_ROOT_NODE
                + "/contents/news//element(*, jmix:editorialContent) [@jcr:language = 'en' and jcr:contains(., 'cucumber')]",
                Query.XPATH);        
        checkResultSize(res, 13);
    }

    private int checkHierarchy(QueryResultWrapper res, int check, final String... pathes) {
        int size = 0;
        try {
            NodeIterator ni = res.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper) ni.next();
                for (String path : pathes) {
                    if (check == NOT_CHILD_CHECK) {
                        assertFalse(
                                "There is a child node in the result, which should not be there: "
                                        + node.getParent().getPath(), node.getParent().getPath()
                                        .equals(path));
                    } else if (check == NOT_DESCENDANT_CHECK) {
                        assertFalse(
                                "There is a descendant node in the result, which should not be there: "
                                        + node.getPath(), node.getPath().startsWith(path));
                    }
                }
                size++;
            }
        } catch (RepositoryException e) {
            logger.error("Error while checking query result hierarchy", e);
        }
        return size;
    }

    private static void initContent(JCRSessionWrapper session) throws RepositoryException {
        int i = 0;
        Calendar calendar = new GregorianCalendar(2000, 0, 1, 12, 0);

        JCRNodeWrapper cats = session.getNode("/sites/systemsite/categories");
        session.getWorkspace().getVersionManager().checkout(cats.getPath());
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

    private QueryResultWrapper doQuery(JCRSessionWrapper session, final String statement,
            String language) throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Query: " + statement);
        }
        Query query = session.getWorkspace().getQueryManager().createQuery(statement, language);
        QueryResultWrapper res = (QueryResultWrapper) query.execute();
        return res;
    }

    private void checkResultSize(QueryResultWrapper res, final int expected)
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
        final JCRNodeWrapper event = node.addNode(name, "jnt:event");
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
        final JCRNodeWrapper event = node.addNode(name, "jnt:news");
        event.setProperty("jcr:title", name);
        event.setProperty("desc", desc);
        event.setProperty("date", calendar);
        event.addMixin("jmix:categorized");
        event.setProperty("j:defaultCategory", new Value[] { event.getSession().getValueFactory()
                .createValue(category) });
        return event;
    }

}
