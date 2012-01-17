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

import org.slf4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.*;

/**
 * Unit test for checking different index options
 * 
 * @author Benjamin Papez
 * 
 */
public class IndexOptionsTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(IndexOptionsTest.class);
    private final static String TESTSITE_NAME = "jcrIndexOptionsTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
            assertNotNull(site);

            JCRStoreService jcrService = ServicesRegistry.getInstance()
                    .getJCRStoreService();
            JCRSessionWrapper session = jcrService.getSessionFactory()
                    .getCurrentUserSession();
            InputStream importStream = IndexOptionsTest.class.getClassLoader()
                    .getResourceAsStream("jahia-test-module-war/src/main/resources/imports/importIndexOptionNodes.xml");
            session.importXML(SITECONTENT_ROOT_NODE + "/home", importStream,
                    ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
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
                String query = "select indexFields.* from [test_templates:fieldsWithIndexOptions] as indexFields where contains(indexFields.*, 'nonindexed')";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                QueryResult queryResult = q.execute();
                
                assertEquals("Query did not return correct number of results", 0, getResultSize(queryResult.getNodes()));

                query = "//element(*, test_templates:fieldsWithIndexOptions)[jcr:like(@nonIndexedSmallText, 'n%')]";
                q = queryManager.createQuery(query, Query.XPATH);
                queryResult = q.execute();

                assertEquals("Query did not return correct number of results", 0, getResultSize(queryResult.getNodes()));                
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
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
                String query = "select indexFields.* from [test_templates:fieldsWithIndexOptions] as indexFields where contains(indexFields.*, 'ZXY')";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                QueryResult queryResult = q.execute();

                assertEquals("Query did not return correct number of results", 0, getResultSize(queryResult.getNodes()));                

                query = "select indexFields.* from [test_templates:fieldsWithIndexOptions] as indexFields where indexFields.nofulltextSmallText like 'ZXY%'";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                queryResult = q.execute();

                assertTrue(getResultSize(queryResult.getNodes()) > 0);
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
                String query = "select indexFields.* from [test_templates:fieldsWithIndexOptions] as indexFields order by indexFields.[sortableFloat] asc";
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

                query = "select indexFields.* from [test_templates:fieldsWithIndexOptions] as indexFields order by indexFields.[nofulltextSmallText] asc";
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

                query = "select indexFields.* from [test_templates:fieldsWithIndexOptions] as indexFields order by indexFields.[simpleSmallText] asc";
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

                query = "select indexFields.* from [test_templates:fieldsWithIndexOptions] as indexFields order by indexFields.[untokenizedDate] asc";
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
    public void testPublishUnpublishPageWithContent() throws Exception {
        JCRStoreService jcrService = ServicesRegistry.getInstance()
                .getJCRStoreService();
        JCRSessionWrapper session = jcrService.getSessionFactory()
                .getCurrentUserSession();
        try {
            QueryManager queryManager = session.getWorkspace()
                    .getQueryManager();

            if (queryManager != null) {
                String query = "select indexFields.* from [test_templates:fieldsWithIndexOptions] as indexFields where contains(indexFields.*, 'ABBA')";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                QueryResult queryResult = q.execute();
                NodeIterator it = queryResult.getNodes();
                assertTrue(it.hasNext());
                assertTrue(((Node) it.next()).getIdentifier().equals(
                        "8c467cc3-a42c-4252-84b7-0b20ecc0ce30"));
                assertTrue(it.hasNext());
                assertTrue(((Node) it.next()).getIdentifier().equals(
                        "225162ba-69ac-4128-a141-fd95bd8c792e"));
                assertTrue(!it.hasNext());
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

}