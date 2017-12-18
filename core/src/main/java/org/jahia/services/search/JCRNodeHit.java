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
package org.jahia.services.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.jcr.JahiaExcerptProvider;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Search result item, represented by the JCR node. Used as a view object in JSP
 * templates.
 *
 * @author Sergiy Shyrkov
 */
public class JCRNodeHit extends AbstractHit<JCRNodeWrapper> {

    private static final Pattern SITES_CONTENTS_FILES = Pattern.compile("/sites/([^/]+)/(contents|files)/.*");

    private static Logger logger = LoggerFactory.getLogger(JCRNodeHit.class);

    private String link  = null;

    private Map<String, JCRPropertyWrapper> propertiesFacade;

    private List<Hit> usages;

    private JCRNodeWrapper displayableNode = null;
    private Node foundNode = null;
    private boolean isDisplayableNodeChecked = false;

    private Set<String> usageFilterSites;
    private String excerpt;
    private List<Row> rows = null;

    /**
     * Initializes an instance of this class.
     *
     * @param node search result item to be wrapped
     * @param context
     */
    public JCRNodeHit(JCRNodeWrapper node, RenderContext context) {
        super(node, context);
    }

    public String getContentType() {
        return "text/html";
    }

    /**
     * Because of our i18n model, it could be that search via JackRabbit index either finds the main or the
     * translation node. As both could have different metadata values and to provide consistent list order when
     * ordering on metadata, we need to retrieve the metadata from the found node. The node stored in resource
     * variable is always the main one.
     * @return the Node found by Jackrabbit search (either main or translation node)
     */
    private Node getFoundNode() throws RepositoryException {
        if (foundNode == null) {
            foundNode = resource;
            if (getRows() != null) {
                // If using ElasticSearch module, JCR row objects are not set to the hit, so we continue using the main node
                String path = getRows().get(0).getPath();
                if (!resource.getPath().equals(path)) {
                    foundNode = resource.getSession().getNode(path);
                }
            }
        }
        return foundNode;
    }

    public Date getCreated() {
        try {
            return getFoundNode().getProperty(Constants.JCR_CREATED).getDate().getTime();
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    public String getCreatedBy() {
        try {
            return getFoundNode().getProperty(Constants.JCR_CREATEDBY).getString();
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    public Date getLastModified() {
        try {
            return getFoundNode().getProperty(Constants.JCR_LASTMODIFIED).getDate().getTime();
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    public String getLastModifiedBy() {
        try {
            return getFoundNode().getProperty(Constants.JCR_LASTMODIFIEDBY).getString();
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    public String getLink() {
        if (link == null && getDisplayableNode() != null) {
            link = context.getURLGenerator().getContext() + resolveURL(getLinkTemplateType()) + getQueryParameter();
        }
        return link;
    }

    public String getPath() {
        return resource.getPath();
    }

    @SuppressWarnings("unchecked")
    public Map<String, JCRPropertyWrapper> getProperties() {

        if (propertiesFacade == null) {
            propertiesFacade = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {

                private Map<Object, JCRPropertyWrapper> accessedProperties = new HashMap<Object, JCRPropertyWrapper>();

                public Object transform(Object input) {
                    JCRPropertyWrapper property = null;
                    if (accessedProperties.containsKey(input)) {
                        property = accessedProperties.get(input);
                    } else {
                        try {
                            property = resource.getProperty(String.valueOf(input));
                        } catch (RepositoryException e) {
                            logger.warn("Error accessing property '" + input + "'.", e);
                        }
                        accessedProperties.put(input, property);
                    }
                    return property;
                }
            });
        }

        return propertiesFacade;
    }

    public String getTitle() {
        if (getDisplayableNode() == null) {
            return null;
        }
        return getDisplayableNode().getDisplayableName();
    }

    /**
     * Returns the name of this file or folder.
     *
     * @return the name of this file or folder
     */
    public String getName() {
        return resource.getName();
    }

    public String getType() {
        try {
            return resource.getPrimaryNodeTypeName();
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException("Unable to retrieve primary node type for the resource " + getPath(), e);
        }
    }

    public String getIconType() {
        return "";
    }

    public List<Hit> getUsages() {
        if (usages == null) {
            usages = Collections.emptyList();

            if (shouldRetrieveUsages(resource)) {
                usages = new ArrayList<>();
                Set<String> addedLinks = new HashSet<>();
                try {
                    for (PropertyIterator it = resource.getWeakReferences(); it.hasNext();) {
                        try {
                            JCRNodeWrapper refNode = (JCRNodeWrapper) it.nextProperty().getParent().getParent();
                            if (usageFilterSites == null || usageFilterSites.contains(refNode.getResolveSite().getName())) {
                                JCRNodeWrapper node = JCRContentUtils.findDisplayableNode(refNode, context, refNode.getResolveSite());
                                if (node != null) {
                                    AbstractHit<?> hit = node.isNodeType(Constants.JAHIANT_PAGE) ? new PageHit(node, context)
                                            : new JCRNodeHit(node, context);
                                    if (!node.equals(resource) && addedLinks.add(hit.getLink())) {
                                        usages.add(hit);
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                } catch (RepositoryException e) {
                }
            }
        }
        return usages;
    }

    private boolean shouldRetrieveUsages(JCRNodeWrapper node) {
        boolean shouldRetrieveUsages = false;
        if (SITES_CONTENTS_FILES.matcher(node.getPath()).matches()) {
            shouldRetrieveUsages = true;
        }
        return shouldRetrieveUsages;
    }

    public JCRNodeWrapper getDisplayableNode() {
        if (!isDisplayableNodeChecked && displayableNode == null) {
            isDisplayableNodeChecked = true;
            JCRSiteNode site;
            try {
                site = resource.getResolveSite();
            } catch (RepositoryException e) {
                site = null;
            }
            displayableNode = JCRContentUtils.findDisplayableNode(resource, context, site);
            if (displayableNode == null && !CollectionUtils.isEmpty(getUsages())) {
                 displayableNode = ((JCRNodeHit)getUsages().get(0)).getDisplayableNode();
            }
            //search.displayableNodeCompat... don't allow to return null, if customers has own searchprovider implementations
            if (displayableNode == null
                  && Boolean.valueOf(SettingsBean.getInstance().getPropertiesFile().getProperty("search.displayableNodeCompat"))) {
                displayableNode = resource;
            }
        }
        return displayableNode;
    }

    private String resolveURL(String templateType) {
        if (getDisplayableNode() != null) {
            return context.getURLGenerator().buildURL(getDisplayableNode(), getDisplayableNode().getLanguage(), null, templateType);
        }
        return null;
    }

    public void setUsageFilterSites(Set<String> usageFilterSites) {
        this.usageFilterSites = usageFilterSites;
    }

    public String getExcerpt() {
        if (excerpt == null && rows != null) {
            try {
                // this is Jackrabbit specific, so if other implementations
                // throw exceptions, we have to do a check here
                for (Row row : rows) {
                    Value excerptValue = row.getValue("rep:excerpt(.)");
                    if (excerptValue != null) {
                        if (excerptValue.getString().contains(
                                "###" + JahiaExcerptProvider.TAG_TYPE + "#")
                                || excerptValue.getString().contains(
                                "###" + JahiaExcerptProvider.CATEGORY_TYPE
                                        + "#")) {
                            StringBuilder r = new StringBuilder();
                            String separator = "";
                            String type = "";
                            for (String s : Patterns.COMMA.split(excerptValue
                                    .getString())) {
                                String s2 = Messages.getInternal(s
                                        .contains(JahiaExcerptProvider.TAG_TYPE) ? "label.tags"
                                        : "label.category", context.getRequest().getLocale());
                                String s1 = s.substring(s.indexOf("###"),
                                        s.lastIndexOf("###"));
                                String identifier = s1.substring(s1
                                        .lastIndexOf("#") + 1);
                                String v = "";
                                if (identifier.startsWith("<span")) {
                                    identifier = identifier.substring(
                                            identifier.indexOf(">") + 1,
                                            identifier.lastIndexOf("</span>"));
                                    v = "<span class=\" searchHighlightedText\">"
                                            + getTitle() + "</span>";
                                } else {
                                    v = getTitle();
                                }
                                if (!type.equals(s2)) {
                                    r.append(s2).append(":");
                                    type = s2;
                                    separator = "";
                                }
                                r.append(separator).append(v);
                                separator = ", ";

                            }
                            setExcerpt(r.toString());
                            break;
                        } else if (!StringUtils.isEmpty(excerptValue.getString())) {
                            setExcerpt(excerptValue.getString());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Search details cannot be retrieved", e);
            }
        }
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public void addRow(Row row) {
        if (this.rows == null) {
            this.rows = new ArrayList<>();
        }
        rows.add(row);
    }

    /**
     * Returns the row objects from the query/search linked to this hit. Multiple query results (row) can be linked to a
     * hit, because some nodes cannot be displayed on its own as they have no template, so the hit's link URL points to a
     * parent node having a template, which can aggregate several sub-nodes.
     *
     * @return list of Row objects
     */
    public List<Row> getRows() {
        return rows;
    }
}
