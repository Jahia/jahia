/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.tags;
import javax.annotation.Nullable;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.*;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.slf4j.Logger;

import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * JCR content tagging service.
 * 
 * @author Sergiy Shyrkov
 */
public class TaggingService extends JahiaService{

    private static final Logger logger = getLogger(TaggingService.class);

    public static List<String> workspaces = Arrays.asList(Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
    private TagsSuggester tagsSuggester;
    private TagHandler tagHandler;

    private static final String JMIX_TAGGED = "jmix:tagged";
    private static final String J_TAG_LIST = "j:tagList";

    private static final Function<JCRValueWrapper, String> JCR_VALUE_WRAPPER_STRING_FUNCTION = new Function<JCRValueWrapper, String>() {
        @Nullable
        @Override
        public String apply(@Nullable JCRValueWrapper input) {
            try {
                return input != null ? input.getString() : null;
            } catch (RepositoryException e) {
                return null;
            }
        }
    };

    /**
     *
     * @param sitePath
     * @param selectedTag
     * @param tagNewName
     * @return
     */
    public Map<String, Set<String>> updateOrDeleteTagOnSite(String sitePath, String selectedTag, String tagNewName) {
        Map<String, Set<String>> errors = new HashMap<String, Set<String>>();
        String query = "SELECT * FROM [jmix:tagged] AS result WHERE ISDESCENDANTNODE(result, '" + sitePath + "') AND (result.[j:tagList] = '" + selectedTag + "')";
        try {
            for (String workspace : workspaces) {
                JCRSessionWrapper session = getSystemSessionWorkspace(workspace);
                QueryManager qm = session.getWorkspace().getQueryManager();
                Query q = qm.createQuery(query, Query.JCR_SQL2);
                NodeIterator ni = q.execute().getNodes();
                int i=0;
                while (ni.hasNext()) {
                    JCRNodeWrapper node = (JCRNodeWrapper) ni.nextNode();
                    try {
                        updateOrDeleteTagOnNode(node, selectedTag, tagNewName);
                        if(i%100==0){
                            session.save();
                        }
                        i++;
                    } catch (RepositoryException e) {
                        String displayableName = JCRContentUtils.getParentOfType(node, "jnt:page").getDisplayableName();
                        if (!errors.containsKey(displayableName)) {
                            errors.put(displayableName, new HashSet<String>());
                        }
                        errors.get(displayableName).add(node.getPath());
                    }
                }
                session.save();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return errors;
    }

    private JCRSessionWrapper getSystemSessionWorkspace(String selectedWorkspace) throws RepositoryException {
        return JCRSessionFactory.getInstance().getCurrentSystemSession(selectedWorkspace, Locale.ENGLISH, null);
    }

    /**
     *
     * @param node
     * @param selectedTag
     * @param tagNewName
     * @throws RepositoryException
     */
    public void updateOrDeleteTagOnNode(JCRNodeWrapper node, String selectedTag, String tagNewName) throws RepositoryException {
        String path = node.getPath();
        Set<String> newValues = new TreeSet<String>();
        JCRValueWrapper[] tags = node.getProperty("j:tagList").getValues();
        for (JCRValueWrapper tag : tags) {
            String tagValue = tag.getString();
            if (!tagValue.equals(selectedTag)) {
                newValues.add(tagValue);
            }
        }
        if (StringUtils.isNotEmpty(tagNewName) && !newValues.contains(tagNewName)) {
            newValues.add(tagNewName);
        }
        node.setProperty("j:tagList", newValues.toArray(new String[newValues.size()]));
        ModuleCacheProvider moduleCacheProvider = ModuleCacheProvider.getInstance();
        moduleCacheProvider.invalidate(path, true);
        List<String> keys = moduleCacheProvider.getRegexpDependenciesCache().getKeys();
        for (String key : keys) {
            if (path.matches(key)) {
                moduleCacheProvider.invalidateRegexp(key, true);
            }
        }
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        private Holder() {
        }

        static final TaggingService INSTANCE = new TaggingService();
    }

    /**
     * Get the tagging service instance
     * @return get the tagging service instance
     */
    public static TaggingService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * @deprecated the tags are no longer nodes, there are stored directly on the content
     *
     * @param tag the tag to create
     * @param siteKey the site where the tag need to be create
     * @throws RepositoryException in case of errors
     * @return true if the tag is successfully created
     */
    @Deprecated
    public boolean createTag(final String tag, final String siteKey) throws RepositoryException {
        return false;
    }

    /**
     * @deprecated not relevant anymore, the tags are no longer nodes
     *
     * @param tag the tag to delete
     * @param siteKey the site where the tag need to be delete
     * @throws RepositoryException in case of errors
     * @return true if the tag is successfully deleted
     */
    @Deprecated
    public boolean deleteTag(final String tag, final String siteKey) throws RepositoryException {
        return false;
    }

    /**
     * @deprecated not relevant anymore, the tags are no longer nodes, there are stored directly on the content,
     * Use {@link #getTagsSuggester().suggest(String, String, Long, Long, Long, boolean, org.jahia.services.content.JCRSessionWrapper)} instead
     *
     * @param tag the tag to test the existence
     * @param siteKey the site where the tag need to be search
     * @throws RepositoryException in case of errors
     * @return true if the tag exist
     */
    @Deprecated
    public boolean exists(final String tag, final String siteKey) throws RepositoryException {
        return false;
    }

    /**
     * @deprecated the tags are no longer nodes, there are stored directly on the content,
     * Use {@link #getTagsSuggester().suggest(String, String, Long, Long, Long, boolean, org.jahia.services.content.JCRSessionWrapper)} instead
     *
     * @param tag the tag to retrieve
     * @param siteKey the site to search the tag
     * @param session the session used to retrieve the tag
     * @throws RepositoryException in case of errors
     * @return return the tag node, null in case of tag not found
     */
    @Deprecated
    public JCRNodeWrapper getTag(String tag, String siteKey, JCRSessionWrapper session) throws RepositoryException {
        return null;
    }

    /**
     * @deprecated Use {@link #getTagsSuggester().suggest(String, String, Long, Long, Long, boolean, org.jahia.services.content.JCRSessionWrapper)} instead
     *
     * @param tag the tag to retrieve
     * @param siteKey the site to search the tag
     * @throws RepositoryException in case of errors
     * @return the count
     */
    @Deprecated
    public long getTagCount(final String tag, final String siteKey) throws RepositoryException {
        return 0;
    }

    /**
     * @deprecated Use {@link #tag(org.jahia.services.content.JCRNodeWrapper, String)} instead
     *
     * @param node the node to tag
     * @param tag the tag to apply on the node
     * @param siteKey the site where the tag is locate
     * @param createTagIfNotExists create the tag if not exist
     * @throws RepositoryException in case of errors
     * @return true if the tag is correctly apply on the node
     */
    @Deprecated
    public boolean tag(final JCRNodeWrapper node, final String tag, final String siteKey, final boolean createTagIfNotExists)
            throws RepositoryException {
        tag(node, tag);
        return true;
    }

    /**
     * @deprecated Use {@link #tag(org.jahia.services.content.JCRNodeWrapper, String)} instead
     *
     * @param nodePath the node to tag
     * @param tag the tag to apply on the node
     * @param siteKey the site where the tag is locate
     * @param createTagIfNotExists create the tag if not exist
     * @param session the session used to tag the content
     * @throws RepositoryException in case of errors
     * @return true if the tag is correctly apply on the node
     */
    @Deprecated
    public boolean tag(final String nodePath, final String tag, final String siteKey, final boolean createTagIfNotExists,
            JCRSessionWrapper session) throws RepositoryException {
        tag(nodePath, tag, session);
        return true;
    }

    /**
     * @deprecated Use {@link #tag(org.jahia.services.content.JCRNodeWrapper, String)} instead
     *
     * @param nodePath the node to tag
     * @param tag the tag to apply on the node
     * @param siteKey the site where the tag is locate
     * @param createTagIfNotExists create the tag if not exist
     * @throws RepositoryException in case of errors
     * @return true if the tag is correctly apply on the node
     */
    @Deprecated
    public boolean tag(final String nodePath, final String tag, final String siteKey, final boolean createTagIfNotExists)
            throws RepositoryException {
        return false;
    }

    /**
     * Tag the specific node
     *
     * @param node the node to tag
     * @param tags the tag list to apply on the node
     *
     * @return the list of added tags
     * @throws RepositoryException
     */
    public List<String> tag(final JCRNodeWrapper node, final List<String> tags) throws RepositoryException {
        if(tags.isEmpty()){
            return Collections.emptyList();
        }

        List<String> currentTags;
        List<JCRValueWrapper> currentTagValues = new ArrayList<JCRValueWrapper>();
        List<String> addedTags = new ArrayList<String>();
        try {
            currentTagValues = Arrays.asList(node.getProperty(J_TAG_LIST).getValues());
        } catch (PathNotFoundException e) {
            // property not found
            node.addMixin(JMIX_TAGGED);
        }
        currentTags = new ArrayList<String>(Collections2.transform(currentTagValues, JCR_VALUE_WRAPPER_STRING_FUNCTION));
        for (String tag : tags) {
            String cleanedTag = tagHandler.execute(tag);
            if (StringUtils.isNotEmpty(cleanedTag) && !currentTags.contains(cleanedTag)) {
                currentTags.add(cleanedTag);
                addedTags.add(cleanedTag);
            }
        }

        if(!addedTags.isEmpty()){
            node.setProperty(J_TAG_LIST, currentTags.toArray(new String[currentTags.size()]));
        }

        return addedTags;
    }

    /**
     * Tag the specific node
     *
     * @param nodePath the path of the node to tag
     * @param tags the tag list to apply on the node
     * @param session the session used to perform the operation
     *
     * @return the list of added tags
     * @throws RepositoryException
     */
    public List<String> tag(final String nodePath, final List<String> tags, JCRSessionWrapper session) throws RepositoryException {
        return tag(session.getNode(nodePath), tags);
    }

    /**
     * Tag the specific node
     *
     * @param node the node to tag
     * @param tag the tag to apply on the node
     *
     * @return the list of added tags
     * @throws RepositoryException
     */
    public List<String> tag(final JCRNodeWrapper node, final String tag) throws RepositoryException {
        return tag(node, Lists.newArrayList(tag));
    }

    /**
     * Tag the specific node
     *
     * @param nodePath the path of the node to tag
     * @param tag the tag to apply on the node
     * @param session the session used to perform the operation
     *
     * @return the list of added tags
     * @throws RepositoryException
     */
    public List<String> tag(final String nodePath, final String tag, JCRSessionWrapper session) throws RepositoryException {
         return tag(session.getNode(nodePath), Lists.newArrayList(tag));
    }

    /**
     * Untag the specific node
     *
     * @param node the node to untag
     * @param tags the tag list to remove from the node
     *
     * @return the list of deleted tags
     * @throws RepositoryException
     */
    public List<String> untag(final JCRNodeWrapper node, final List<String> tags) throws RepositoryException {
        if(tags.isEmpty()){
            return Collections.emptyList();
        }

        List<String> currentTags = new ArrayList<String>();
        List<String> deletedTags = new ArrayList<String>();
        if(node.isNodeType(JMIX_TAGGED)){
            try{
                JCRValueWrapper[] currentTagValues = node.getProperty(J_TAG_LIST).getValues();
                currentTags = new ArrayList<String>(Collections2.transform(Arrays.asList(currentTagValues), JCR_VALUE_WRAPPER_STRING_FUNCTION));
                for (String tag : tags){
                    int index = currentTags.indexOf(tag);
                    if(index != -1){
                        currentTags.remove(index);
                        deletedTags.add(tag);
                    }
                }
            } catch (PathNotFoundException e){
                // property not found
            }
        }

        if(!deletedTags.isEmpty()){
            node.setProperty(J_TAG_LIST, currentTags.toArray(new String[currentTags.size()]));
        }

        return deletedTags;
    }

    /**
     * Untag the specific node
     *
     * @param nodePath the path of the node to untag
     * @param tags the tag list to remove from the node
     * @param session the session used to perform the operation
     *
     * @return the list of deleted tags
     * @throws RepositoryException
     */
    public List<String> untag(final String nodePath, final List<String> tags, JCRSessionWrapper session) throws RepositoryException {
        return untag(session.getNode(nodePath), tags);
    }

    /**
     * Untag the specific node
     *
     * @param node the node to untag
     * @param tag the tag to remove from the node
     *
     * @return the list of deleted tags
     * @throws RepositoryException
     */
    public List<String> untag(final JCRNodeWrapper node, final String tag) throws RepositoryException {
        return untag(node, Lists.newArrayList(tag));
    }

    /**
     * Untag the specific node
     *
     * @param nodePath the path of the node to untag
     * @param tag the tag to remove from the node
     * @param session the session used to perform the operation
     *
     * @return the list of deleted tags
     * @throws RepositoryException
     */
    public List<String> untag(final String nodePath, final String tag, JCRSessionWrapper session) throws RepositoryException {
        return untag(session.getNode(nodePath), Lists.newArrayList(tag));
    }

    public void setTagsSuggester(TagsSuggester tagsSuggester) {
        this.tagsSuggester = tagsSuggester;
    }

    public TagHandler getTagHandler() {
        return tagHandler;
    }

    public void setTagHandler(TagHandler tagHandler) {
        this.tagHandler = tagHandler;
    }

    public TagsSuggester getTagsSuggester() {
        return tagsSuggester;
    }

    @Override
    public void start() throws JahiaInitializationException {
        // do nothing
    }

    @Override
    public void stop() throws JahiaException {
        // do nothing
    }
}
