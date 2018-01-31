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
package org.jahia.services.tags;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * JCR content tagging service.
 * 
 * @author Sergiy Shyrkov
 */
public class TaggingService extends JahiaService implements JahiaAfterInitializationService {
    private static final Logger logger = getLogger(TaggingService.class);

    private TagsSuggester tagsSuggester;
    private TagHandler tagHandler;
    private String tagSeparator = null;

    public static final String JMIX_TAGGED = "jmix:tagged";
    public static final String J_TAG_LIST = "j:tagList";

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

    public void init() throws NoSuchNodeTypeException {
    }

    @Override
    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        try {
            Map<String, String> tagPropSelectorOptions = NodeTypeRegistry.getInstance().getNodeType(TaggingService.JMIX_TAGGED).getPropertyDefinition(TaggingService.J_TAG_LIST).getSelectorOptions();
            tagSeparator = tagPropSelectorOptions.get("separator");
        } catch (NoSuchNodeTypeException e) {
            throw new JahiaInitializationException("Cannot initialize tag service",e);
        }
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
     * @throws RepositoryException in case of JCR-related errors
     */
    public List<String> tag(final JCRNodeWrapper node, final List<String> tags) throws RepositoryException {
        if(tags == null || tags.isEmpty()){
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
            String[] splitedTags = tagSeparator != null ? tag.split(tagSeparator) : new String[]{tag};
            for (String splitedTag : splitedTags){
                String cleanedTag = tagHandler.execute(splitedTag);
                if (StringUtils.isNotEmpty(cleanedTag) && !currentTags.contains(cleanedTag)) {
                    currentTags.add(cleanedTag);
                    addedTags.add(cleanedTag);
                }
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
     * @throws RepositoryException in case of JCR-related errors
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
     * @throws RepositoryException in case of JCR-related errors
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
     * @throws RepositoryException in case of JCR-related errors
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
     * @throws RepositoryException in case of JCR-related errors
     */
    public List<String> untag(final JCRNodeWrapper node, final List<String> tags) throws RepositoryException {
        if(tags == null || tags.isEmpty()){
            return Collections.emptyList();
        }

        List<String> deletedTags = new ArrayList<String>();
        if(node.isNodeType(JMIX_TAGGED) && node.hasProperty(J_TAG_LIST)){
            try{
                JCRPropertyWrapper tagsProp = node.getProperty(J_TAG_LIST);
                JCRValueWrapper[] currentTagValues = tagsProp.getValues();
                List<String> currentTags = new ArrayList<String>(Collections2.transform(Arrays.asList(currentTagValues), JCR_VALUE_WRAPPER_STRING_FUNCTION));
                for (String tag : tags){
                    int index = currentTags.indexOf(tag);
                    if(index != -1){
                        currentTags.remove(index);
                        deletedTags.add(tag);
                    }
                }

                if(!deletedTags.isEmpty()){
                    if(currentTags.isEmpty()){
                        try {
                            tagsProp.remove();
                            node.removeMixin(JMIX_TAGGED);
                        } catch (NoSuchNodeTypeException noSuchNodeTypeException) {
                            // mixin jmix:tagged is on the nodetype definition, can't remove it
                        }
                    } else {
                        node.setProperty(J_TAG_LIST, currentTags.toArray(new String[currentTags.size()]));
                    }
                }
            } catch (PathNotFoundException e){
                // property not found
            }
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
     * @throws RepositoryException in case of JCR-related errors
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
     * @throws RepositoryException in case of JCR-related errors
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
     * @throws RepositoryException in case of JCR-related errors
     */
    public List<String> untag(final String nodePath, final String tag, JCRSessionWrapper session) throws RepositoryException {
        return untag(session.getNode(nodePath), Lists.newArrayList(tag));
    }

    /**
     * Rename a specific tag on a node
     * @param node target node
     * @param selectedTag tag to rename
     * @param tagNewName new tag name
     * @throws RepositoryException in case of JCR-related errors
     */
    public void renameTag(final JCRNodeWrapper node, final String selectedTag, final String tagNewName) throws RepositoryException {
        if(node.isNodeType(JMIX_TAGGED) && node.hasProperty(J_TAG_LIST) && tagNewName != null && tagNewName.length() > 0){
            Set<String> newValues = new TreeSet<String>();
            JCRValueWrapper[] tags = node.getProperty(J_TAG_LIST).getValues();
            for (JCRValueWrapper tag : tags) {
                String tagValue = tag.getString();
                if(!tagValue.equals(selectedTag)) {
                    newValues.add(tagValue);
                }
            }

            String[] splitedTags = tagSeparator != null ? tagNewName.split(tagSeparator) : new String[]{tagNewName};
            for (String splitedTag : splitedTags){
                newValues.add(tagHandler.execute(splitedTag));
            }

            node.setProperty("j:tagList", newValues.toArray(new String[newValues.size()]));
        }
    }

    /**
     * Rename a specific tag on a node
     * @param nodePath target node path
     * @param selectedTag tag to rename
     * @param tagNewName new tag name
     * @param session the session used to perform the operation
     * @throws RepositoryException in case of JCR-related errors
     */
    public void renameTag(final String nodePath, final String selectedTag, final String tagNewName, JCRSessionWrapper session) throws RepositoryException {
        renameTag(session.getNode(nodePath), selectedTag, tagNewName);
    }

    /**
     * Rename all occurrence of a specific tag under a given path
     * The session will be saved each 100 nodes processed
     *
     * @param startPath the start path for the renaming
     * @param session the session used to perform the operation
     * @param selectedTag tag to rename
     * @param tagNewName new tag name
     * @param callback an optional callback can be used to hook on the actions processed
     * @throws RepositoryException in case of JCR-related errors
     */
    public <X> X renameTagUnderPath(String startPath, JCRSessionWrapper session, String selectedTag, String tagNewName, TagActionCallback<X> callback) throws RepositoryException {
        //Check here if tagNewName is not empty before bench operation
        if(StringUtils.isNotEmpty(tagHandler.execute(tagNewName))){
            return  updateOrDeleteTagUnderPath(startPath, session, selectedTag, tagNewName, callback);
        }
        return null;
    }

    /**
     * Delete all occurrence of a specific tag under a given path
     *
     * @param startPath the start path for the deleting
     * @param session the session used to perform the operation
     * @param selectedTag tag to delete
     * @param callback an optional callback can be used to hook on the actions processed
     * @throws RepositoryException in case of JCR-related errors
     */
    public <X> X deleteTagUnderPath(String startPath, JCRSessionWrapper session, String selectedTag, TagActionCallback<X> callback) throws RepositoryException {
        return updateOrDeleteTagUnderPath(startPath, session, selectedTag, null, callback);
    }

    private <X> X updateOrDeleteTagUnderPath(String startPath, JCRSessionWrapper session, String selectedTag, String tagNewName, TagActionCallback<X> callback) throws RepositoryException{
        String query = "SELECT * FROM [jmix:tagged] AS result WHERE ISDESCENDANTNODE(result, '" + JCRContentUtils.sqlEncode(startPath) + "') AND " +
                "(result.[j:tagList] = '" + Text.escapeIllegalXpathSearchChars(selectedTag).replaceAll("'", "''") + "')";
        QueryManager qm = session.getWorkspace().getQueryManager();
        Query q = qm.createQuery(query, Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();

        while (ni.hasNext()) {
            JCRNodeWrapper node = (JCRNodeWrapper) ni.nextNode();
            try {
                if(StringUtils.isNotEmpty(tagNewName)) {
                    renameTag(node, selectedTag, tagNewName);
                } else {
                    untag(node, selectedTag);
                }
                if(callback != null){
                    callback.afterTagAction(node);
                }
            } catch (RepositoryException e){
                if(callback != null){
                    callback.onError(node, e);
                } else {
                    logger.error("Error trying to " + (StringUtils.isNotEmpty(tagNewName) ? "rename" : "delete") + " tag '" + selectedTag + "' on node " + node.getPath(), e);
                }
            }
        }

        if(callback != null) {
            return callback.end();
        }
        return null;
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
