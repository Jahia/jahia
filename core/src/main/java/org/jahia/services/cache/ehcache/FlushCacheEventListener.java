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
package org.jahia.services.cache.ehcache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import org.apache.jackrabbit.core.cluster.ClusterNode;
import org.apache.jackrabbit.core.journal.JournalException;
import org.apache.jackrabbit.core.journal.Record;
import org.apache.jackrabbit.core.journal.RecordConsumer;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.jahia.services.render.URLResolverFactory;
import org.jahia.services.render.filter.cache.CacheClusterEvent;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.seo.jcr.VanityUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.jahia.services.cache.CacheHelper.*;

/**
 * A listener that will invalidate corresponding caches upon messages received.
 *
 * @author cedric . mailleux @ jahia . com
 */
public class FlushCacheEventListener extends CacheEventListenerAdapter implements RecordConsumer {

    private static final Logger logger = LoggerFactory.getLogger(FlushCacheEventListener.class);
    private ClusterNode clusterNode;
    private Queue<PostPonedEvent> events;
    private long revision;

    private void init() {
        if (clusterNode == null) {
            clusterNode = SpringJackrabbitRepository.getInstance().getClusterNode();
            events = new ConcurrentLinkedQueue<>();
        }
        try {
            clusterNode.getJournal().register(this);
            this.setRevision(clusterNode.getRevision());
        } catch (JournalException e) {
            logger.debug(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("java:S1130")
    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {
        if (!SpringContextSingleton.getInstance().isInitialized() || SpringJackrabbitRepository.getInstance().getRepository() == null) {
            return;
        }
        init();
        String command = ((String) element.getObjectKey());
        final Object objectValue = element.getObjectValue();
        if (!(objectValue instanceof CacheClusterEvent)) {
            throw new CacheException(
                    "This cache only support element of type " + CacheClusterEvent.class.getName() + " you put " +
                            objectValue.getClass().getName()
            );
        }
        final CacheClusterEvent cacheClusterEvent = (CacheClusterEvent) objectValue;
        logger.debug("Received an event with revision {} where current journal revision is {}", cacheClusterEvent.getClusterRevision(), revision);
        if (revision >= cacheClusterEvent.getClusterRevision()) {
            executeCommand(command, ehcache.getName(), cacheClusterEvent.getEvent());
        } else {
            events.add(new PostPonedEvent(cacheClusterEvent.getEvent(), ehcache.getName(), revision, command));
        }
    }

    @SuppressWarnings({"deprecation","java:S1541"})
    private void executeCommand(String command, String cacheName, String event) {
        if (logger.isDebugEnabled()) {
            logger.debug("{}: Received command {} ({}) remotely", cacheName, command, event);
        }
        if (command.startsWith(CMD_FLUSH_PATH)) {
            ModuleCacheProvider.getInstance().invalidate(event, false);
        } else if (command.startsWith(CMD_FLUSH_REGEXPDEP)) {
            flushRegexpDependenciesOfPath(event);
        } else if (command.startsWith(CMD_FLUSH_REGEXP)) {
            ModuleCacheProvider.getInstance().invalidateRegexp(event, false);
        } else if (command.startsWith(CMD_FLUSH_CHILDREN) || command.startsWith(CMD_FLUSH_CHILDS)) {
            flushChildrenDependenciesOfPath(event);
        } else if (command.startsWith(CMD_FLUSH_MATCHINGPERMISSIONS)) {
            JahiaAccessManager.flushMatchingPermissions();
        } else if (command.equals(CMD_FLUSH_OUTPUT_CACHES)) {
            flushOutputCaches();
        } else if (command.startsWith(CMD_FLUSH_URLRESOLVER)) {
            URLResolverFactory urlResolverFactory = (URLResolverFactory) SpringContextSingleton.getInstance().getContext().getBean("urlResolverFactory");
            urlResolverFactory.flushCaches(event);
        } else if (command.startsWith(CMD_FLUSH_VANITYURL)) {
            VanityUrlService vanityUrlService = (VanityUrlService) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.seo.jcr.VanityUrlService");
            vanityUrlService.flushCaches();
        } else if (command.equals(CMD_FLUSH_ALL_CACHES)) {
            flushAllCaches(false);
        }
    }

    @Override
    @SuppressWarnings("java:S1130")
    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {
        notifyElementPut(ehcache, element);
    }

    private void flushChildrenDependenciesOfPath(String path) {
        ModuleCacheProvider.getInstance().flushChildrenDependenciesOfPath(path, false);
    }

    private void flushRegexpDependenciesOfPath(String path) {
        ModuleCacheProvider.getInstance().flushRegexpDependenciesOfPath(path, false);
    }

    /**
     * Return the unique identifier of the records this consumer
     * will be able to handle.
     *
     * @return unique identifier
     */
    @Override
    public String getId() {
        return "JDF_CACHE";
    }

    /**
     * Return the revision this consumer has last seen.
     *
     * @return revision
     */
    @Override
    public long getRevision() {
        return revision;
    }

    /**
     * Consume a record.
     *
     * @param record record to consume
     */
    @Override
    public void consume(Record record) {
        logger.error("This consumer can not handle records.");
    }

    /**
     * Set the revision this consumer has last seen.
     *
     * @param revision revision
     */
    @Override
    public void setRevision(long revision) {
        logger.debug("Flushing event previous to revision: {}", revision);
        PostPonedEvent peek = events.peek();
        while (peek != null && peek.getRevision() <= revision) {
            events.remove();
            executeCommand(peek.getCommand(), peek.getName(), peek.getElement());
            peek = events.peek();
        }
        if (logger.isDebugEnabled()) {
            if (events.isEmpty()) {
                logger.debug("No more events fo flush");
            } else {
                logger.debug("Still some events to flush. Next revision to flush is {}", events.peek()
                        .getRevision());
            }
        }
        this.revision = revision;
    }

    private static class PostPonedEvent {
        private final String element;
        private final String name;
        private final long revision;
        private final String command;

        public PostPonedEvent(String element, String name, long revision, String command) {

            this.element = element;
            this.name = name;
            this.revision = revision;
            this.command = command;
        }

        public String getElement() {
            return element;
        }

        public String getName() {
            return name;
        }

        public long getRevision() {
            return revision;
        }

        public String getCommand() {
            return command;
        }
    }
}
