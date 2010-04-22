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
        try {
            site = TestHelper.createSite(TESTSITE_NAME);
            assertNotNull(site);

            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                    LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));

            initContent(session);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
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

    private void initContent(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper node = session.getNode("/sites/jcrFacetTest/contents");
        i = 0;
        Calendar calendar = new GregorianCalendar(2000, 0, 1, 12, 0);
        createEvent(node, MEETING, PARIS, calendar);
        createEvent(node, MEETING, GENEVA, calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONSUMER_SHOW, PARIS, calendar);
        createEvent(node, CONSUMER_SHOW, PARIS, calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONSUMER_SHOW, GENEVA, calendar);
        createEvent(node, ROAD_SHOW, PARIS, calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, ROAD_SHOW, PARIS, calendar);
        createEvent(node, ROAD_SHOW, GENEVA, calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, ROAD_SHOW, GENEVA, calendar);
        createEvent(node, CONFERENCE, PARIS, calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONFERENCE, PARIS, calendar);
        createEvent(node, CONFERENCE, PARIS, calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, CONFERENCE, GENEVA, calendar);
        createEvent(node, CONFERENCE, GENEVA, calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, SHOW, PARIS, calendar);
        createEvent(node, SHOW, PARIS, calendar);
        calendar.add(Calendar.DAY_OF_MONTH, 5);
        createEvent(node, SHOW, PARIS, calendar);
        createEvent(node, SHOW, GENEVA, calendar);
        createEvent(node, SHOW, GENEVA, calendar);
        createEvent(node, SHOW, GENEVA, calendar);
        createEvent(node, SHOW, GENEVA, calendar);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar);
        createEvent(node, PRESS_CONFERENCE, PARIS, calendar);
        createEvent(node, PRESS_CONFERENCE, GENEVA, calendar);
        createEvent(node, PRESS_CONFERENCE, GENEVA, calendar);

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

    private void createEvent(JCRNodeWrapper node, final String eventType, String location, Calendar calendar)
            throws RepositoryException {
        final String name = eventType + (i++);
        final JCRNodeWrapper event = node.addNode(name, "jnt:event");
        event.setProperty("jcr:title", name);
        event.setProperty("eventsType", eventType);
        event.setProperty("location", location);
        event.setProperty("startDate", calendar);
    }


}
