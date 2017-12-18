/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.test.services.acl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.PerformanceTestUtils;
import org.jahia.test.PerformanceTestUtils.TimingStatistics;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * This test collects performance metrics of CRD operations performed on folder/file sub-nodes of multiple LDAP user nodes.
 * <p>
 * The test relies on LDAP user/group provider pre-configured in the DX instance. The provider must be able to supply for users named "user0" - "user${NUM_USERS - 1}".
 */
public class AclExtendedNodesPerformanceTest {

    // Be careful updating any user/file numbers used to access files after creation.
    // Many of the file access tests pick random users/files, while keeping track of elements picked during the same test before and rejecting such recurring users/files.
    // These tests therefore may hung in endless loop trying to pick a user/file that hasn't been used yet in case there is not sufficient total number of users/files
    // and thus each of the users/files has already been used.

    // Common user constants.
    private static final String USER_GROUP_PROVIDER = "ldap";
    private static final String USER_NAME = "user%s";
    private static final int NUM_USERS = 100;

    // Common folder/file constants.
    private static final String FOLDER_FILE_NAME = AclExtendedNodesPerformanceTest.class.getSimpleName() + "%s";
    private static final Pattern FOLDER_FILE_NAME_PATTERN = Pattern.compile(AclExtendedNodesPerformanceTest.class.getSimpleName() + "\\d+");
    private static final int USER_FOLDER_TREE_DEPTH = 3;
    private static final int NUM_USER_FOLDERS_PER_LEVEL = 2;
    private static final String FILE_CONTENT_TYPE = "text/plain";
    private static final String FILE_CONTENT = "Test file";
    private static final String FILE_ENCODING = "UTF-8";

    // Single user test constants.
    private static final int NUM_FILES_PER_FOLDER_SINGLE_USER = 10;
    private static final int NUM_ITERATIONS_TOUCHING_FILES_BY_SINGLE_USER = 50;
    private static final int NUM_FILES_TO_TOUCH_BY_SINGLE_USER = 20;
    private static final int NUM_FILES_TO_DOWNLOAD_BY_SINGLE_USER = 100;

    // Multiple users test constants.
    private static final int NUM_FILES_PER_FOLDER_MULTIPLE_USERS = 5;
    private static final int NUM_OWNER_USERS = 10;
    private static final int NUM_TOUCHING_USERS = 50;
    private static final int NUM_FILES_TO_TOUCH_PER_OWNER = 2;
    private static final int NUM_DOWNLOADER_USERS = 25;
    private static final int NUM_FILES_TO_DOWNLOAD_PER_DOWNLOADER_PER_OWNER = 2;

    // Statistics output.
    private enum Measurement {

        NUM_SAMPLES("samples") {

            @Override
            protected long extractValue(TimingStatistics statistics) {
                return statistics.getNumSamples();
            }
        },
        MIN("min") {

            @Override
            protected long extractValue(TimingStatistics statistics) {
                return statistics.getMin();
            }
        },
        MAX("max") {

            @Override
            protected long extractValue(TimingStatistics statistics) {
                return statistics.getMax();
            }
        },
        AVG("avg") {

            @Override
            protected long extractValue(TimingStatistics statistics) {
                return statistics.getAvg();
            }
        },
        PERCENTILE_50("50th pc") {

            @Override
            protected long extractValue(TimingStatistics statistics) {
                return statistics.getPercentile(50);
            }
        },
        PERCENTILE_90("90th pc") {

            @Override
            protected long extractValue(TimingStatistics statistics) {
                return statistics.getPercentile(90);
            }
        };

        private String title;

        private Measurement(String title) {
            this.title = title;
        }

        abstract protected long extractValue(PerformanceTestUtils.TimingStatistics statistics);

        public String getTitle() {
            return title;
        }
    }

    private JahiaUserManagerService userManagerService;

    @Before
    public void setUp() throws Exception {
        userManagerService = ServicesRegistry.getInstance().getJahiaUserManagerService();
    }

    @Test
    public void testCrdBySingleUser() throws Exception {

        List<Long> createFolderSamples = new LinkedList<Long>();
        List<Long> uploadFileSamples = new LinkedList<Long>();
        List<Long> touchFileSamplesDefault = new LinkedList<Long>();
        List<Long> downloadFileSamplesDefault = new LinkedList<Long>();
        List<Long> touchFileSamplesLive = new LinkedList<Long>();
        List<Long> downloadFileSamplesLive = new LinkedList<Long>();
        List<Long> listSamples = new LinkedList<Long>();
        List<Long> removeSamples = new LinkedList<Long>();

        JCRUserNode user = getUser(0);

        try {

            System.out.println("Creating folders/files by single user...");
            List<String> filePaths = new ArrayList<String>(createFoldersAndFiles(user, NUM_FILES_PER_FOLDER_SINGLE_USER, createFolderSamples, uploadFileSamples));

            System.out.println("Accessing files by single user in default workspace...");
            touchRandomFiles(Constants.EDIT_WORKSPACE, user, filePaths, touchFileSamplesDefault);

            System.out.println("Downloading files by single user from default workspace...");
            downloadRandomFiles(Constants.EDIT_WORKSPACE, user, filePaths, downloadFileSamplesDefault);

            // There is no publication concept for external content and its extensions.
            // Any external content appears in the live workspace without a need to publish, so can be read.

            System.out.println("Accessing files by single user in live workspace...");
            touchRandomFiles(Constants.LIVE_WORKSPACE, user, filePaths, touchFileSamplesLive);

            System.out.println("Downloading files by single user from live workspace...");
            downloadRandomFiles(Constants.LIVE_WORKSPACE, user, filePaths, downloadFileSamplesLive);
        } finally {

            System.out.println("Removing folders/files by single user...");
            removeFoldersAndFiles(user, listSamples, removeSamples);
        }

        printStatistics("Single user CRD test", new OperationInfo[] {
            new OperationInfo("Creating folders/files", createFolderSamples),
            new OperationInfo("Uploading files", uploadFileSamples),
            new OperationInfo("Accessing files (default)", touchFileSamplesDefault),
            new OperationInfo("Downloading files (default)", downloadFileSamplesDefault),
            new OperationInfo("Accessing files (live)", touchFileSamplesLive),
            new OperationInfo("Downloading files (live)", downloadFileSamplesLive),
            new OperationInfo("Listing sub-nodes", listSamples),
            new OperationInfo("Removing folders/files", removeSamples),
        });
    }

    @Test
    public void testCrdByMultipleUsers() throws Exception {

        List<Long> createFolderSamples = new LinkedList<Long>();
        List<Long> uploadFileSamples = new LinkedList<Long>();
        List<Long> touchFileSamplesLive = new LinkedList<Long>();
        List<Long> downloadFileSamplesLive = new LinkedList<Long>();
        List<Long> listSamples = new LinkedList<Long>();
        List<Long> removeSamples = new LinkedList<Long>();

        try {

            System.out.println("Creating folders/files by multiple users...");
            ListMultimap<Integer, String> filePathsByOwner = ArrayListMultimap.create();
            for (int i = 0; i < NUM_USERS; i++) {
                JCRUserNode owner = getUser(i);
                List<String> filePaths = new ArrayList<String>(createFoldersAndFiles(owner, NUM_FILES_PER_FOLDER_MULTIPLE_USERS, createFolderSamples, uploadFileSamples));
                filePathsByOwner.putAll(i, filePaths);
            }

            // There is no publication concept for external content and its extensions.
            // Any external content appears in the live workspace without a need to publish, so can be read.

            System.out.println("Accessing files by multiple users in live workspace...");
            touchRandomFilesByRandomUsers(Constants.LIVE_WORKSPACE, filePathsByOwner, touchFileSamplesLive);

            System.out.println("Downloading files by multiple users from live workspace...");
            downloadRandomFilesByRandomUsers(Constants.LIVE_WORKSPACE, filePathsByOwner, downloadFileSamplesLive);
        } finally {

            System.out.println("Removing folders/files by multiple users...");
            removeFoldersAndFiles(listSamples, removeSamples);
        }

        printStatistics("Multiple users CRD test", new OperationInfo[] {
            new OperationInfo("Creating folders/files", createFolderSamples),
            new OperationInfo("Uploading files", uploadFileSamples),
            new OperationInfo("Accessing files (live)", touchFileSamplesLive),
            new OperationInfo("Downloading files (live)", downloadFileSamplesLive),
            new OperationInfo("Listing sub-nodes", listSamples),
            new OperationInfo("Removing folders/files", removeSamples),
        });
    }

// Users lookup.

    private JCRUserNode getUser(int index) {
        return getUser(String.format(USER_NAME, index));
    }

    private JCRUserNode getUser(String name) {
        JCRUserNode user = userManagerService.lookup(name);
        assert (user != null);
        assert (USER_GROUP_PROVIDER.equals(user.getPropertyAsString(JCRUserNode.J_EXTERNAL_SOURCE))); // Ensure the user is retrieved from the desired provider.
        return user;
    }

// Folders/files creation.

    private List<String> createFoldersAndFiles(JCRUserNode user, final int numFilesPerFolder, final Collection<Long> createFolderSamples, final Collection<Long> uploadFileSamples) {

        final List<String> filePaths = new LinkedList<String>();

        byte[] fileBytes;
        try {
            fileBytes = FILE_CONTENT.getBytes(FILE_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        final ByteArrayInputStream fileContentStream = new ByteArrayInputStream(fileBytes);

        try {

            JCRTemplate.getInstance().doExecute(user.getJahiaUser(), Constants.EDIT_WORKSPACE, Locale.ENGLISH, new JCRCallback<Void>() {

                @Override
                public Void doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    createFoldersAndFiles(session.getUserNode(), USER_FOLDER_TREE_DEPTH, numFilesPerFolder, fileContentStream, filePaths, createFolderSamples, uploadFileSamples);
                    return null;
                }
            });
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }

        return filePaths;
    }

    private void createFoldersAndFiles(JCRNodeWrapper parent, int numLevelsToCreate, int numFilesPerFolder, InputStream fileContentStream, List<String> filePaths, Collection<Long> createFolderSamples, Collection<Long> uploadFileSamples) {

        for (int i = 0; i < numFilesPerFolder; i++) {
            String fileName = String.format(FOLDER_FILE_NAME, System.currentTimeMillis());
            try {
                fileContentStream.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                long start = System.currentTimeMillis();
                parent.uploadFile(fileName, fileContentStream, FILE_CONTENT_TYPE);
                parent.getSession().save();
                uploadFileSamples.add(System.currentTimeMillis() - start);
                filePaths.add(parent.getNode(fileName).getPath());
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }

        if (numLevelsToCreate == 0) {
            return;
        }

        for (int i = 0; i < NUM_USER_FOLDERS_PER_LEVEL; i++) {

            String folderName = String.format(FOLDER_FILE_NAME, System.currentTimeMillis());
            JCRNodeWrapper folder;
            long start = System.currentTimeMillis();
            try {
                folder = parent.addNode(folderName, Constants.JAHIANT_FOLDER);
                parent.getSession().save();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
            createFolderSamples.add(System.currentTimeMillis() - start);

            createFoldersAndFiles(folder, numLevelsToCreate - 1, numFilesPerFolder, fileContentStream, filePaths, createFolderSamples, uploadFileSamples);
        }
    }

// Touching files.

    private void touchRandomFiles(String workspace, JCRUserNode touchingUser, List<String> candidateFilePaths, Collection<Long> touchSamples) {
        Collection<String> filePathsToTouch = PerformanceTestUtils.getUniqueRandomElements(candidateFilePaths, NUM_FILES_TO_TOUCH_BY_SINGLE_USER);
        NodeProcessor fileTouching = new TouchFileProcessor();
        for (int i = 0; i < NUM_ITERATIONS_TOUCHING_FILES_BY_SINGLE_USER; i++) {
            accessFiles(workspace, touchingUser, filePathsToTouch, touchSamples, fileTouching);
        }
    }

    private void touchRandomFilesByRandomUsers(final String workspace, ListMultimap<Integer, String> filePathsByOwner, final Collection<Long> touchSamples) {

        List<String> filePathsToTouch = new LinkedList<String>();
        Random random = ThreadLocalRandom.current();
        final Set<Integer> ownersSeen = new HashSet<Integer>();
        for (int numOwners = 0; numOwners < NUM_OWNER_USERS; ) {
            int ownerIndex = random.nextInt(NUM_USERS);
            if (ownersSeen.contains(ownerIndex)) {
                continue;
            }
            ownersSeen.add(ownerIndex);
            numOwners++;
            List<String> filePaths = filePathsByOwner.get(ownerIndex);
            filePathsToTouch.addAll(PerformanceTestUtils.getUniqueRandomElements(filePaths, NUM_FILES_TO_TOUCH_PER_OWNER));
        }
        final List<String> finalFilePathsToTouch = new ArrayList<String>(filePathsToTouch);

        accessFilesByRandomUsers(workspace, NUM_TOUCHING_USERS,
            new UserRejector() {

                @Override
                public boolean isToBeRejected(int userIndex) {
                    // Reject this touching user in case it is among owners of files to touch.
                    return ownersSeen.contains(userIndex);
                }
            },
            new FilesAccessor() {

                @Override
                public void accessFilesByUser(int touchingIndex) {
                    JCRUserNode touchingUser = getUser(touchingIndex);
                    accessFiles(workspace, touchingUser, finalFilePathsToTouch, touchSamples, new TouchFileProcessor());
                }
            }
        );
    }

    private static class TouchFileProcessor implements NodeProcessor {

        @Override
        public void process(JCRNodeWrapper file) {
            file.getDisplayableName();
            file.getUrl();
        }
    }

// Downloading files.

    private void downloadRandomFiles(String workspace, JCRUserNode downloaderUser, List<String> candidateFilePaths, Collection<Long> downloadSamples) {
        Collection<String> filePathsToDownload = PerformanceTestUtils.getUniqueRandomElements(candidateFilePaths, NUM_FILES_TO_DOWNLOAD_BY_SINGLE_USER);
        accessFiles(workspace, downloaderUser, filePathsToDownload, downloadSamples, new DownloadFileProcessor());
    }

    private void downloadRandomFilesByRandomUsers(final String workspace, final ListMultimap<Integer, String> filePathsByOwner, final Collection<Long> downloadSamples) {

        final NodeProcessor fileDownloader = new DownloadFileProcessor();
        final Set<String> filePathsSeen = new HashSet<String>();

        accessFilesByRandomUsers(workspace, NUM_DOWNLOADER_USERS, null, new FilesAccessor() {

            @Override
            public void accessFilesByUser(int downloaderIndex) {
                JCRUserNode downloaderUser = getUser(downloaderIndex);
                Random random = ThreadLocalRandom.current();
                final Set<Integer> ownersSeen = new HashSet<Integer>();
                for (int numOwners = 0; numOwners < NUM_OWNER_USERS; ) {
                    int ownerIndex = random.nextInt(NUM_USERS);
                    if (ownersSeen.contains(ownerIndex)) {
                        continue;
                    }
                    if (ownerIndex == downloaderIndex) {
                        continue;
                    }
                    ownersSeen.add(ownerIndex);
                    numOwners++;
                    List<String> filePaths = filePathsByOwner.get(ownerIndex);
                    Collection<String> filePathsToDownload = PerformanceTestUtils.getUniqueRandomElements(filePaths, NUM_FILES_TO_DOWNLOAD_PER_DOWNLOADER_PER_OWNER, new PerformanceTestUtils.ElementRejector<String>() {

                        @Override
                        public boolean isToBeRejected(String filePath) {
                            return filePathsSeen.contains(filePath);
                        }
                    });
                    filePathsSeen.addAll(filePathsToDownload);
                    accessFiles(workspace, downloaderUser, filePathsToDownload, downloadSamples, fileDownloader);
                }
            }
        });
    }

    private static class DownloadFileProcessor implements NodeProcessor {

        @Override
        public void process(JCRNodeWrapper file) {
            file.getFileContent().downloadFile();
        }
    }

// General files access.

    private void accessFilesByRandomUsers(String workspace, int numAccessorUsers, UserRejector userRejector, FilesAccessor fileAccessor) {
        Random random = ThreadLocalRandom.current();
        Set<Integer> accessorsSeen = new HashSet<Integer>();
        for (int numAccessors = 0; numAccessors < numAccessorUsers; ) {
            int accessorIndex = random.nextInt(NUM_USERS);
            if (accessorsSeen.contains(accessorIndex)) {
                continue;
            }
            if (userRejector != null && userRejector.isToBeRejected(accessorIndex)) {
                continue;
            }
            accessorsSeen.add(accessorIndex);
            numAccessors++;
            fileAccessor.accessFilesByUser(accessorIndex);
        }
    }

    private interface UserRejector {

        boolean isToBeRejected(int userIndex);
    }

    private interface FilesAccessor {

        void accessFilesByUser(int accessorUserIndex);
    }

    private void accessFiles(String workspace, JCRUserNode accessorUser, final Collection<String> filePaths, final Collection<Long> accessSamples, final NodeProcessor accessor) {

        try {
            JCRTemplate.getInstance().doExecute(accessorUser.getJahiaUser(), workspace, Locale.ENGLISH, new JCRCallback<Void>() {

                @Override
                public Void doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    for (String filePath : filePaths) {
                        JCRNodeWrapper file = session.getNode(filePath);
                        long start = System.currentTimeMillis();
                        accessor.process(file);
                        accessSamples.add(System.currentTimeMillis() - start);
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

// Folders/files removal.

    private void removeFoldersAndFiles(Collection<Long> listSamples, Collection<Long> removeSamples) {
        for (int i = 0; i < NUM_USERS; i++) {
            JCRUserNode user = getUser(i);
            removeFoldersAndFiles(user, listSamples, removeSamples);
        }
    }

    private void removeFoldersAndFiles(JCRUserNode ownerUser, final Collection<Long> listSamples, final Collection<Long> removeSamples) {

        try {
            JCRTemplate.getInstance().doExecute(ownerUser.getJahiaUser(), Constants.EDIT_WORKSPACE, Locale.ENGLISH, new JCRCallback<Void>() {

                @Override
                public Void doInJCR(final JCRSessionWrapper session) throws RepositoryException {

                    processFoldersAndFiles(session.getUserNode(), listSamples, new NodeProcessor() {

                        @Override
                        public void process(JCRNodeWrapper folderOrFile) {
                            long start = System.currentTimeMillis();
                            try {
                                folderOrFile.remove();
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

// General folders/files processing.

    private void processFoldersAndFiles(JCRNodeWrapper scope, final Collection<Long> listSamples, final NodeProcessor nodeProcessor) {

        Iterable<JCRNodeWrapper> subNodes;
        long start = System.currentTimeMillis();
        try {
            subNodes = scope.getNodes();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        listSamples.add(System.currentTimeMillis() - start);

        for (JCRNodeWrapper subNode : subNodes) {

            String subNodeName = subNode.getName();
            if (subNodeName == null || !FOLDER_FILE_NAME_PATTERN.matcher(subNodeName).matches()) {
                continue;
            }

            // Process sub-nodes before the node itself, so that the processor could delete sub-nodes before deleting the node.
            processFoldersAndFiles(subNode, listSamples, nodeProcessor);
            nodeProcessor.process(subNode);
        }
    }

    private interface NodeProcessor {

        void process(JCRNodeWrapper node);
    }

// Results output.

    private static void printStatistics(String title, OperationInfo[] operations) {

        System.out.println(title);

        Integer operationColumnWidth = null;
        for (OperationInfo operation : operations) {
            String operationTitle = operation.getTitle();
            if (operationColumnWidth == null || operationTitle.length() > operationColumnWidth) {
                operationColumnWidth = operationTitle.length();
            }
        }
        Integer valueColumnWidth = null;
        for (Measurement measurement : Measurement.values()) {
            String columnTitle = measurement.getTitle();
            if (valueColumnWidth == null || columnTitle.length() > valueColumnWidth) {
                valueColumnWidth = columnTitle.length();
            }
        }
        int tableWidth = 1 + operationColumnWidth + 1 + Measurement.values().length * (valueColumnWidth + 1);
        StringBuilder horizontalRule = new StringBuilder();
        for (int i = 0; i < tableWidth; i++) {
            horizontalRule.append('-');
        }

        System.out.println(horizontalRule);
        System.out.print('|');
        printTableCellAdjustLeft(StringUtils.EMPTY, operationColumnWidth);
        for (Measurement measurement : Measurement.values()) {
            printTableCellAdjustRight(measurement.getTitle(), valueColumnWidth);
        }
        System.out.println();
        System.out.println(horizontalRule);

        for (OperationInfo operation : operations) {
            PerformanceTestUtils.TimingStatistics statistics = PerformanceTestUtils.getTimingStatistics(operation.getSamples());
            System.out.print('|');
            printTableCellAdjustLeft(operation.getTitle(), operationColumnWidth);
            for (Measurement measurement : Measurement.values()) {
                printTableCellAdjustRight(Long.toString(measurement.extractValue(statistics)), valueColumnWidth);
            }
            System.out.println();
            System.out.println(horizontalRule);
        }
    }

    private static void printTableCellAdjustLeft(String content, int width) {
        System.out.print(content);
        printSpace(width - content.length());
        System.out.print('|');
    }

    private static void printTableCellAdjustRight(String content, int width) {
        printSpace(width - content.length());
        System.out.print(content);
        System.out.print('|');
    }

    private static void printSpace(int width) {
        StringBuilder space = new StringBuilder();
        for (int i = 0; i < width; i++) {
            space.append(' ');
        }
        System.out.print(space);
    }

    private static class OperationInfo {

        private String title;
        private Collection<Long> samples;

        public OperationInfo(String title, Collection<Long> samples) {
            this.title = title;
            this.samples = samples;
        }

        public String getTitle() {
            return title;
        }

        public Collection<Long> getSamples() {
            return samples;
        }
    }
}
