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
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import static org.junit.Assert.*;

import java.util.*;

/**
 * Unit test for checking all different cases where we modify the query
 * 
 * @author Benjamin Papez
 * 
 */
public class QueryModificationTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(QueryModificationTest.class);

    private final static String TESTSITE_NAME = "jcrQueryModificationTest";

    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(TESTSITE_NAME, "localhost", TestHelper.WEB_BLUE_TEMPLATES);
                    } catch (Exception e) {
                        logger.error("Cannot create or publish site", e);
                    }
                    session.save();
                    return null;
                }
            });

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
    public void testSortingModification() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);
        try {
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            if (queryManager != null) {
                String query = "SELECT * FROM [jnt:news] as news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) ORDER BY news.[jcr:title]";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND news.[jcr:language] = 'en' ORDER BY news.[jcr:title]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());

                query = "SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND news.[jcr:language] = 'en' ORDER BY news.[jcr:title]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND news.[jcr:language] = 'en' ORDER BY news.[jcr:title]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT * FROM [jnt:news] as news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) ORDER BY news.[jcr:createdBy], news.[jcr:title], news.[jcr:created] desc";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND news.[jcr:language] = 'en' ORDER BY news.[jcr:createdBy], news.[jcr:title], news.[jcr:created] DESC", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND news.[jcr:language] = 'en' ORDER BY news.[jcr:createdBy], news.[jcr:title], news.[jcr:created] DESC";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND news.[jcr:language] = 'en' ORDER BY news.[jcr:createdBy], news.[jcr:title], news.[jcr:created] DESC", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
                
                query = "SELECT * FROM [jnt:news] as news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) ORDER BY news.[jcr:created] desc";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND (NOT news.[jcr:language] IS NOT NULL OR news.[jcr:language] = 'en') ORDER BY news.[jcr:created] DESC", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND (NOT news.[jcr:language] IS NOT NULL OR news.[jcr:language] = 'en') ORDER BY news.[jcr:created] DESC";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
// TODO: Uncomment, when we receive a Jackrabbit patch                 
//                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND (NOT news.[jcr:language] IS NOT NULL OR news.[jcr:language] = 'en') ORDER BY news.[jcr:created] DESC", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }

    @Test
    public void testPropertyComparison() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);
        try {
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            if (queryManager != null) {
                String query = "SELECT * FROM [jnt:news] as news WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (length(news.[desc]) > 100 OR news.[date] > '+2001-01-01T01:02:03.000Z') ORDER BY news.[jcr:title]";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (LENGTH(news.desc) > 100 OR news.date > '+2001-01-01T01:02:03.000Z') AND news.[jcr:language] = 'en' ORDER BY news.[jcr:title]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT news.* FROM [jnt:news] AS news WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (LENGTH(news.desc) > 100 OR news.date > '+2001-01-01T01:02:03.000Z') AND news.[jcr:language] = 'en' ORDER BY news.[jcr:title]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (LENGTH(news.desc) > 100 OR news.date > '+2001-01-01T01:02:03.000Z') AND news.[jcr:language] = 'en' ORDER BY news.[jcr:title]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                

// TODO: uncomment when we install a Jackrabbit patch                
//                query = "SELECT * FROM [jnt:news] as news WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (length(news.[j:nodename]) > 100 OR news.[date] > '+2001-01-01T01:02:03.000Z')";
//                q = queryManager.createQuery(query, Query.JCR_SQL2);
//                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (LENGTH(news.[j:nodename]) > 100 OR news.[date] > '+2001-01-01T01:02:03.000Z') AND (NOT news.[jcr:language] IS NOT NULL OR news.[jcr:language] = 'en')", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
//                
//                query = "SELECT news.* FROM [jnt:news] AS news WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (LENGTH(news.[j:nodename]) > 100 OR news.[date] > '+2001-01-01T01:02:03.000Z') AND (NOT news.[jcr:language] IS NOT NULL OR news.[jcr:language] = 'en')";
//                q = queryManager.createQuery(query, Query.JCR_SQL2);
//                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (LENGTH(news.[j:nodename]) > 100 OR news.[date] > '+2001-01-01T01:02:03.000Z') AND (NOT news.[jcr:language] IS NOT NULL OR news.[jcr:language] = 'en')", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }

    @Test
    public void testFulltextSearch() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);        
        try {
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            if (queryManager != null) {
                String query = "SELECT * FROM [jnt:news] as news WHERE contains(news.*, 'ACME') ORDER BY news.[date]";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE CONTAINS(news.*, 'ACME') AND news.[jcr:language] = 'en' ORDER BY news.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT news.* FROM [jnt:news] AS news WHERE CONTAINS(news.*, 'ACME') AND news.[jcr:language] = 'en' ORDER BY news.date";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE CONTAINS(news.*, 'ACME') AND news.[jcr:language] = 'en' ORDER BY news.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
                
                query = "SELECT * FROM [jnt:news] as news WHERE contains(news.[jcr:title], 'ACME') ORDER BY news.[date]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE CONTAINS(news.[jcr:title], 'ACME') AND news.[jcr:language] = 'en' ORDER BY news.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
                
                query = "SELECT news.* FROM [jnt:news] AS news WHERE CONTAINS(news.[jcr:title], 'ACME') AND news.[jcr:language] = 'en' ORDER BY news.date";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE CONTAINS(news.[jcr:title], 'ACME') AND news.[jcr:language] = 'en' ORDER BY news.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT * FROM [jnt:news] as news WHERE contains(news.[jcr:title], 'ACME') OR contains(news.[j:keywords], 'ACME') ORDER BY news.[date]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE (CONTAINS(news.[jcr:title], 'ACME') OR CONTAINS(news.[j:keywords], 'ACME')) AND news.[jcr:language] = 'en' ORDER BY news.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
                
                query = "SELECT news.* FROM [jnt:news] AS news WHERE (CONTAINS(news.[jcr:title], 'ACME') OR CONTAINS(news.[j:keywords], 'ACME')) AND news.[jcr:language] = 'en' ORDER BY news.date";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE (CONTAINS(news.[jcr:title], 'ACME') OR CONTAINS(news.[j:keywords], 'ACME')) AND news.[jcr:language] = 'en' ORDER BY news.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT * FROM [jmix:keywords] as node WHERE contains(node.[j:keywords], 'ACME')";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
// TODO: Uncomment, when we receive the Jackrabbit patch                
//                assertEquals("SELECT node.* FROM [jmix:keywords] AS node WHERE CONTAINS(node.[j:keywords], 'ACME') AND (NOT node.[jcr:language] IS NOT NULL OR node.[jcr:language] = 'en')", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
//
//                query = "SELECT * FROM [jmix:keywords] as node WHERE contains(node.*, 'ACME') AND (NOT node.[jcr:language] IS NOT NULL OR node.[jcr:language] = 'en')";
//                q = queryManager.createQuery(query, Query.JCR_SQL2);
//                assertEquals("SELECT node.* FROM [jmix:keywords] AS node WHERE CONTAINS(node.*, 'ACME') AND (NOT node.[jcr:language] IS NOT NULL OR node.[jcr:language] = 'en')", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                                
            }
        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }

    @Test
    public void testWithOtherLanguageSet() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);
        try {
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            if (queryManager != null) {
                String query = "SELECT * FROM [jnt:news] as news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND news.[jcr:language] = 'fr' ORDER BY news.[jcr:title]";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND news.[jcr:language] = 'fr' ORDER BY news.[jcr:title]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());

                query = "SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND news.[jcr:language] = 'fr' ORDER BY news.[jcr:title]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND news.[jcr:language] = 'fr' ORDER BY news.[jcr:title]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }
    
    @Test
    public void testWithJoins() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);
        try {
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            if (queryManager != null) {
                String query = "SELECT * FROM [jnt:news] as news INNER JOIN [jmix:tagged] AS tags ON news.[j.tags] = tags.[jcr:uuid] WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND LOCALNAME(tags)='test' ORDER BY news.[jcr:title]";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.*, tags.* FROM [jnt:news] AS news INNER JOIN [jmix:tagged] AS tags ON news.[j.tags] = tags.[jcr:uuid] WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND LOCALNAME(tags) = 'test' AND news.[jcr:language] = 'en' ORDER BY news.[jcr:title]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT news.*, tags.* FROM [jnt:news] AS news INNER JOIN [jmix:tagged] AS tags ON news.[j.tags] = tags.[jcr:uuid] WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND LOCALNAME(tags) = 'test' AND news.[jcr:language] = 'en' ORDER BY news.[jcr:title]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.*, tags.* FROM [jnt:news] AS news INNER JOIN [jmix:tagged] AS tags ON news.[j.tags] = tags.[jcr:uuid] WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND LOCALNAME(tags) = 'test' AND news.[jcr:language] = 'en' ORDER BY news.[jcr:title]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
            }

        } catch (Exception ex) {
            logger.warn("Exception during test", ex);
        } finally {
            session.save();
        }
    }
    
    @Test
    public void testWithReferences() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE,
                Locale.ENGLISH);
        try {
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            if (queryManager != null) {
                String query = "select press.* from [jnt:press] as press left outer join [nt:file] as file on press.pdfVersion = file.[jcr:uuid] inner join [nt:resource] as filecontent on ischildnode(filecontent, file) where contains(filecontent.*, 'ACME') or contains(press.*, 'ACME') order by press.[date] desc";
                Query q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT press.* FROM [jnt:press] AS press LEFT OUTER JOIN [nt:file] AS file ON press.pdfVersion = file.[jcr:uuid] INNER JOIN [nt:resource] AS filecontent ON ISCHILDNODE(filecontent, file) WHERE (CONTAINS(filecontent.*, 'ACME') OR CONTAINS(press.*, 'ACME')) AND press.[jcr:language] = 'en' ORDER BY press.date DESC", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT press.* FROM [jnt:press] AS press LEFT OUTER JOIN [nt:file] AS file ON press.pdfVersion = file.[jcr:uuid] INNER JOIN [nt:resource] AS filecontent ON ISCHILDNODE(filecontent, file) WHERE (CONTAINS(filecontent.*, 'ACME') OR CONTAINS(press.*, 'ACME')) AND press.[jcr:language] = 'en' ORDER BY press.date DESC";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT press.* FROM [jnt:press] AS press LEFT OUTER JOIN [nt:file] AS file ON press.pdfVersion = file.[jcr:uuid] INNER JOIN [nt:resource] AS filecontent ON ISCHILDNODE(filecontent, file) WHERE (CONTAINS(filecontent.*, 'ACME') OR CONTAINS(press.*, 'ACME')) AND press.[jcr:language] = 'en' ORDER BY press.date DESC", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
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
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (session.nodeExists(SITECONTENT_ROOT_NODE)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }
            session.save();

            session.logout();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

}