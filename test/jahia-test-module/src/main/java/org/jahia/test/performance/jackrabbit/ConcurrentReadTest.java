/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.performance.jackrabbit;

import org.slf4j.Logger;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.StopWatch;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 8 oct. 2010
 */
public class ConcurrentReadTest {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ConcurrentReadTest.class);
    private final static String TESTSITE_NAME = "jcrConcurrentReadTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";
//    private static final String INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE = "English shared text";
    public static final int NB_CHILDREN = 10;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    StopWatch stopWatch = new StopWatch("oneTimeSetUp");
                    stopWatch.start(Thread.currentThread().getName() + " creating set up nodes");
                    int pagesCreated = TestHelper.createSubPages(session.getNode(SITECONTENT_ROOT_NODE), 3, NB_CHILDREN);
                    logger.info("Created " + pagesCreated + " page hierarchy.");
                    session.save();
                    stopWatch.stop();
                    logger.error(stopWatch.prettyPrint());
                    return null;
                }
            });
            Assert.assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            Assert.fail();
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

    private class Reader implements Runnable {
        private final Random random = new Random();
        StopWatch stopWatch;
        private JCRTemplate jcrTemplate;

        public Reader() {
            stopWatch = new StopWatch("ReaderRunnable");
            stopWatch.start(Thread.currentThread().getName() + " reading node");
            jcrTemplate = JCRTemplate.getInstance();
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        public void run() {
            try {
                jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        int i = random.nextInt(NB_CHILDREN);
                        int j = random.nextInt(NB_CHILDREN);
                        int k = random.nextInt(NB_CHILDREN);
                        session.getNode(SITECONTENT_ROOT_NODE).getNode("child" + i).getNode("child" + j).getNode(
                                "child" + k);
                        return null;
                    }
                });
                stopWatch.stop();
//                logger.error(stopWatch.prettyPrint());
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private class Writer implements Runnable {
        private final Random random = new Random();
        StopWatch stopWatch;
        private JCRTemplate jcrTemplate;
        private String primaryNodeTypeName;

        public Writer(String primaryNodeTypeName) {
            stopWatch = new StopWatch("WriterRunnable");
            jcrTemplate = JCRTemplate.getInstance();
            this.primaryNodeTypeName = primaryNodeTypeName;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        public void run() {
            try {
                stopWatch.start(Thread.currentThread().getName() + " creating node");
                jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        int i = random.nextInt(NB_CHILDREN);
                        int j = random.nextInt(NB_CHILDREN);
                        int k = random.nextInt(NB_CHILDREN);
                        JCRNodeWrapper node = session.getNode(SITECONTENT_ROOT_NODE).getNode("child" + i).getNode(
                                "child" + j).getNode("child" + k);
                        node.addNode("child" + random.nextLong(), primaryNodeTypeName);
                        session.save();
                        return null;
                    }
                });
                stopWatch.stop();
//                logger.error(stopWatch.prettyPrint());
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private class Search implements Runnable {
        StopWatch stopWatch;
        private JCRTemplate jcrTemplate;
        private String testQuery;

        public Search(String s) {
            testQuery = s;
            stopWatch = new StopWatch("SearchRunnable");
            jcrTemplate = JCRTemplate.getInstance();
        }

        public void run() {
            try {
                stopWatch.start(Thread.currentThread().getName() + " searching node");
                jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        testQuery = testQuery.replace("path", session.getNode(SITECONTENT_ROOT_NODE).getPath());
                        Query query = session.getWorkspace().getQueryManager().createQuery(testQuery, Query.JCR_SQL2);
                        query.execute();
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private class SearchIteratorResults implements Runnable {
        StopWatch stopWatch;
        private JCRTemplate jcrTemplate;
        private String testQuery;

        public SearchIteratorResults(String s) {
            testQuery = s;
            stopWatch = new StopWatch("SearchIteratorResultsRunnable");
            jcrTemplate = JCRTemplate.getInstance();
        }

        public void run() {
            try {
                stopWatch.start(Thread.currentThread().getName() + " searching node");
                jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        testQuery = testQuery.replace("path", session.getNode(SITECONTENT_ROOT_NODE).getPath());
                        Query query = session.getWorkspace().getQueryManager().createQuery(testQuery, Query.JCR_SQL2);
                        QueryResult queryResult = query.execute();
                        if (queryResult.getNodes().getSize() == -1) {
                            NodeIterator nodeIterator = queryResult.getNodes();
                            nodeIterator.nextNode();
                        }
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static final int CHILD_COUNT = 1000;

    @Test
    public void testCreateManyChildUnstructuredNodes() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                StopWatch stopWatch = new StopWatch("testCreateManyChildUnstructuredNodes");
                stopWatch.start(Thread.currentThread().getName() + " creating unstructured nodes");
                JCRNodeWrapper currentNode = session.getNode(SITECONTENT_ROOT_NODE);
                Node node = currentNode.addNode("testnodeUnstructured", "nt:unstructured");
                for (int i = 0; i < CHILD_COUNT; i++) {
                    node.addNode("node" + i, "nt:unstructured");
                }
                session.save();
                stopWatch.stop();
                logger.error(stopWatch.prettyPrint());
                return null;
            }
        });
    }

    @Test
    public void testCreateManyChildPageNodes() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                StopWatch stopWatch = new StopWatch("testCreateManyChildPageNodes");
                stopWatch.start(Thread.currentThread().getName() + " creating page nodes");
                JCRNodeWrapper currentNode = session.getNode(SITECONTENT_ROOT_NODE);
                Node node = currentNode.addNode("testPageNode", "jnt:page");
                for (int i = 0; i < CHILD_COUNT; i++) {
                    node.addNode("child" + Integer.toString(i), "jnt:page");
                }
                session.save();
                stopWatch.stop();
                logger.error(stopWatch.prettyPrint());
                return null;
            }
        });
    }

    @Test
    public void testConcurrentRead() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch("testConcurrentRead");
        stopWatch.start(Thread.currentThread().getName() + " only reading nodes");
        for (int i = 0; i < 1000; i++) {
            service.submit(new Reader(), Boolean.TRUE);
        }
        for (int i = 0; i < 1000; i++) {
            Boolean aBoolean = service.take().get();
        }
        stopWatch.stop();
        logger.error(stopWatch.prettyPrint());
        executor.shutdown();
    }

    @Test
    public void testConcurrentReadWrite10Percent() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch("testConcurrentReadWrite10Percent");
        stopWatch.start(Thread.currentThread().getName() + " reading // writing nodes (10% writers)");
        for (int i = 0; i < 1000; i++) {
            if (i % 10 == 0) {
                service.submit(new Writer("jnt:page"), Boolean.TRUE);
            } else {
                service.submit(new Reader(), Boolean.TRUE);
            }
        }
        for (int i = 0; i < 1000; i++) {
            Boolean aBoolean = service.take().get();
        }
        stopWatch.stop();
        logger.error(stopWatch.prettyPrint());
        executor.shutdown();
    }

    @Test
    public void testConcurrentReadWrite1Percent() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch("testConcurrentReadWrite1Percent");
        stopWatch.start(Thread.currentThread().getName() + " reading // writing nodes (1% writers)");
        for (int i = 0; i < 1000; i++) {
            if (i % 100 == 0) {
                service.submit(new Writer("jnt:page"), Boolean.TRUE);
            } else {
                service.submit(new Reader(), Boolean.TRUE);
            }
        }
        for (int i = 0; i < 1000; i++) {
            Boolean aBoolean = service.take().get();
        }
        stopWatch.stop();
        logger.error(stopWatch.prettyPrint());
        executor.shutdown();
    }

    @Test
    public void testConcurrentReadWrite10PercentUnstructured() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch("testConcurrentReadWrite10PercentUnstructured");
        stopWatch.start(Thread.currentThread().getName() + " reading // writing nodes (10% writers)");
        for (int i = 0; i < 1000; i++) {
            if (i % 10 == 0) {
                service.submit(new Writer("nt:unstructured"), Boolean.TRUE);
            } else {
                service.submit(new Reader(), Boolean.TRUE);
            }
        }
        for (int i = 0; i < 1000; i++) {
            Boolean aBoolean = service.take().get();
        }
        stopWatch.stop();
        logger.error(stopWatch.prettyPrint());
        executor.shutdown();
    }

    @Test
    public void testConcurrentSearchIsDescendant() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch("testConcurrentSearchIsDescendant");
        stopWatch.start(Thread.currentThread().getName() + " search jnt:pages nodes is descendant site node");
        for (int i = 0; i < 1000; i++) {
            service.submit(new Search("select * from [jnt:page] as page where isdescendantnode(page, 'path') "), Boolean.TRUE);
        }
        for (int i = 0; i < 1000; i++) {
            Boolean aBoolean = service.take().get();
        }
        stopWatch.stop();
        logger.error(stopWatch.prettyPrint());
        executor.shutdown();
    }

    @Test
    public void testConcurrentSearchIsChild() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch("testConcurrentSearchIsChild");
        stopWatch.start(Thread.currentThread().getName() + " search jnt:pages nodes is child site node");
        for (int i = 0; i < 1000; i++) {
            service.submit(new Search("select * from [jnt:page] as page where ischildnode(page, 'path') "), Boolean.TRUE);
        }
        for (int i = 0; i < 1000; i++) {
            Boolean aBoolean = service.take().get();
        }
        stopWatch.stop();
        logger.error(stopWatch.prettyPrint());
        executor.shutdown();
    }

    @Test
    public void testConcurrentSearchIsDescendantAndIteratorResults() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch("testConcurrentSearchIsDescendantAndIteratorResults");
        stopWatch.start(Thread.currentThread().getName() + " search jnt:page nodes is descendant site node, iterator results for query results size = -1 ");
        for (int i = 0; i < 1000; i++) {
            service.submit(new SearchIteratorResults("select * from [jnt:page] as page where isdescendantnode(page, 'path') "), Boolean.TRUE);
        }
        for (int i = 0; i < 1000; i++) {
            Boolean aBoolean = service.take().get();
        }
        stopWatch.stop();
        logger.error(stopWatch.prettyPrint());
        executor.shutdown();
    }

    @Test
    public void testConcurrentSearchIsCreatedBeforeNow() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch("testConcurrentSearchIsCreatedBeforeNow");
        stopWatch.start(Thread.currentThread().getName() + " search jnt:page nodes is created before now");
        for (int i = 0; i < 1000; i++) {
            service.submit(new Search("select * from [jnt:page] as page where page.[jcr:created] < CAST(" + System.currentTimeMillis() + " as date) "), Boolean.TRUE);
        }
        for (int i = 0; i < 1000; i++) {
            Boolean aBoolean = service.take().get();
        }
        stopWatch.stop();
        logger.error(stopWatch.prettyPrint());
        executor.shutdown();
    }

    @Test
    public void testConcurrentSearchIsCreatedBeforeNowAndIsDescendant() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch("testConcurrentSearchIsCreatedBeforeNowAndIsDescendant");
        stopWatch.start(Thread.currentThread().getName() + " search jnt:page nodes is created before now and is descendant site node");
        for (int i = 0; i < 1000; i++) {
            service.submit(new Search("select * from [jnt:page] as page where isdescendantnode(page, 'path') and page.[jcr:created] < CAST(" + System.currentTimeMillis() + " as date) "), Boolean.TRUE);
        }
        for (int i = 0; i < 1000; i++) {
            Boolean aBoolean = service.take().get();
        }
        stopWatch.stop();
        logger.error(stopWatch.prettyPrint());
        executor.shutdown();
    }
}
