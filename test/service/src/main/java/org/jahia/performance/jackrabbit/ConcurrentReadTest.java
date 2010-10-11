/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.performance.jackrabbit;

import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.springframework.util.StopWatch;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 8 oct. 2010
 */
public class ConcurrentReadTest extends TestCase {
    private transient static Logger logger = Logger.getLogger(ConcurrentReadTest.class);
    private final static String TESTSITE_NAME = "jcrConcurrentReadTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String INITIAL_ENGLISH_TEXT_NODE_PROPERTY_VALUE = "English text";
//    private static final String INITIAL_ENGLISH_SHARED_TEXT_NODE_PROPERTY_VALUE = "English shared text";
    public static final int NB_CHILDREN = 10;

    protected void setUp() throws Exception {
        try {
            JahiaSite site = TestHelper.createSite(TESTSITE_NAME);
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start(Thread.currentThread().getName() + " creating set up nodes");
                    TestHelper.createSubPages(session.getNode(SITECONTENT_ROOT_NODE), 3, NB_CHILDREN);
                    session.save();
                    stopWatch.stop();
                    logger.fatal(stopWatch.prettyPrint());
                    return null;
                }
            });
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            fail();
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

    private class Reader implements Runnable {
        private final Random random = new Random();
        StopWatch stopWatch;
        private JCRTemplate jcrTemplate;

        public Reader() {
            stopWatch = new StopWatch();
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
//                logger.fatal(stopWatch.prettyPrint());
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
            stopWatch = new StopWatch();
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
//                logger.fatal(stopWatch.prettyPrint());
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static final int CHILD_COUNT = 1000;

    public void testCreateManyChildUnstructuredNodes() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start(Thread.currentThread().getName() + " creating unstructured nodes");
                JCRNodeWrapper currentNode = session.getNode(SITECONTENT_ROOT_NODE);
                Node node = currentNode.addNode("testnode", "nt:unstructured");
                for (int i = 0; i < CHILD_COUNT; i++) {
                    node.addNode("node" + i, "nt:unstructured");
                }
                session.save();
                stopWatch.stop();
                logger.fatal(stopWatch.prettyPrint());
                return null;
            }
        });
    }

    public void testCreateManyChildPageNodes() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start(Thread.currentThread().getName() + " creating page nodes");
                JCRNodeWrapper currentNode = session.getNode(SITECONTENT_ROOT_NODE);
                Node node = currentNode.addNode("testnode", "jnt:page");
                for (int i = 0; i < CHILD_COUNT; i++) {
                    node.addNode("child" + Integer.toString(i), "jnt:page");
                }
                session.save();
                stopWatch.stop();
                logger.fatal(stopWatch.prettyPrint());
                return null;
            }
        });
    }

    public void testConcurrentRead() throws InterruptedException, ExecutionException {
        Executor executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start(Thread.currentThread().getName() + " only reading nodes");
        for (int i = 0; i < 1000; i++) {
            service.submit(new Reader(), Boolean.TRUE);
        }
        for (int i = 0; i < 1000; i++) {
            Boolean aBoolean = service.take().get();
        }
        stopWatch.stop();
        logger.fatal(stopWatch.prettyPrint());
    }

    public void testConcurrentReadWrite10Percent() throws InterruptedException, ExecutionException {
        Executor executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch();
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
        logger.fatal(stopWatch.prettyPrint());
    }

    public void testConcurrentReadWrite1Percent() throws InterruptedException, ExecutionException {
        Executor executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch();
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
        logger.fatal(stopWatch.prettyPrint());
    }

    public void testConcurrentReadWrite10PercentUnstructured() throws InterruptedException, ExecutionException {
        Executor executor = Executors.newFixedThreadPool(300);
        ExecutorCompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
        StopWatch stopWatch = new StopWatch();
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
        logger.fatal(stopWatch.prettyPrint());
    }
}
