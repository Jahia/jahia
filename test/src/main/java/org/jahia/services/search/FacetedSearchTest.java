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

    public void testFacets() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(Constants.EDIT_WORKSPACE,
                        LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()));


        JCRNodeWrapper node = session.getNode("/sites/jcrFacetTest/contents");
        i = 0;
        createEvent(node, MEETING, PARIS);
        createEvent(node, MEETING, GENEVA);
        createEvent(node, CONSUMER_SHOW, PARIS);
        createEvent(node, CONSUMER_SHOW, PARIS);
        createEvent(node, CONSUMER_SHOW, GENEVA);
        createEvent(node, ROAD_SHOW, PARIS);
        createEvent(node, ROAD_SHOW, PARIS);
        createEvent(node, ROAD_SHOW, GENEVA);
        createEvent(node, ROAD_SHOW, GENEVA);
        createEvent(node, CONFERENCE, PARIS);
        createEvent(node, CONFERENCE, PARIS);
        createEvent(node, CONFERENCE, PARIS);
        createEvent(node, CONFERENCE, GENEVA);
        createEvent(node, CONFERENCE, GENEVA);
        createEvent(node, SHOW, PARIS);
        createEvent(node, SHOW, PARIS);
        createEvent(node, SHOW, PARIS);
        createEvent(node, SHOW, GENEVA);
        createEvent(node, SHOW, GENEVA);
        createEvent(node, SHOW, GENEVA);
        createEvent(node, SHOW, GENEVA);
        createEvent(node, PRESS_CONFERENCE, PARIS);
        createEvent(node, PRESS_CONFERENCE, PARIS);
        createEvent(node, PRESS_CONFERENCE, PARIS);
        createEvent(node, PRESS_CONFERENCE, PARIS);
        createEvent(node, PRESS_CONFERENCE, GENEVA);
        createEvent(node, PRESS_CONFERENCE, GENEVA);

        session.save();

        try {
            FacetField field;
            QueryResultWrapper res;

            // check facets
            res = doQuery(session, "rep:facet()");
            checkResultSize(res, 27);
            field = res.getFacetField("eventsType");

            assertEquals("Query did not return correct number of facets",6,field.getValues().size());
            Iterator<FacetField.Count> counts = field.getValues().iterator();

            checkFacet(counts.next(), SHOW, 7);
            checkFacet(counts.next(), PRESS_CONFERENCE, 6);
            checkFacet(counts.next(), CONFERENCE, 5);
            checkFacet(counts.next(), ROAD_SHOW, 4);
            checkFacet(counts.next(), CONSUMER_SHOW, 3);
            checkFacet(counts.next(), MEETING, 2);

            for (FacetField.Count count : field.getValues()) {
                QueryResultWrapper resCheck = doFilteredQuery(session, count.getName());
                checkResultSize(resCheck, (int) count.getCount());                
            }

            // test facet options : prefix
            res = doQuery(session, "rep:facet(prefix=c)");
            field = res.getFacetField("eventsType");
            assertEquals("Query did not return correct number of facets",2,field.getValues().size());
            counts = field.getValues().iterator();

            checkFacet(counts.next(), CONFERENCE, 5);
            checkFacet(counts.next(), CONSUMER_SHOW, 3);

            // test facet options : sort=false  - lexicographic order
            res = doQuery(session, "rep:facet(sort=false)");
            field = res.getFacetField("eventsType");

            assertEquals("Query did not return correct number of facets",6,field.getValues().size());
            counts = field.getValues().iterator();

            checkFacet(counts.next(), CONFERENCE, 5);
            checkFacet(counts.next(), CONSUMER_SHOW, 3);
            checkFacet(counts.next(), MEETING, 2);
            checkFacet(counts.next(), PRESS_CONFERENCE, 6);
            checkFacet(counts.next(), ROAD_SHOW, 4);
            checkFacet(counts.next(), SHOW, 7);

        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private QueryResultWrapper doQuery(JCRSessionWrapper session, final String facet)
            throws RepositoryException {
        QueryObjectModelFactory factory = session.getWorkspace().getQueryManager().getQOMFactory();
        QOMBuilder qomBuilder = new QOMBuilder(factory, session.getValueFactory());

        qomBuilder.setSource(factory.selector("jnt:event", "event"));
        qomBuilder.getColumns().add(factory.column("event", "eventsType", facet));

        QueryObjectModel qom  = qomBuilder.createQOM();
        QueryResultWrapper res = (QueryResultWrapper) qom.execute();
        return res;
    }

    private QueryResultWrapper doFilteredQuery(JCRSessionWrapper session, final String value)
            throws RepositoryException {
        QueryObjectModelFactory factory = session.getWorkspace().getQueryManager().getQOMFactory();
        QOMBuilder qomBuilder = new QOMBuilder(factory, session.getValueFactory());

        qomBuilder.setSource(factory.selector("jnt:event", "event"));

        qomBuilder.andConstraint(factory.comparison(factory.propertyValue("event", "eventsType"),
                QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
                factory.literal(session.getValueFactory().createValue(value))));

        QueryObjectModel qom  = qomBuilder.createQOM();
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

    private void createEvent(JCRNodeWrapper node, final String eventType, String location) throws RepositoryException {
        final String name = eventType + (i++);
        final JCRNodeWrapper event = node.addNode(name, "jnt:event");
        event.setProperty("jcr:title", name);
        event.setProperty("eventsType", eventType);
        event.setProperty("location", location);
    }


}
