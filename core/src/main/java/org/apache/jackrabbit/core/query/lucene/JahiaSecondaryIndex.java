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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.query.OnWorkspaceInconsistency;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.query.lucene.directory.DirectoryManager;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.spi.Path;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Similarity;
import org.apache.tika.parser.Parser;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;

import java.io.IOException;
import java.util.*;

/**
 * Temporary index used for reindexing only.
 */
public class JahiaSecondaryIndex extends JahiaSearchIndex {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JahiaSecondaryIndex.class);

    class DelayedIndexUpdate {
        Iterator<NodeId> remove;
        Iterator<NodeState> add;

        DelayedIndexUpdate(Iterator<NodeId> remove, Iterator<NodeState> add) {
            super();
            this.remove = remove;
            this.add = add;
        }
    }

    private List<DelayedIndexUpdate> delayedUpdates = Collections.synchronizedList(new LinkedList<DelayedIndexUpdate>());

    private JahiaSearchIndex mainIndex;

    private SpellChecker spellChecker;

    private String path;

    /**
     * Initializes an instance of this class.
     *
     * @param mainIndex
     *            the main search index instance
     */
    public JahiaSecondaryIndex(JahiaSearchIndex mainIndex) {
        super();
        this.mainIndex = mainIndex;
    }

    /**
     * Initializes this <code>QueryHandler</code>. This implementation requires
     * that a path parameter is set in the configuration. If this condition
     * is not met, a <code>IOException</code> is thrown.
     *
     * @throws IOException if an error occurs while initializing this handler.
     */
    protected void newIndexInit() throws IOException {
        QueryHandlerContext context = getContext();

        Set<NodeId> excludedIDs = new HashSet<NodeId>();
        if (context.getExcludedNodeId() != null) {
            excludedIDs.add(context.getExcludedNodeId());
        } else {
            // Avoid parsing of version storage
            if (JahiaSearchIndex.SKIP_VERSION_INDEX) {
                excludedIDs.add(RepositoryImpl.VERSION_STORAGE_NODE_ID);
            }
        }

        long startTime = System.currentTimeMillis();

        index = new MultiIndex(this, excludedIDs);
        if (index.numDocs() == 0) {
            Path rootPath;
            if (context.getExcludedNodeId() == null) {
                // this is the index for jcr:system
                rootPath = JCR_SYSTEM_PATH;
            } else {
                rootPath = ROOT_PATH;
            }
            index.createInitialIndex(context.getItemStateManager(),
                    context.getRootId(), rootPath);
        }

        log.info("Creation of initial index finished in {} ms", System.currentTimeMillis() - startTime);

        startTime = System.currentTimeMillis();

        log.info("Running consistency check...");
        try {
            ConsistencyCheck check = runConsistencyCheck();
            check.repair(true);
        } catch (Exception e) {
            log.warn("Failed to run consistency check on index: " + e);
        }

        log.info("Consistency check took {} ms", System.currentTimeMillis() - startTime);

        initSpellChecker();
    }

    private void initSpellChecker() {
        // initialize spell checker
        SpellChecker spCheck = null;
        String spellCheckerClassName = getSpellCheckerClass();
        if (spellCheckerClassName != null) {
            try {
                Class<?> clazz = Class.forName(spellCheckerClassName);
                if (SpellChecker.class.isAssignableFrom(clazz)) {
                    spCheck = (SpellChecker) clazz.newInstance();
                    spCheck.init(this);
                } else {
                    log.warn("Invalid value for spellCheckerClass, {} " + "does not implement SpellChecker interface.",
                            spellCheckerClassName);
                }
            } catch (Exception e) {
                log.warn("Exception initializing spell checker: " + spellCheckerClassName, e);
            }
        }

        spellChecker = spCheck;
    }

    void replayDelayedUpdates(JahiaSearchIndex targetIndex) throws RepositoryException, IOException {
        long start = System.currentTimeMillis();
        int count = 0;
        while (!delayedUpdates.isEmpty()) {
            DelayedIndexUpdate n = delayedUpdates.remove(0);
            count++;
            targetIndex.updateNodes(n.remove, n.add, false);
        }
        if (count > 0) {
            log.info("Replayed {} delayed updates on index {} in {} ms",
                    new Object[] { count, getPath(), System.currentTimeMillis() - start });
        }
    }

    void addDelayedUpdated(Iterator<NodeId> remove, Iterator<NodeState> add) {
        delayedUpdates.add(new DelayedIndexUpdate(remove, add));
    }

    @Override
    public SpellChecker getSpellChecker() {
        return spellChecker;
    }

    @Override
    public IndexingConfiguration getIndexingConfig() {
        return mainIndex.getIndexingConfig();
    }

    @Override
    public NamespaceMappings getNamespaceMappings() {
        return mainIndex.getNamespaceMappings();
    }

    @Override
    public Analyzer getTextAnalyzer() {
        return mainIndex.getTextAnalyzer();
    }

    @Override
    public Similarity getSimilarity() {
        return mainIndex.getSimilarity();
    }

    @Override
    public DirectoryManager getDirectoryManager() {
        try {
            Class<?> clazz = Class.forName(getDirectoryManagerClass());
            if (!DirectoryManager.class.isAssignableFrom(clazz)) {
                throw new IOException(getDirectoryManagerClass() +
                        " is not a DirectoryManager implementation");
            }
            DirectoryManager df = (DirectoryManager) clazz.newInstance();
            df.init(this);
            return df;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RedoLogFactory getRedoLogFactory() {
        return mainIndex.getRedoLogFactory();
    }

    @Override
    public boolean getUseCompoundFile() {
        return mainIndex.getUseCompoundFile();
    }

    @Override
    public int getMinMergeDocs() {
        return mainIndex.getMinMergeDocs();
    }

    @Override
    public int getMaxExtractLength() {
        return mainIndex.getMaxExtractLength();
    }

    @Override
    public int getMaxFieldLength() {
        return mainIndex.getMaxFieldLength();
    }

    @Override
    public long getMaxHistoryAge() {
        return mainIndex.getMaxHistoryAge();
    }

    @Override
    public long getMaxVolatileIndexSize() {
        return mainIndex.getMaxVolatileIndexSize();
    }

    @Override
    public int getMergeFactor() {
        return mainIndex.getMergeFactor();
    }

    @Override
    public int getMaxMergeDocs() {
        return mainIndex.getMaxMergeDocs();
    }

    @Override
    public Parser getParser() {
        return mainIndex.getParser();
    }

    @Override
    public String getPath() {
        if (path == null) {
            path = mainIndex.getPath() + ".new." + System.currentTimeMillis();
        }

        return path;
    }

    @Override
    public Path getRelativePath(NodeState nodeState, PropertyState propState) throws RepositoryException, ItemStateException {
        return mainIndex.getRelativePath(nodeState, propState);
    }

    @Override
    public String getSpellCheckerClass() {
        return mainIndex.getSpellCheckerClass();
    }

    @Override
    public boolean getSupportHighlighting() {
        return mainIndex.getSupportHighlighting();
    }

    @Override
    public int getTermInfosIndexDivisor() {
        return mainIndex.getTermInfosIndexDivisor();
    }

    @Override
    public int getVolatileIdleTime() {
        return mainIndex.getVolatileIdleTime();
    }

    @Override
    public String getDirectoryManagerClass() {
        return mainIndex.getDirectoryManagerClass();
    }

    @Override
    public int getCacheSize() {
        return mainIndex.getCacheSize();
    }

    @Override
    public int getBufferSize() {
        return mainIndex.getBufferSize();
    }

    @Override
    public QueryHandlerContext getContext() {
        return mainIndex.getContext();
    }

    @Override
    public OnWorkspaceInconsistency getOnWorkspaceInconsistencyHandler() {
        return OnWorkspaceInconsistency.LOG;
    }
}
