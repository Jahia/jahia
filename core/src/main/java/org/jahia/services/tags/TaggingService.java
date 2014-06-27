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
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.jahia.services.content.*;
import org.jahia.services.query.QOMBuilder;
import org.jahia.services.query.QueryResultWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * JCR content tagging service.
 * 
 * @author Sergiy Shyrkov
 */
public class TaggingService {

    private static Logger logger = LoggerFactory.getLogger(TaggingService.class);
    private final static String JMIX_TAGGED = "jmix:tagged";
    private final static String J_TAG_LIST = "j:tagList";

    private final static Function<JCRValueWrapper, String> JCR_VALUE_WRAPPER_STRING_FUNCTION = new Function<JCRValueWrapper, String>() {
        @Override
        public String apply(JCRValueWrapper input) {
            try {
                return input.getString();
            } catch (RepositoryException e) {
                return null;
            }
        }
    };

    /**
     * @deprecated the tags are no longer nodes, there are stored directly on the content
     * @throws RepositoryException
     *             in case of errors
     */
    @Deprecated
    public boolean createTag(final String tag, final String siteKey) throws RepositoryException {
        return false;
    }

    /**
     * Deletes the specified tag node using specified name for the current site.
     *
     * @param tag
     *            the name of the tag to be deleted
     * @param siteKey
     *            the site key for the current site
     * @return <code>true</code> if the tag node was successfully deleted;
     *         <code>false</code> if the specified tag does not exist.
     * @throws RepositoryException
     *             in case of errors
     */
    public boolean deleteTag(final String tag, final String siteKey) throws RepositoryException {
        //TODO

        /*return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                final String tagsPath = getTagsPath(siteKey);
                JCRNodeWrapper tagNode = null;
                try {
                    tagNode = session.getNode(tagsPath + "/" + tag);
                } catch (PathNotFoundException ex) {
                    // no tag can be found
                }
                if (tagNode != null) {
                    JCRNodeWrapper parent = tagNode.getParent();
                    session.checkout(parent);
                    session.checkout(tagNode);
                    tagNode.remove();
                    session.save();
                }
                return tagNode != null;
            }
        });*/
        return true;
    }

    /**
     * @deprecated not relevant anymore, the tags are no longer nodes, there are stored directly on the content
     * @throws RepositoryException
     *             in case of errors
     */
    @Deprecated
    public boolean exists(final String tag, final String siteKey) throws RepositoryException {
        return false;
    }

    /**
     * @deprecated the tags are no longer nodes, there are stored directly on the content
     * @throws RepositoryException
     *             in case of errors
     */
    @Deprecated
    public JCRNodeWrapper getTag(String tag, String siteKey, JCRSessionWrapper session) throws RepositoryException {
        return null;
    }

    /**
     * Returns the total number of tag references. Returns <code>-1</code> if
     * the corresponding tag does not exist.
     * 
     * @param tag
     *            the name of the tag to check references
     * @param siteKey
     *            the site key for the current site
     * @return the total number of tag references. Returns <code>-1</code> if
     *         the corresponding tag does not exist.
     * @throws RepositoryException
     *             in case of errors
     */
    public long getTagCount(final String tag, final String siteKey) throws RepositoryException {
        //TODO
        return 0;
    }

    /**
     * @deprecated Use {@link #tag(org.jahia.services.content.JCRNodeWrapper, String)} instead
     * @throws RepositoryException
     *             in case of errors
     */
    @Deprecated
    public boolean tag(final JCRNodeWrapper node, final String tag, final String siteKey, final boolean createTagIfNotExists)
            throws RepositoryException {
        tag(node, tag);
        return true;
    }

    /**
     * @deprecated Use {@link #tag(String, String, org.jahia.services.content.JCRSessionWrapper)} instead
     * @throws RepositoryException
     *             in case of errors
     */
    @Deprecated
    public boolean tag(final String nodePath, final String tag, final String siteKey, final boolean createTagIfNotExists,
            JCRSessionWrapper session) throws RepositoryException {
        tag(nodePath, tag, session);
        return true;
    }

    /**
     * @deprecated Use {@link #tag(String, String)} instead
     * @throws RepositoryException
     *             in case of errors
     */
    @Deprecated
    public boolean tag(final String nodePath, final String tag, final String siteKey, final boolean createTagIfNotExists)
            throws RepositoryException {
        tag(nodePath, tag);
        return true;
    }

    public List<String> tag(final JCRNodeWrapper node, final List<String> tags) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(node.getSession().getUser().getUsername(), node.getSession().getWorkspace().getName(),((JCRSessionWrapper)node.getSession()).getLocale(), new JCRCallback<List<String>>() {
            public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                List<String> addedTags = tag(node.getPath(), tags, session);
                session.save();
                return addedTags;
            }
        });
    }

    public List<String> tag(final String nodePath, final List<String> tags) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<String>>() {
            public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                List<String> addedTags = tag(nodePath, tags, session);
                session.save();
                return addedTags;
            }
        });
    }

    public List<String> tag(final String nodePath, final List<String> tags, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper node = session.getNode(nodePath);
        List<String> currentTags = new ArrayList<String>();
        List<JCRValueWrapper> currentTagValues = new ArrayList<JCRValueWrapper>();
        List<String> addedTags = new ArrayList<String>();
        boolean updated = false;
        try {
            currentTagValues = Arrays.asList(node.getProperty(J_TAG_LIST).getValues());
        } catch (PathNotFoundException e) {
            // property not found
            if (tags.size() > 0) {
                node.addMixin(JMIX_TAGGED);
                updated = true;
            }
        }
        currentTags = new ArrayList<String>(Collections2.transform(currentTagValues, JCR_VALUE_WRAPPER_STRING_FUNCTION));
        for (String tag : tags) {
            if (StringUtils.isNotEmpty(tag.trim()) && !currentTags.contains(tag)) {
                currentTags.add(tag);
                addedTags.add(tag);
                updated = true;
            }
        }

        if(updated){
            node.setProperty(J_TAG_LIST, currentTags.toArray(new String[currentTags.size()]));
        }

        return addedTags;
    }

    public List<String> tag(final JCRNodeWrapper node, final String tag) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(node.getSession().getUser().getUsername(), node.getSession().getWorkspace().getName(),((JCRSessionWrapper)node.getSession()).getLocale(), new JCRCallback<List<String>>() {
            public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                List<String> addedTags = tag(node.getPath(), tag, session);
                session.save();
                return addedTags;
            }
        });
    }

    public List<String> tag(final String nodePath, final String tag) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<String>>() {
            public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                List<String> addedTags = tag(nodePath, tag, session);
                session.save();
                return addedTags;
            }
        });
    }

    public List<String> tag(final String nodePath, final String tag, JCRSessionWrapper session) throws RepositoryException {
         return tag(nodePath, Lists.newArrayList(tag), session);
    }

    public void untag(final JCRNodeWrapper node, final List<String> tags) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(node.getSession().getUser().getUsername(), node.getSession().getWorkspace().getName(),((JCRSessionWrapper)node.getSession()).getLocale(), new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                untag(node.getPath(), tags, session);
                session.save();
                return null;
            }
        });
    }

    public void untag(final String nodePath, final List<String> tags) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                untag(nodePath, tags, session);
                session.save();
                return null;
            }
        });
    }

    public void untag(final String nodePath, final List<String> tags, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper node = session.getNode(nodePath);
        ArrayList<String> currentTags = new ArrayList<String>();
        boolean updated = false;
        if(node.isNodeType(JMIX_TAGGED)){
            try{
                JCRValueWrapper[] currentTagValues = node.getProperty(J_TAG_LIST).getValues();
                currentTags = new ArrayList<String>(Collections2.transform(Arrays.asList(currentTagValues), JCR_VALUE_WRAPPER_STRING_FUNCTION));
                for (String tag : tags){
                    int index = currentTags.indexOf(tag);
                    if(index != -1){
                        currentTags.remove(index);
                        updated = true;
                    }
                }
            } catch (PathNotFoundException e){
                // property not found
            }
        }

        if(updated){
            node.setProperty(J_TAG_LIST, currentTags.toArray(new String[currentTags.size()]));
        }
    }

    public void untag(final JCRNodeWrapper node, final String tag) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(node.getSession().getUser().getUsername(), node.getSession().getWorkspace().getName(),((JCRSessionWrapper)node.getSession()).getLocale(), new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                untag(node.getPath(), tag, session);
                session.save();
                return null;
            }
        });
    }

    public void untag(final String nodePath, final String tag) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                untag(nodePath, tag, session);
                session.save();
                return null;
            }
        });
    }

    public void untag(final String nodePath, final String tag, JCRSessionWrapper session) throws RepositoryException {
        untag(nodePath, Lists.newArrayList(tag), session);
    }

    public Map<String, Long> searchTags(String prefix, String startPath, Long mincount, Long limit, Long offset,
                                        boolean sortByCount, JCRSessionWrapper sessionWrapper) throws RepositoryException {
        LinkedHashMap<String, Long> tagsMap = new LinkedHashMap<String, Long>();

        QueryManager queryManager = sessionWrapper.getWorkspace().getQueryManager();
        if (queryManager == null) {
            logger.error("Unable to obtain QueryManager instance");
            return tagsMap;
        }

        if(StringUtils.isEmpty(startPath)){
            startPath = "/sites";
        }

        StringBuilder facet = new StringBuilder();
        facet.append("rep:facet(nodetype=jmix:tagged&key=j:tagList")
                .append(mincount != null ? "&facet.mincount=" + mincount.toString() : "")
                .append(limit != null ? "&facet.limit=" + limit.toString() : "")
                .append(offset != null ? "&facet.offset=" + offset.toString() : "")
                .append("&facet.sort=").append(String.valueOf(sortByCount))
                .append(StringUtils.isNotEmpty(prefix) ? "&facet.prefix=" + prefix : "")
                .append(")");

        QueryObjectModelFactory factory = queryManager.getQOMFactory();
        QOMBuilder qomBuilder = new QOMBuilder(factory, sessionWrapper.getValueFactory());

        qomBuilder.setSource(factory.selector("jmix:tagged", "tagged"));
        qomBuilder.andConstraint(factory.descendantNode("tagged", startPath));
        qomBuilder.getColumns().add(factory.column("tagged", "j:tagList", facet.toString()));

        QueryObjectModel qom = qomBuilder.createQOM();
        QueryResultWrapper res = (QueryResultWrapper) qom.execute();

        if(res.getFacetField("j:tagList").getValues() != null){
            for(FacetField.Count count : res.getFacetField("j:tagList").getValues()){
                tagsMap.put(count.getName(), count.getCount());
            }
        }

        return tagsMap;
    }
}
