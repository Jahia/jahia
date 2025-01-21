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
package org.jahia.services.importexport;

import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.QueryManagerWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.util.*;

/**
 * Site Export Context
 * @author achaabni
 */
public class ExportContext {

    private static final Logger logger = LoggerFactory.getLogger(ExportContext.class);

    private boolean logProgressEnabled = false;
    private boolean collectBinariesEnabled = false;
    private long nodesToExport = 0;
    private int exportIndex = 0;
    private long step = 0;
    private String actualPath;
    private long startedTimeMillis = 0;
    private final Map<String, Binary> binariesToExport = new HashMap<>();
    private ExportContext liveExportContext;

    public ExportContext(boolean logProgressEnabled, boolean collectBinariesEnabled) {
        this.logProgressEnabled = logProgressEnabled;
        this.collectBinariesEnabled = collectBinariesEnabled;
    }

    /**
     * @deprecated use {@link #ExportContext(boolean, boolean)} instead
     */
    @Deprecated(since = "8.2.1.0", forRemoval = true)
    public ExportContext(long nodesToExport) {
        this.nodesToExport = nodesToExport;
    }

    /**
     * log progress of export, this method will log the progress of the export
     *
     * @param newPath the path being exported
     */
    protected void logExportXmlDocumentElementProcessed(String newPath) {
        if (!logProgressEnabled) {
            return;
        }

        this.actualPath = newPath;
        this.exportIndex = exportIndex + 1;
        if (logger.isDebugEnabled()) {
            logger.debug("Index: {}, Exporting  : {}", exportIndex, actualPath);
        }

        // this will show the percentage of export done by 10% increment will start by 10 and end by 90
        long currentStep = exportIndex * 10L / nodesToExport;
        if (currentStep > step && step < 9) {
            this.step = currentStep;
            logger.info("Export {}%", step * 10);
        }
    }

    /**
     * log initialization of export, this method will estimate the number of nodes to export
     *
     * @param sortedNodes       the nodes to be exported
     * @param session           the session used to export
     * @param nodeTypesToIgnore the node types to ignore in the export
     * @return the estimation of nodes to export
     * @throws RepositoryException in case of JCR-related errors
     */
    protected void logExportXmlDocumentStarted(Set<JCRNodeWrapper> sortedNodes, JCRSessionWrapper session, Set<String> nodeTypesToIgnore) throws RepositoryException {
        if (!logProgressEnabled) {
            return;
        }
        this.startedTimeMillis = System.currentTimeMillis();

        long result = 0;
        List<String> extraPathsToExport = new ArrayList<>();

        for (JCRNodeWrapper nodesToExport : sortedNodes) {
            // site node are a bit special (languages nodes have to be exported)
            if (nodesToExport instanceof JCRSiteNode) {
                Set<String> languages = ((JCRSiteNode) nodesToExport).getLanguages();
                List<String> sitePaths = Collections.singletonList(nodesToExport.getPath());
                result += estimateSubNodesNumber(sitePaths, session, nodeTypesToIgnore, null);
                if (languages != null && !languages.isEmpty()) {
                    for (String language : languages) {
                        result += estimateSubNodesNumber(sitePaths, session, nodeTypesToIgnore, language);
                    }
                }
            } else {
                extraPathsToExport.add(nodesToExport.getPath());
            }
        }

        // Avoid using all paths within a single query to prevent a stack overflow when number of paths is large.
        // see https://jira.jahia.org/browse/QA-11510
        final int maxBatchSize = 100;
        while (!extraPathsToExport.isEmpty()) {
            int toIndex = Math.min(extraPathsToExport.size(), maxBatchSize);
            List<String> paths = extraPathsToExport.subList(0, toIndex);
            result += estimateSubNodesNumber(paths, session, nodeTypesToIgnore, null);
            paths.clear();
        }

        this.nodesToExport = result;
        logger.info("Approximate number of nodes to export: {}, estimated in: {} seconds", nodesToExport, getDuration(startedTimeMillis));
    }

    /**
     * log end of export, this method will log the end of the export
     */
    protected void logExportXmlDocumentGenerated() {
        if (!logProgressEnabled) {
            return;
        }
        // Nodes are now exported in the .xml, so we log the time difference for this export
        logger.info("Exported {} nodes in {} seconds", exportIndex, getDuration(startedTimeMillis));
    }

    /**
     * Collect binary to export, this method will collect binaries to export
     *
     * @param path   the path of the binary
     * @param binary the binary to export
     */
    protected void collectBinaryToExport(String path, Binary binary) {
        if (collectBinariesEnabled) {
            binariesToExport.put(path, binary);
        }
    }

    /**
     * Check if binary to export is already collected, this method will check if the binary to export is already collected
     *
     * @param path   the path of the binary
     * @param binary the binary to export
     * @return true if the binary is already collected, false otherwise
     */
    protected boolean isBinaryAlreadyCollected(String path, Binary binary) {
        if (!collectBinariesEnabled) {
            return false;
        }
        return binary != null && binary.equals(binariesToExport.get(path));
    }

    /**
     * Estimates subnodes number to be exported, estimation use a count query using given parameters.
     *
     * @param paths             list of paths in query
     * @param session           session used to execute the query
     * @param nodeTypesToIgnore Set of nodetypes to filter in query
     * @param locale            language to be used when jnt:translation nodes have to be retrieved
     * @return the final estimation
     * @throws RepositoryException in case of JCR-related errors
     */
    private long estimateSubNodesNumber(List<String> paths, JCRSessionWrapper session, Set<String> nodeTypesToIgnore, String locale) throws RepositoryException {
        if (paths.isEmpty()) {
            return 0;
        }

        // create the query count, if a locale is specified the query is adapted to retrieved only jnt:translation nodes
        QueryManagerWrapper queryManagerWrapper = session.getWorkspace().getQueryManager();
        StringBuilder statement = new StringBuilder("SELECT count AS [rep:count(skipChecks=1)] FROM [")
                .append(locale != null ? "jnt:translation" : "nt:base")
                .append("] WHERE (");

        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            if (i > 0) {
                statement.append(" OR ");
            }
            statement.append("isdescendantnode(['").append(JCRContentUtils.sqlEncode(path)).append("'])");
        }
        statement.append(")");

        if (locale == null && nodeTypesToIgnore != null && !nodeTypesToIgnore.isEmpty()) {
            statement.append("AND NOT (");
            Iterator<String> nodeTypesToIgnoreIterator = nodeTypesToIgnore.iterator();
            while (nodeTypesToIgnoreIterator.hasNext()) {
                statement.append("[jcr:primaryType] = '").append(JCRContentUtils.sqlEncode(nodeTypesToIgnoreIterator.next())).append("'");
                if (nodeTypesToIgnoreIterator.hasNext()) {
                    statement.append(" OR ");
                }
            }
            statement.append(")");
        }

        if (locale != null) {
            statement.append(" AND [jcr:language] = '").append(JCRContentUtils.sqlEncode(locale)).append("'");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Executing query: {}", statement);
        }

        return queryManagerWrapper.createQuery(statement.toString(), Query.JCR_SQL2).execute().getRows().nextRow().getValue("count").getLong();
    }

    private String getDuration(long start) {
        return DateUtils.formatDurationWords(System.currentTimeMillis() - start);
    }

    public ExportContext getLiveExportContext() {
        return liveExportContext;
    }

    public void setLiveExportContext(ExportContext liveExportContext) {
        this.liveExportContext = liveExportContext;
    }

    public Map<String, Binary> getBinariesToExport() {
        return Collections.unmodifiableMap(binariesToExport);
    }

    public boolean isLogProgressEnabled() {
        return logProgressEnabled;
    }

    public void setLogProgressEnabled(boolean logProgressEnabled) {
        this.logProgressEnabled = logProgressEnabled;
    }

    public long getNodesToExport() {
        return nodesToExport;
    }

    public void setNodesToExport(long nodesToExport) {
        this.nodesToExport = nodesToExport;
    }

    public int getExportIndex() {
        return exportIndex;
    }

    public void setExportIndex(int exportIndex) {
        this.exportIndex = exportIndex;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public String getActualPath() {
        return actualPath;
    }

    public void setActualPath(String actualPath) {
        this.actualPath = actualPath;
    }
}
