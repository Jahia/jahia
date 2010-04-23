package org.jahia.services.search;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
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

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 19, 2010
 * Time: 5:03:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class FacetedSearchTest extends TestCase {
    private static Logger logger = Logger.getLogger(FacetedSearchTest.class);
    private JahiaSite site;
    private int i;

    private final static String TESTSITE_NAME = "jcrFacetTest";
    private static final String MEETING = "meeting";
    private static final String CONSUMER_SHOW = "consumerShow";
    private static final String ROAD_SHOW = "roadShow";
    private static final String CONFERENCE = "conference";
    private static final String SHOW = "show";
    private static final String PRESS_CONFERENCE = "pressConference";
    private static final String PARIS = "paris";
    private static final String GENEVA = "geneva";


    @Override
    protected void setUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME);
        assertNotNull(site);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        initContent(session);
    }

    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
    }

    public void testSimpleFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        FacetField field;
        QueryResultWrapper res;

        // check facets
        res = doQuery(session, "eventsType", "rep:facet()");
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

        // test facet options : prefix
        res = doQuery(session, "eventsType", "rep:facet(prefix=c)");
        field = res.getFacetField("eventsType");
        assertNotNull("Facet field is null",field);
        assertNotNull("Facet values are null",field.getValues());
        assertEquals("Query did not return correct number of facet values", 2, field.getValues().size());
        counts = field.getValues().iterator();

        checkFacet(counts.next(), CONFERENCE, 5);
        checkFacet(counts.next(), CONSUMER_SHOW, 3);

        // test facet options : sort=false  - lexicographic order
        res = doQuery(session, "eventsType", "rep:facet(sort=false)");
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

    }
    public void testDateFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        FacetField field;
        QueryResultWrapper res;

        // test date facets
        res = doQuery(session, "startDate", "rep:facet(date.start=2000-01-01T00:00:00Z&date.end=2002-01-01T00:00:00Z&date.gap=+1MONTH)");
        field = res.getFacetDate("startDate");

        assertEquals("Query did not return correct number of facets", 24, field.getValues().size());
        Iterator<FacetField.Count> counts = field.getValues().iterator();

        checkFacet(counts.next(), "2000-01-01T00:00:00.000Z", 14);
        checkFacet(counts.next(), "2000-02-01T00:00:00.000Z", 13);
        checkFacet(counts.next(), "2000-03-01T00:00:00.000Z", 0);

        res = doQuery(session, "startDate", "rep:facet(date.start=2000-01-01T00:00:00Z&date.end=2002-01-01T00:00:00Z&date.gap=+1YEAR)");
        field = res.getFacetDate("startDate");

        assertEquals("Query did not return correct number of facets", 2, field.getValues().size());
        counts = field.getValues().iterator();

        checkFacet(counts.next(), "2000-01-01T00:00:00.000Z", 27);
        checkFacet(counts.next(), "2001-01-01T00:00:00.000Z", 0);

    }

    public void testI18NFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        FacetField field;
        QueryResultWrapper res;

        // test i18n facets
        res = doQuery(session, "location", "rep:facet()");
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
    }

    public void testCategoryFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        FacetField field;
        QueryResultWrapper res;

        // test i18n facets
        res = doQuery(session, "j:defaultCategory", "rep:facet(nodetype=jmix:categorized)");
        field = res.getFacetField("j:defaultCategory");
        assertNotNull("Facet field is null",field);
        assertNotNull("Facet values are null",field.getValues());
        assertEquals("Query did not return correct number of facets", 3, field.getValues().size());
        Iterator<FacetField.Count> counts = field.getValues().iterator();

        checkFacet(counts.next(), session.getNode("/categories/cat3").getIdentifier(), 16);
        checkFacet(counts.next(), session.getNode("/categories/cat2").getIdentifier(), 6);
        checkFacet(counts.next(), session.getNode("/categories/cat1").getIdentifier(), 5);

        for (FacetField.Count count : field.getValues()) {
            QueryResultWrapper resCheck = doFilteredQuery(session, "j:defaultCategory", count.getName());
            checkResultSize(resCheck, (int) count.getCount());
        }
    }


    public void testMultipleFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        FacetField field;
        QueryResultWrapper res;

        // check facets
        res = doQuery(session, "eventsType", "rep:facet(key=1)", "startDate","rep:facet(key=2)");
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

        field = res.getFacetField("startDate");
        assertNotNull("Facet field is null",field);
        assertNotNull("Facet values are null",field.getValues());
        assertEquals("Query did not return correct number of facet value", 9 ,field.getValues().size());
    }

    public void testQueryFacets() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

        FacetField field;
        QueryResultWrapper res;

        // test i18n facets
        res = doQuery(session, "eventsType", "rep:facet(facet.query=1\\:FACET\\:eventsType\\:[a TO p]&facet.query=1\\:FACET\\:eventsType\\:[p TO z])");
        Map<String,Long> queryFacet = res.getFacetQuery();
        assertNotNull("Query facet result is null",queryFacet);
        assertEquals("Query did not return correct number of facets", 2, queryFacet.size());

        assertEquals("Facet count is incorrect", 10, queryFacet.get("1\\:FACET\\:eventsType\\:[a TO p]").longValue());
        assertEquals("Facet count is incorrect", 17, queryFacet.get("1\\:FACET\\:eventsType\\:[p TO z]").longValue());
    }

    private void initContent(JCRSessionWrapper session) throws RepositoryException {
        i = 0;
        Calendar calendar = new GregorianCalendar(2000, 0, 1, 12, 0);

        JCRNodeWrapper cats = session.getNode("/categories");
        cats.checkout();
        if (!cats.hasNode("cat1")) cats.addNode("cat1", "jnt:category");
        if (!cats.hasNode("cat2")) cats.addNode("cat2", "jnt:category");
        if (!cats.hasNode("cat3")) cats.addNode("cat3", "jnt:category");
        JCRNodeWrapper cat1 = cats.getNode("cat1");
        JCRNodeWrapper cat2 = cats.getNode("cat2");
        JCRNodeWrapper cat3 = cats.getNode("cat3");

        JCRNodeWrapper node = session.getNode("/sites/jcrFacetTest/contents");
        createEvent(node, MEETING, PARIS, calendar, cat1);
        createEvent(node, MEETING, GENEVA, calendar, cat1);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONSUMER_SHOW, PARIS, calendar, cat1);
        createEvent(node, CONSUMER_SHOW, PARIS, calendar, cat1);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONSUMER_SHOW, GENEVA, calendar, cat1);
        createEvent(node, ROAD_SHOW, PARIS, calendar, cat2);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, ROAD_SHOW, PARIS, calendar, cat2);
        createEvent(node, ROAD_SHOW, GENEVA, calendar, cat2);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, ROAD_SHOW, GENEVA, calendar, cat2);
        createEvent(node, CONFERENCE, PARIS, calendar, cat2);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONFERENCE, PARIS, calendar, cat2);
        createEvent(node, CONFERENCE, PARIS, calendar, cat3);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONFERENCE, GENEVA, calendar, cat3);
        createEvent(node, CONFERENCE, GENEVA, calendar, cat3);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, SHOW, PARIS, calendar, cat3);
        createEvent(node, SHOW, PARIS, calendar, cat3);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, SHOW, PARIS, calendar, cat3);
        createEvent(node, SHOW, GENEVA, calendar, cat3);
        createEvent(node, SHOW, GENEVA, calendar, cat3);
        createEvent(node, SHOW, GENEVA, calendar, cat3);
        createEvent(node, SHOW, GENEVA, calendar, cat3);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar, cat3);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar, cat3);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar, cat3);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar, cat3);
        createEvent(node, PRESS_CONFERENCE, GENEVA, calendar, cat3);
        createEvent(node, PRESS_CONFERENCE, GENEVA, calendar, cat3);

        session.save();
    }

    private QueryResultWrapper doQuery(JCRSessionWrapper session, final String... facet) throws RepositoryException {
        QueryObjectModelFactory factory = session.getWorkspace().getQueryManager().getQOMFactory();
        QOMBuilder qomBuilder = new QOMBuilder(factory, session.getValueFactory());

        qomBuilder.setSource(factory.selector("jnt:event", "event"));
        qomBuilder.andConstraint(factory.descendantNode("event", "/sites/jcrFacetTest"));
        for (int j = 0; j < facet.length; j++) {
            String prop = facet[j++];
            String col = facet[j];
            qomBuilder.getColumns().add(factory.column("event", prop, col));
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

    private void createEvent(JCRNodeWrapper node, final String eventType, String location, Calendar calendar,
                             JCRNodeWrapper category)
            throws RepositoryException {
        final String name = eventType + (i++);
        final JCRNodeWrapper event = node.addNode(name, "jnt:event");
        event.setProperty("jcr:title", name);
        event.setProperty("eventsType", eventType);
        event.setProperty("location", location);
        event.setProperty("startDate", calendar);
        event.addMixin("jmix:categorized");
        event.setProperty("j:defaultCategory", new Value[] {event.getSession().getValueFactory().createValue(category)} );
    }


}
