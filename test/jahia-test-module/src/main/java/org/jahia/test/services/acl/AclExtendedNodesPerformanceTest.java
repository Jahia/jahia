package org.jahia.test.services.acl;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.PerformanceTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This test collects performance metrics of CRD operations performed on folder/file sub-nodes of multiple LDAP user nodes.
 * <p>
 * The test relies on LDAP user/group provider pre-configured in the DX instance. The provider must be able to supply for users named "user0" - "user${NUM_USERS - 1}".
 */
public class AclExtendedNodesPerformanceTest {

    private static final String USER_GROUP_PROVIDER = "ldap";

    private static final String NODE_NAME = AclExtendedNodesPerformanceTest.class.getSimpleName() + "%s";
    private static final Pattern NODE_NAME_PATTERN = Pattern.compile(AclExtendedNodesPerformanceTest.class.getSimpleName() + "\\d+");

    private static final int USER_FOLDER_TREE_DEPTH = 3;
    private static final int MAX_USER_FOLDERS_PER_LEVEL = 2;
    private static final int MAX_FILES_PER_FOLDER_SINGLE_USER = 10;
    private static final int MAX_FILES_PER_FOLDER_MULTIPLE_USERS = 1;
    private static final String FILE_CONTENT_TYPE = "text/plain";
    private static final String FILE_CONTENT = "Test file";
    private static final String FILE_ENCODING = "UTF-8";

    private static final int NUM_USERS = 100;
    private static final String USER_NAME = "user%s";

    private JahiaUserManagerService userManagerService;

    @Before
    public void setUp() throws Exception {
        userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
    }

    @Test
    public void testCrdBySingleUser() throws Exception {

        JCRUserNode user = getUser(0);
        final LinkedList<Long> createFolderSamples = new LinkedList<Long>();
        final LinkedList<Long> uploadFileSamples = new LinkedList<Long>();
        final LinkedList<Long> downloadFileSamples = new LinkedList<Long>();
        final LinkedList<Long> removeSamples = new LinkedList<Long>();
        final LinkedList<Long> listSamples = new LinkedList<Long>();

        try {

            System.out.println("Creating folders by single user...");
            createFolders(user, createFolderSamples);

            System.out.println("Uploading files by single user...");
            uploadFiles(user, MAX_FILES_PER_FOLDER_SINGLE_USER, listSamples, uploadFileSamples);

            System.out.println("Downloading files by single user...");
            downloadFiles(user, user, listSamples, downloadFileSamples);
        } finally {

            System.out.println("Removing folders/files by single user...");
            removeNodes(user, listSamples, removeSamples);
        }

        printStatistics("Creating folders by single user", createFolderSamples);
        printStatistics("Uploading files by single user", uploadFileSamples);
        printStatistics("Downloading files by single user", downloadFileSamples);
        printStatistics("Removing folders/files by single user", removeSamples);
        printStatistics("Listing sub-nodes by single user", listSamples);
    }

    @Test
    public void testCrdByMultipleUsers() throws Exception {

        final LinkedList<Long> createFolderSamples = new LinkedList<Long>();
        final LinkedList<Long> uploadFileSamples = new LinkedList<Long>();
        final LinkedList<Long> downloadFileSamples = new LinkedList<Long>();
        final LinkedList<Long> removeSamples = new LinkedList<Long>();
        final LinkedList<Long> listSamples = new LinkedList<Long>();

        try {

            System.out.println("Creating folders by multiple users...");
            for (int i = 0; i < NUM_USERS; i++) {
                JCRUserNode user = getUser(i);
                createFolders(user, createFolderSamples);
            }

            System.out.println("Uploading files by multiple users...");
            for (int i = 0; i < NUM_USERS; i++) {
                JCRUserNode user = getUser(i);
                uploadFiles(user, MAX_FILES_PER_FOLDER_MULTIPLE_USERS, listSamples, uploadFileSamples);
            }

            System.out.println("Downloading files by multiple users...");
            for (int i = 0; i < NUM_USERS; i++) {
                JCRUserNode readerUser = getUser(i);
                JCRUserNode ownerUser = getUser(ThreadLocalRandom.current().nextInt(NUM_USERS));
                downloadFiles(readerUser, ownerUser, listSamples, downloadFileSamples);
            }
        } finally {

            System.out.println("Removing folders/files by multiple users...");
            for (int i = 0; i < NUM_USERS; i++) {
                JCRUserNode user = getUser(i);
                removeNodes(user, listSamples, removeSamples);
            }
        }

        printStatistics("Creating folders by multiple users", createFolderSamples);
        printStatistics("Uploading files by multiple users", uploadFileSamples);
        printStatistics("Downloading files by multiple users", downloadFileSamples);
        printStatistics("Removing folders/files by multiple users", removeSamples);
        printStatistics("Listing sub-nodes by multiple users", listSamples);
    }

    private JCRUserNode getUser(int index) {
        return getUser(String.format(USER_NAME, index));
    }

    private JCRUserNode getUser(String userName) {

        JCRUserNode user = userManagerService.lookup(userName);
        Assert.assertNotNull(user);
        Assert.assertEquals(USER_GROUP_PROVIDER, user.getPropertyAsString(JCRUserNode.J_EXTERNAL_SOURCE)); // Ensure the user is retrieved from desired provider.

        // Work around the https://jira.jahia.org/browse/BACKLOG-6053 issue.
        // TODO: Remove entirely once BACKLOG-6053 is resolved.
        try {
            user.setProperty("j:skypeID", "test");
            user.getSession().save();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

        return user;
    }

    private void createFolders(final JCRUserNode user, final Collection<Long> samples) {

        try {

            JCRTemplate.getInstance().doExecute(user.getJahiaUser(), Constants.EDIT_WORKSPACE, Locale.ENGLISH, new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    createSubFolders(user, USER_FOLDER_TREE_DEPTH, samples);
                    return null;
                }
            });
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private void createSubFolders(JCRNodeWrapper node, int numLevelsToCreate, final Collection<Long> samples) {

        if (numLevelsToCreate == 0) {
            return;
        }

        int numFolders = ThreadLocalRandom.current().nextInt(MAX_USER_FOLDERS_PER_LEVEL) + 1;
        for (int i = 0; i < numFolders; i++) {
            String folderName = String.format(NODE_NAME, System.currentTimeMillis());
            JCRNodeWrapper folder;
            long start = System.currentTimeMillis();
            try {
                folder = node.addNode(folderName, Constants.JAHIANT_FOLDER);
                node.getSession().save();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
            samples.add(System.currentTimeMillis() - start);
            createSubFolders(folder, numLevelsToCreate - 1, samples);
        }
    }

    private void uploadFiles(final JCRUserNode user, final int maxFilesPerFolder, final Collection<Long> listSamples, final Collection<Long> uploadSamples) {

        byte[] bytes;
        try {
            bytes = FILE_CONTENT.getBytes(FILE_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        final ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        try {

            JCRTemplate.getInstance().doExecute(user.getJahiaUser(), Constants.EDIT_WORKSPACE, Locale.ENGLISH, new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                    processSubNodes(user, listSamples, new NodeProcessor() {

                        @Override
                        public void process(JCRNodeWrapper node) {
                            try {
                                if (!node.isNodeType(Constants.JAHIANT_FOLDER)) {
                                    return;
                                }
                                int numFiles = Math.round(maxFilesPerFolder * ThreadLocalRandom.current().nextFloat());
                                for (int i = 0; i < numFiles; i++) {
                                    String fileName = String.format(NODE_NAME, System.currentTimeMillis());
                                    in.reset();
                                    long start = System.currentTimeMillis();
                                    node.uploadFile(fileName, in, FILE_CONTENT_TYPE);
                                    node.getSession().save();
                                    uploadSamples.add(System.currentTimeMillis() - start);
                                }
                            } catch (RepositoryException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    return null;
                }
            });
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadFiles(JCRUserNode downloaderUser, final JCRUserNode ownerUser, final Collection<Long> listSamples, final Collection<Long> downloadSamples) {

        try {

            JCRTemplate.getInstance().doExecute(downloaderUser.getJahiaUser(), Constants.EDIT_WORKSPACE, Locale.ENGLISH, new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                    processSubNodes(ownerUser, listSamples, new NodeProcessor() {

                        @Override
                        public void process(JCRNodeWrapper node) {
                            if (!node.isFile()) {
                                return;
                            }
                            long start = System.currentTimeMillis();
                            node.getFileContent().downloadFile();
                            downloadSamples.add(System.currentTimeMillis() - start);
                        }
                    });
                    return null;
                }
            });
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeNodes(JCRUserNode user, final Collection<Long> listSamples, final Collection<Long> removeSamples) {

        try {
            JCRTemplate.getInstance().doExecute(user.getJahiaUser(), Constants.EDIT_WORKSPACE, Locale.ENGLISH, new JCRCallback<Object>() {

                @Override
                public Object doInJCR(final JCRSessionWrapper session) throws RepositoryException {

                    processSubNodes(session.getUserNode(), listSamples, new NodeProcessor() {

                        @Override
                        public void process(JCRNodeWrapper node) {
                            long start = System.currentTimeMillis();
                            try {
                                node.remove();
                                session.save();
                            } catch (RepositoryException e) {
                                throw new RuntimeException(e);
                            }
                            removeSamples.add(System.currentTimeMillis() - start);
                        }
                    });
                    return null;
                }
            });
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private void processSubNodes(JCRNodeWrapper node, final Collection<Long> listSamples, final NodeProcessor nodeProcessor) {

        Iterable<JCRNodeWrapper> subNodes;
        long start = System.currentTimeMillis();
        try {
            subNodes = node.getNodes();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        listSamples.add(System.currentTimeMillis() - start);

        for (JCRNodeWrapper subNode : subNodes) {

            if (!NODE_NAME_PATTERN.matcher(subNode.getName()).matches()) {
                continue;
            }

            // Process sub-nodes before the node itself, so that the processor could delete sub-nodes before deleting the node.
            processSubNodes(subNode, listSamples, nodeProcessor);
            nodeProcessor.process(subNode);
        }
    }

    private interface NodeProcessor {

        void process(JCRNodeWrapper node);
    }

    private static void printStatistics(String title, List<Long> samples) {
        PerformanceTestUtils.TimingStatistics statistics = PerformanceTestUtils.getTimingStatistics(samples);
        System.out.println("******************************************************************");
        System.out.println(title);
        System.out.println("******************************************************************");
        System.out.println("Samples:              " + statistics.getNumSamples());
        System.out.println("Min time (ms):        " + statistics.getMin());
        System.out.println("Max time (ms):        " + statistics.getMax());
        System.out.println("Avg time (ms):        " + statistics.getAvg());
        System.out.println("50th percentile (ms): " + statistics.getPercentile(50));
        System.out.println("90th percentile (ms): " + statistics.getPercentile(90));
        System.out.println("******************************************************************");
    }
}
