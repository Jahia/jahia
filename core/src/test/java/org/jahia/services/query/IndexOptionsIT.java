/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.query;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit test for checking different index options
 *
 * @author Benjamin Papez
 *
 */
public class IndexOptionsIT extends AbstractJUnitTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(IndexOptionsIT.class);
    private final static String TESTSITE_NAME = "jcrIndexOptionsTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        try {
            JahiaSite site = TestHelper.createSite(TESTSITE_NAME, null);
            assertNotNull(site);

            JCRStoreService jcrService = ServicesRegistry.getInstance()
                    .getJCRStoreService();
            JCRSessionWrapper session = jcrService.getSessionFactory()
                    .getCurrentUserSession();
            InputStream importStream = IndexOptionsIT.class.getClassLoader()
                    .getResourceAsStream("imports/importIndexOptionNodes.xml");
            session.importXML(SITECONTENT_ROOT_NODE + "/home", importStream,
                    ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
            importStream.close();
            session.save();
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testNonIndexedFields() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();
        try {
            QueryManager queryManager = session.getWorkspace()
                    .getQueryManager();

            if (queryManager != null) {
                Set<QueryToTest> queries = new HashSet<>();
                queries.add(new QueryToTest("select indexFields.* from [test:fieldsWithIndexOptions] as indexFields where contains(indexFields.*, " +
                        "'nonindexed')", Query.JCR_SQL2, 0));
                queries.add(new QueryToTest("//element(*, test:fieldsWithIndexOptions)[jcr:like(@nonIndexedSmallText, 'n%')]", Query.XPATH, 0));
                queries.add(new QueryToTest("select * from [test:excludeFromIndexContent]", Query.JCR_SQL2, 0));
                queries.add(new QueryToTest("//element(*, test:excludeFromIndexContent)", Query.XPATH, 0));
                queries.add(new QueryToTest("select * from [test:excludeFromIndexContentMixin]", Query.JCR_SQL2, 1));
                queries.add(new QueryToTest("//element(*, test:excludeFromIndexContentMixin)", Query.XPATH, 1));

                for (QueryToTest queryToTest : queries) {
                    String query = queryToTest.query;
                    Query q = queryManager.createQuery(query, queryToTest.type);
                    QueryResult queryResult = q.execute();
                    assertEquals(String.format("Query %s did not return correct number of results", query), queryToTest.result, getResultSize(queryResult.getNodes
                            ()));
                }
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }

    private  class QueryToTest {
        private String query, type;
        private int result;

        QueryToTest(String query, String type, int result) {
            this.query = query;
            this.type = type;
            this.result = result;
        }
    }

    private long getResultSize(NodeIterator nodes) {
        long resultSize = nodes.getSize();
        if (resultSize == -1) {
            resultSize = 0;
            for (NodeIterator it = nodes; it.hasNext(); ) {
                it.next();
                resultSize++;
            }
        }
        return resultSize;
    }

    @Test
    public void testNoFulltextIndexedField() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();
        try {
            QueryManager queryManager = session.getWorkspace()
                    .getQueryManager();

            if (queryManager != null) {
                String query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields where contains(indexFields.*, 'ZXY')";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                QueryResult queryResult = q.execute();

                assertEquals("Query did not return correct number of results", 0, getResultSize(queryResult.getNodes()));

                query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields where indexFields.nofulltextSmallText like 'ZXY%'";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                queryResult = q.execute();

                assertTrue("Query did not return correct number of results", getResultSize(queryResult.getNodes()) > 0);
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }

    @Test
    public void testSorting() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();
        try {
            QueryManager queryManager = session.getWorkspace()
                    .getQueryManager();

            if (queryManager != null) {
                String query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields order by indexFields.[sortableFloat] asc";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                QueryResult queryResult = q.execute();
                Node previousNode = null;
                for (NodeIterator it = queryResult.getNodes(); it.hasNext();) {
                    Node currentNode = (Node) it.next();
                    if (previousNode != null) {
                        double previousDouble = 0;
                        double currentDouble = 0;
                        try {
                            previousDouble = previousNode.getProperty(
                                    "sortableFloat").getDouble();
                        } catch (Exception ex) {
                        }
                        try {
                            currentDouble = currentNode.getProperty(
                                    "sortableFloat").getDouble();
                        } catch (Exception ex) {
                        }
                        assertTrue(previousDouble <= currentDouble);
                    }
                    previousNode = currentNode;
                }

                query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields order by indexFields.[nofulltextSmallText] asc";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                queryResult = q.execute();
                previousNode = null;
                for (NodeIterator it = queryResult.getNodes(); it.hasNext();) {
                    Node currentNode = (Node) it.next();
                    if (previousNode != null) {
                        String previousString = "";
                        String currentString = "";
                        try {
                            previousString = previousNode.getProperty(
                                    "nofulltextSmallText").getString();
                        } catch (Exception ex) {
                        }
                        try {
                            currentString = currentNode.getProperty(
                                    "nofulltextSmallText").getString();
                        } catch (Exception ex) {
                        }
                        assertTrue(previousString.compareTo(currentString) < 0);
                    }
                    previousNode = currentNode;
                }

                query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields order by indexFields.[simpleSmallText] asc";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                queryResult = q.execute();
                previousNode = null;
                for (NodeIterator it = queryResult.getNodes(); it.hasNext();) {
                    Node currentNode = (Node) it.next();
                    if (previousNode != null) {
                        String previousString = "";
                        String currentString = "";
                        try {
                            previousString = previousNode.getProperty(
                                    "simpleSmallText").getString();
                        } catch (Exception ex) {
                        }
                        try {
                            currentString = currentNode.getProperty(
                                    "simpleSmallText").getString();
                        } catch (Exception ex) {
                        }
                        assertTrue(previousString.compareTo(currentString) < 0);
                    }
                    previousNode = currentNode;
                }

                query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields order by indexFields.[untokenizedDate] asc";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                queryResult = q.execute();
                previousNode = null;
                for (NodeIterator it = queryResult.getNodes(); it.hasNext();) {
                    Node currentNode = (Node) it.next();
                    if (previousNode != null) {
                        Calendar previousDate = null;
                        Calendar currentDate = null;
                        try {
                            previousDate = previousNode.getProperty(
                                    "untokenizedDate").getDate();
                        } catch (Exception ex) {
                        }
                        try {
                            currentDate = currentNode.getProperty(
                                    "untokenizedDate").getDate();
                        } catch (Exception ex) {
                        }
                        if (previousDate != null && currentDate != null) {
                            assertTrue(previousDate.compareTo(currentDate) < 0);
                        }
                    }
                    previousNode = currentNode;
                }
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }

    @Test
    public void testFulltextAndNonIndexedField() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();
        try {
            QueryManager queryManager = session.getWorkspace()
                    .getQueryManager();

            if (queryManager != null) {
                String query = "select indexFields.* from [test:fieldsWithIndexOptions] as indexFields where contains(indexFields.*, 'ABBA')";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                QueryResult queryResult = q.execute();
                NodeIterator it = queryResult.getNodes();
                assertEquals(2, it.getSize());
                Set<String> results = new HashSet<String>();
                results.add(it.nextNode().getIdentifier());
                results.add(it.nextNode().getIdentifier());
                assertTrue(results.containsAll(Arrays.asList("8c467cc3-a42c-4252-84b7-0b20ecc0ce30",
                        "225162ba-69ac-4128-a141-fd95bd8c792e")));
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

}
