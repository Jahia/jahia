/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.query;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
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
    private static Logger logger = Logger.getLogger(QueryModificationTest.class);

    private final static String TESTSITE_NAME = "jcrQueryModificationTest";

    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(TESTSITE_NAME, "localhost", TestHelper.ACME_TEMPLATES,
                                null);
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
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:title_en]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());

                query = "SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:title_en]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:title_en]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT * FROM [jnt:news] as news WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) ORDER BY news.[jcr:createdBy], news.[jcr:title], news.[jcr:created] desc";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:createdBy], newstranslationAdded.[jcr:title_en], newstranslationAdded.[jcr:created] DESC", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:createdBy], newstranslationAdded.[jcr:title_en], newstranslationAdded.[jcr:created] DESC";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:createdBy], newstranslationAdded.[jcr:title_en], newstranslationAdded.[jcr:created] DESC", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
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
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (LENGTH(newstranslationAdded.desc_en) > 100 OR newstranslationAdded.date > '+2001-01-01T01:02:03.000Z') AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:title_en]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (LENGTH(newstranslationAdded.desc_en) > 100 OR newstranslationAdded.date > '+2001-01-01T01:02:03.000Z') AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:title_en]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISDESCENDANTNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND (LENGTH(newstranslationAdded.desc_en) > 100 OR newstranslationAdded.date > '+2001-01-01T01:02:03.000Z') AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:title_en]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
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
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE CONTAINS(newstranslationAdded.*, 'ACME') AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE CONTAINS(newstranslationAdded.*, 'ACME') AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.date";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE CONTAINS(newstranslationAdded.*, 'ACME') AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
                
                query = "SELECT * FROM [jnt:news] as news WHERE contains(news.[jcr:title], 'ACME') ORDER BY news.[date]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE CONTAINS(newstranslationAdded.[jcr:title_en], 'ACME') AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
                
                query = "SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE CONTAINS(newstranslationAdded.[jcr:title_en], 'ACME') AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.date";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE CONTAINS(newstranslationAdded.[jcr:title_en], 'ACME') AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT * FROM [jnt:news] as news WHERE contains(news.[jcr:title], 'ACME') OR contains(news.[j:keywords], 'ACME') ORDER BY news.[date]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE (CONTAINS(newstranslationAdded.[jcr:title_en], 'ACME') OR CONTAINS(newstranslationAdded.[j:keywords], 'ACME')) AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
                
                query = "SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE (CONTAINS(newstranslationAdded.[jcr:title_en], 'ACME') OR CONTAINS(newstranslationAdded.[j:keywords], 'ACME')) AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.date";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE (CONTAINS(newstranslationAdded.[jcr:title_en], 'ACME') OR CONTAINS(newstranslationAdded.[j:keywords], 'ACME')) AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.date", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT * FROM [jmix:keywords] as node WHERE contains(node.[j:keywords], 'ACME')";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT node.* FROM [jmix:keywords] AS node WHERE CONTAINS(node.[j:keywords], 'ACME')", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                

                query = "SELECT * FROM [jmix:keywords] as node WHERE contains(node.*, 'ACME')";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT node.* FROM [jmix:keywords] AS node WHERE CONTAINS(node.*, 'ACME')", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                                
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
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND newstranslationAdded.[jcr:language] = 'fr' ORDER BY newstranslationAdded.[jcr:title_fr]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());

                query = "SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND newstranslationAdded.[jcr:language] = 'fr' ORDER BY newstranslationAdded.[jcr:title_fr]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.* FROM [jnt:news] AS news INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "/home/page8/news]) AND newstranslationAdded.[jcr:language] = 'fr' ORDER BY newstranslationAdded.[jcr:title_fr]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
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
                assertEquals("SELECT news.*, tags.* FROM [jnt:news] AS news INNER JOIN [jmix:tagged] AS tags ON newstranslationAdded.[j.tags] = tags.[jcr:uuid] INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND LOCALNAME(tags) = 'test' AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:title_en]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT news.*, tags.* FROM [jnt:news] AS news INNER JOIN [jmix:tagged] AS tags ON newstranslationAdded.[j.tags] = tags.[jcr:uuid] INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND LOCALNAME(tags) = 'test' AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:title_en]";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT news.*, tags.* FROM [jnt:news] AS news INNER JOIN [jmix:tagged] AS tags ON newstranslationAdded.[j.tags] = tags.[jcr:uuid] INNER JOIN [jnt:translation] AS newstranslationAdded ON ISCHILDNODE(newstranslationAdded, news) WHERE ISCHILDNODE(news, [" + SITECONTENT_ROOT_NODE + "]) AND LOCALNAME(tags) = 'test' AND newstranslationAdded.[jcr:language] = 'en' ORDER BY newstranslationAdded.[jcr:title_en]", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
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
                assertEquals("SELECT press.* FROM [jnt:press] AS press LEFT OUTER JOIN [nt:file] AS file ON presstranslationAdded.pdfVersion_en = file.[jcr:uuid] INNER JOIN [nt:resource] AS filecontent ON ISCHILDNODE(filecontent, file) INNER JOIN [jnt:translation] AS presstranslationAdded ON ISCHILDNODE(presstranslationAdded, press) WHERE (CONTAINS(filecontent.*, 'ACME') OR CONTAINS(presstranslationAdded.*, 'ACME')) AND presstranslationAdded.[jcr:language] = 'en' ORDER BY presstranslationAdded.date DESC", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());
                
                query = "SELECT press.* FROM [jnt:press] AS press LEFT OUTER JOIN [nt:file] AS file ON presstranslationAdded.pdfVersion_en = file.[jcr:uuid] INNER JOIN [nt:resource] AS filecontent ON ISCHILDNODE(filecontent, file) INNER JOIN [jnt:translation] AS presstranslationAdded ON ISCHILDNODE(presstranslationAdded, press) WHERE (CONTAINS(filecontent.*, 'ACME') OR CONTAINS(presstranslationAdded.*, 'ACME')) AND presstranslationAdded.[jcr:language] = 'en' ORDER BY presstranslationAdded.date DESC";
                q = queryManager.createQuery(query, Query.JCR_SQL2);
                assertEquals("SELECT press.* FROM [jnt:press] AS press LEFT OUTER JOIN [nt:file] AS file ON presstranslationAdded.pdfVersion_en = file.[jcr:uuid] INNER JOIN [nt:resource] AS filecontent ON ISCHILDNODE(filecontent, file) INNER JOIN [jnt:translation] AS presstranslationAdded ON ISCHILDNODE(presstranslationAdded, press) WHERE (CONTAINS(filecontent.*, 'ACME') OR CONTAINS(presstranslationAdded.*, 'ACME')) AND presstranslationAdded.[jcr:language] = 'en' ORDER BY presstranslationAdded.date DESC", ((QueryWrapper)q).getQueries().values().iterator().next().getStatement());                
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