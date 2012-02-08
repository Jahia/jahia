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

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.solr.client.solrj.response.FacetField;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.query.QOMBuilder;
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
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.*;

import static org.junit.Assert.*;

/**
 * 
 * User: toto
 * Date: Apr 19, 2010
 * Time: 5:03:27 PM
 * 
 */
public class FacetedQueryTest {
    private static String DEFAULT_LANGUAGE = "fr";
    
    private final static String TESTSITE_NAME = "jcrFacetTest";
    private static final String MEETING = "meeting";
    private static final String CONSUMER_SHOW = "consumerShow";
    private static final String ROAD_SHOW = "roadShow";
    private static final String CONFERENCE = "conference";
    private static final String SHOW = "show";
    private static final String PRESS_CONFERENCE = "pressConference";
    private static final String PARIS = "paris";
    private static final String GENEVA = "geneva";


    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
        assertNotNull(site);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
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

    }    
    
    @Test
    public void testSimpleFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        FacetField field;
        QueryResultWrapper res;

        // check facets
        res = doQuery(session, "eventsType", "rep:facet(facet.mincount=1)");
        checkResultSize(res, 27);
        field = res.getFacetField("eventsType");
        assertNotNull("Facet field is null",field);
        assertNotNull("Facet values are null",field.getValues());
        assertEquals("Query did not return correct number of facets", 6, field.getValues().size());
        Iterator<FacetField.Count> counts = field.getValues().iterator();

        checkFacet(counts.next(), SHOW, 7);
        checkFacet(counts.next(), PRESS_CONFERENCE, 6);
        checkFacet(counts.next(), CONFERENCE, 5);
        checkFacet(counts.next(), ROAD_SHOW, 4);
        checkFacet(counts.next(), CONSUMER_SHOW, 3);
        checkFacet(counts.next(), MEETING, 2);

        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doFilteredQuery(session, "eventsType", count.getName());
            checkResultSize(resCheck, (int) count.getCount());
        }
        
        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doQuery(session, "rep:filter(eventsType)", count.getAsFilterQuery());
            checkResultSize(resCheck, (int) count.getCount());
        }        

        // test facet options : prefix
        res = doQuery(session, "eventsType", "rep:facet(facet.mincount=1&prefix=c)");
        field = res.getFacetField("eventsType");
        assertNotNull("Facet field is null",field);
        assertNotNull("Facet values are null",field.getValues());
        assertEquals("Query did not return correct number of facet values", 2, field.getValues().size());
        counts = field.getValues().iterator();

        checkFacet(counts.next(), CONFERENCE, 5);
        checkFacet(counts.next(), CONSUMER_SHOW, 3);
        
        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doQuery(session, "rep:filter(eventsType)", count.getAsFilterQuery());
            checkResultSize(resCheck, (int) count.getCount());
        }                

        // test facet options : sort=false  - lexicographic order
        res = doQuery(session, "eventsType", "rep:facet(facet.mincount=1&sort=false)");
        field = res.getFacetField("eventsType");
        assertNotNull("Facet field is null",field);
        assertNotNull("Facet values are null",field.getValues());
        assertEquals("Query did not return correct number of facet value", 6, field.getValues().size());
        counts = field.getValues().iterator();

        checkFacet(counts.next(), CONFERENCE, 5);
        checkFacet(counts.next(), CONSUMER_SHOW, 3);
        checkFacet(counts.next(), MEETING, 2);
        checkFacet(counts.next(), PRESS_CONFERENCE, 6);
        checkFacet(counts.next(), ROAD_SHOW, 4);
        checkFacet(counts.next(), SHOW, 7);
        
        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doQuery(session, "rep:filter(eventsType)", count.getAsFilterQuery());
            checkResultSize(resCheck, (int) count.getCount());
        }                

    }
    
    @Test    
    public void testDateFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        FacetField field;
        QueryResultWrapper res;

        // test date facets
        res = doQuery(session, "startDate", "rep:facet(date.start=2000-01-01T00:00:00Z&date.end=2002-01-01T00:00:00Z&date.gap=+1MONTH)");
        field = res.getFacetDate("startDate");

        assertEquals("Query did not return correct number of facets", 24, field.getValues().size());
        
        res = doQuery(session, "startDate", "rep:facet(facet.mincount=1&date.start=2000-01-01T00:00:00Z&date.end=2002-01-01T00:00:00Z&date.gap=+1MONTH)");
        field = res.getFacetDate("startDate");

        assertEquals("Query did not return correct number of facets", 2, field.getValues().size());
        
        Iterator<FacetField.Count> counts = field.getValues().iterator();

        checkFacet(counts.next(), "2000-01-01T00:00:00.000Z", 14);
        checkFacet(counts.next(), "2000-02-01T00:00:00.000Z", 13);
        
        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doQuery(session, "rep:filter(startDate)", count.getAsFilterQuery());
            checkResultSize(resCheck, (int) count.getCount());
        }

        res = doQuery(session, "startDate", "rep:facet(facet.mincount=1&date.start=2000-01-01T00:00:00Z&date.end=2002-01-01T00:00:00Z&date.gap=+1YEAR)");
        field = res.getFacetDate("startDate");

        assertEquals("Query did not return correct number of facets", 1, field.getValues().size());
        counts = field.getValues().iterator();

        checkFacet(counts.next(), "2000-01-01T00:00:00.000Z", 27);
        
        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doQuery(session, "rep:filter(startDate)", count.getAsFilterQuery());
            checkResultSize(resCheck, (int) count.getCount());
        }        
    }
    
    @Test
    public void testI18NFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        FacetField field;
        QueryResultWrapper res;

        // test i18n facets
        res = doQuery(session, "location", "rep:facet(facet.mincount=1)");
        field = res.getFacetField("location");
        assertNotNull("Facet field is null",field);
        assertNotNull("Facet values are null",field.getValues());
        assertEquals("Query did not return correct number of facets", 2, field.getValues().size());
        Iterator<FacetField.Count> counts = field.getValues().iterator();

        checkFacet(counts.next(), PARIS, 15);
        checkFacet(counts.next(), GENEVA, 12);

        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doFilteredQuery(session, "location", count.getName());
            checkResultSize(resCheck, (int) count.getCount());
        }
        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doQuery(session, "rep:filter(location)", count.getAsFilterQuery());
            checkResultSize(resCheck, (int) count.getCount());
        }        
    }
    
    @Test
    public void testCategoryFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        FacetField field;
        QueryResultWrapper res;

        // test i18n facets
        res = doQuery(session, "j:defaultCategory", "rep:facet(facet.mincount=1&nodetype=jmix:categorized)");
        field = res.getFacetField("j:defaultCategory");
        assertNotNull("Facet field is null",field);
        assertNotNull("Facet values are null",field.getValues());
        assertEquals("Query did not return correct number of facets", 3, field.getValues().size());
        Iterator<FacetField.Count> counts = field.getValues().iterator();

        checkFacet(counts.next(), "1/sites/systemsite/categories/cat3", 16);
        checkFacet(counts.next(), "1/sites/systemsite/categories/cat2", 6);
        checkFacet(counts.next(), "1/sites/systemsite/categories/cat1", 5);

        for (FacetField.Count count : field.getValues()) {
            String countName = count.getName();
            QueryResultWrapper resCheck = doFilteredQuery(session, "j:defaultCategory",
                    session.getNode(countName.substring(countName.indexOf("/"))).getIdentifier());
            checkResultSize(resCheck, (int) count.getCount());
        }
        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doQuery(session, "rep:filter(j:defaultCategory)", count.getAsFilterQuery());
            checkResultSize(resCheck, (int) count.getCount());
        }        
    }

    @Test
    public void testMultipleFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        FacetField field;
        QueryResultWrapper res;

        // check facets
        res = doQuery(session, "eventsType", "rep:facet(facet.mincount=1&key=1)", "startDate","rep:facet(facet.mincount=1&date.start=2000-01-01T00:00:00Z&date.end=2000-03-01T00:00:00Z&date.gap=+1MONTH&key=2)");
        checkResultSize(res, 27);
        field = res.getFacetField("eventsType");
        assertNotNull("Facet field is null",field);
        assertNotNull("Facet values are null",field.getValues());
        assertEquals("Query did not return correct number of facet value", 6 ,field.getValues().size());
        Iterator<FacetField.Count> counts = field.getValues().iterator();

        checkFacet(counts.next(), SHOW, 7);
        checkFacet(counts.next(), PRESS_CONFERENCE, 6);
        checkFacet(counts.next(), CONFERENCE, 5);
        checkFacet(counts.next(), ROAD_SHOW, 4);
        checkFacet(counts.next(), CONSUMER_SHOW, 3);
        checkFacet(counts.next(), MEETING, 2);
        
        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doQuery(session, "rep:filter(eventsType)", count.getAsFilterQuery());
            checkResultSize(resCheck, (int) count.getCount());
        }          

        field = res.getFacetDate("startDate");
        assertNotNull("Facet field is null",field);
        assertNotNull("Facet values are null",field.getValues());
        assertEquals("Query did not return correct number of facet values", 2, field.getValues().size());
        counts = field.getValues().iterator();

        checkFacet(counts.next(), "2000-01-01T00:00:00.000Z", 14);
        checkFacet(counts.next(), "2000-02-01T00:00:00.000Z", 13);
        
        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doQuery(session, "rep:filter(startDate)", count.getAsFilterQuery(), "eventsType", "rep:facet(facet.mincount=1&key=1)");
            checkResultSize(resCheck, (int) count.getCount());
            
            FacetField nestedField = resCheck.getFacetDate("startDate");
            
            assertNull("Facet field is not null",nestedField);
            
            nestedField = resCheck.getFacetField("eventsType");
            Iterator<FacetField.Count> nestedCounts = nestedField.getValues().iterator();
            
            if ("2000-01-01T00:00:00.000Z".equals(count.getName())) {
                checkFacet(nestedCounts.next(), CONFERENCE, 5);
                checkFacet(nestedCounts.next(), ROAD_SHOW, 4);
                checkFacet(nestedCounts.next(), CONSUMER_SHOW, 3);
                checkFacet(nestedCounts.next(), MEETING, 2);                
            } else {
                checkFacet(nestedCounts.next(), SHOW, 7);
                checkFacet(nestedCounts.next(), PRESS_CONFERENCE, 6);                
            }
            
            for (FacetField.Count nestedCount : nestedField.getValues()) {
                QueryResultWrapper nestedResCheck = doQuery(session, "rep:filter(startDate)", count.getAsFilterQuery(), "rep:filter(eventsType)", nestedCount.getAsFilterQuery());
                checkResultSize(nestedResCheck, (int) nestedCount.getCount());                
            }
        }        
    }

    @Test
    public void testQueryFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(DEFAULT_LANGUAGE));

        // test i18n facets
        QueryResultWrapper res = doQuery(session, "eventsType", "rep:facet(facet.query=0\\:FACET\\:eventsType:[a TO p]&facet.query=0\\:FACET\\:eventsType:[p TO z])");
        Map<String,Long> queryFacet = res.getFacetQuery();
        assertNotNull("Query facet result is null",queryFacet);
        assertEquals("Query did not return correct number of facets", 2, queryFacet.size());

        assertEquals("Facet count is incorrect", 10, queryFacet.get("0\\:FACET\\:eventsType:[a TO p]").longValue());
        assertEquals("Facet count is incorrect", 17, queryFacet.get("0\\:FACET\\:eventsType:[p TO z]").longValue());
        
        
        QueryResultWrapper resCheck = doQuery(session, "rep:filter()", "0\\:FACET\\:eventsType:[a TO p]");
        checkResultSize(resCheck, 10);
        resCheck = doQuery(session, "rep:filter()", "0\\:FACET\\:eventsType:[p TO z]");
        checkResultSize(resCheck, 17);            

        res = doQuery(session, "startDate", "rep:facet(facet.query=0\\:FACET\\:startDate:[2000-01-01T00:00:00.000Z TO 2000-01-01T00:00:00.000Z+1MONTH]&facet.query=0\\:FACET\\:startDate:[2000-01-01T00:00:00.000Z+1MONTH TO 2000-01-01T00:00:00.000Z+2MONTH])");
        queryFacet = res.getFacetQuery();
        assertNotNull("Query facet result is null",queryFacet);
        assertEquals("Query did not return correct number of facets", 2, queryFacet.size());
        
        assertEquals("Facet count is incorrect", 14, queryFacet.get("0\\:FACET\\:startDate:[2000-01-01T00:00:00.000Z TO 2000-01-01T00:00:00.000Z+1MONTH]").longValue());
        assertEquals("Facet count is incorrect", 13, queryFacet.get("0\\:FACET\\:startDate:[2000-01-01T00:00:00.000Z+1MONTH TO 2000-01-01T00:00:00.000Z+2MONTH]").longValue());        
        
        resCheck = doQuery(session, "rep:filter()", "0\\:FACET\\:startDate:[2000-01-01T00:00:00.000Z TO 2000-01-01T00:00:00.000Z+1MONTH]");
        checkResultSize(resCheck, 14);
        resCheck = doQuery(session, "rep:filter()", "0\\:FACET\\:startDate:[2000-01-01T00:00:00.000Z+1MONTH TO 2000-01-01T00:00:00.000Z+2MONTH]");
        checkResultSize(resCheck, 13);        
    }

    private static void initContent(JCRSessionWrapper session) throws RepositoryException {
        int i = 0;
        Calendar calendar = new GregorianCalendar(2000, 0, 1, 12, 0);

        JCRNodeWrapper cats = session.getNode("/sites/systemsite/categories");
        session.getWorkspace().getVersionManager().checkout(cats.getPath());
        if (!cats.hasNode("cat1")) cats.addNode("cat1", "jnt:category");
        if (!cats.hasNode("cat2")) cats.addNode("cat2", "jnt:category");
        if (!cats.hasNode("cat3")) cats.addNode("cat3", "jnt:category");
        JCRNodeWrapper cat1 = cats.getNode("cat1");
        JCRNodeWrapper cat2 = cats.getNode("cat2");
        JCRNodeWrapper cat3 = cats.getNode("cat3");

        JCRNodeWrapper node = session.getNode("/sites/jcrFacetTest/contents");
        session.getWorkspace().getVersionManager().checkout(node.getPath());        
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

        session.save();
    }

    private QueryResultWrapper doQuery(JCRSessionWrapper session, final String... facet) throws RepositoryException {
        QueryObjectModelFactory factory = session.getWorkspace().getQueryManager().getQOMFactory();
        QOMBuilder qomBuilder = new QOMBuilder(factory, session.getValueFactory());

        qomBuilder.setSource(factory.selector("jnt:event", "event"));
        qomBuilder.andConstraint(factory.descendantNode("event", "/sites/jcrFacetTest"));
        for (int j = 0; j < facet.length; j++) {
            String prop = facet[j++];
            String val = facet[j];
            if (prop.startsWith("rep:filter(")) {
                qomBuilder.andConstraint(factory.fullTextSearch("event", "rep:filter("
                        + Text.escapeIllegalJcrChars(StringUtils
                                .substringAfter(prop, "rep:filter(")), factory.literal(session
                        .getValueFactory().createValue(val))));
            } else {
                qomBuilder.getColumns().add(factory.column("event", prop, val));                
            }
        }

        QueryObjectModel qom = qomBuilder.createQOM();
        QueryResultWrapper res = (QueryResultWrapper) qom.execute();
        return res;
    }
    
    private QueryResultWrapper doFilteredQuery(JCRSessionWrapper session, final String... constraints)
            throws RepositoryException {
        QueryObjectModelFactory factory = session.getWorkspace().getQueryManager().getQOMFactory();
        QOMBuilder qomBuilder = new QOMBuilder(factory, session.getValueFactory());

        qomBuilder.setSource(factory.selector("jnt:event", "event"));
        qomBuilder.andConstraint(factory.descendantNode("event", "/sites/jcrFacetTest"));

        for (int j = 0; j < constraints.length; j++) {
            String prop = constraints[j++];
            String val = constraints[j];

            qomBuilder.andConstraint(factory.comparison(factory.propertyValue("event", prop),
                    QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
                    factory.literal(session.getValueFactory().createValue(val))));
        }


        QueryObjectModel qom = qomBuilder.createQOM();
        QueryResultWrapper res = (QueryResultWrapper) qom.execute();
        return res;
    }

    private void checkResultSize(QueryResultWrapper res, final int expected) throws RepositoryException {
        // check total results
        NodeIterator ni = res.getNodes();
        List<JCRNodeWrapper> results = new ArrayList<JCRNodeWrapper>();
        while (ni.hasNext()) {
            results.add((JCRNodeWrapper) ni.next());
        }
        assertEquals("", expected, results.size());
    }

    private void checkFacet(FacetField.Count c, final String name, final int count) {
        assertEquals("Facet are not correctly ordered or has incorrect name", name, c.getName());
        assertEquals("Facet count is incorrect", count, c.getCount());
    }

    private static void createEvent(JCRNodeWrapper node, final String eventType, String location, Calendar calendar,
                             JCRNodeWrapper category, int i)
            throws RepositoryException {
        final String name = eventType + i;
        final JCRNodeWrapper event = node.addNode(name, "jnt:event");
        event.setProperty("jcr:title", name);
        event.setProperty("eventsType", eventType);
        event.setProperty("location", location);
        event.setProperty("startDate", calendar);
        event.addMixin("jmix:categorized");
        event.setProperty("j:defaultCategory", new Value[] {event.getSession().getValueFactory().createValue(category)} );
    }


}
