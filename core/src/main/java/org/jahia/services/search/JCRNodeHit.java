/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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

import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.jahia.services.content.JCRContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.render.RenderContext;

/**
 * Search result item, represented by the JCR node. Used as a view object in JSP
 * templates.
 * 
 * @author Sergiy Shyrkov
 */
public class JCRNodeHit extends AbstractHit<JCRNodeWrapper> {

    private static Logger logger = LoggerFactory.getLogger(JCRNodeHit.class);
    
    private String link  = null;

    private Map<String, JCRPropertyWrapper> propertiesFacade;

    private List<AbstractHit<?>> usages;
    
    private JCRNodeWrapper displayableNode = null;
    
    private Set<String> usageFilterSites;
    
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

    public Date getCreated() {
        return resource.getCreationDateAsDate();
    }

    public String getCreatedBy() {
        return resource.getCreationUser();
    }

    public Date getLastModified() {
        return resource.getLastModifiedAsDate();
    }

    public String getLastModifiedBy() {
        return resource.getModificationUser();
    }

    public String getLink() {
        if (link == null) {
            link = context.getURLGenerator().getContext() + resolveURL() + getQueryParameter();
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
        return resource.getDisplayableName();
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
        String type = null;
        try {
            type = resource.getPrimaryNodeTypeName();
        } catch (RepositoryException e) {
            logger.warn("Unable to retrieve primary node type for the resource " + getPath(), e);
        }

        return type;
    }

    public String getIconType() {
        return "";
    }
    
    public List<AbstractHit<?>> getUsages() {
        if (usages == null) {
            usages = Collections.emptyList();
            
            if (shouldRetrieveUsages(resource)) {
                usages = new ArrayList<AbstractHit<?>>();
                Set<String> addedLinks = new HashSet<String>();
                try {
                    for (PropertyIterator it = resource.getWeakReferences(); it.hasNext();) {
                        try {
                            JCRNodeWrapper refNode = (JCRNodeWrapper) it.nextProperty().getParent().getParent();
                            if (usageFilterSites == null || usageFilterSites.contains(refNode.getResolveSite().getName())) {
                                JCRNodeWrapper node = JCRContentUtils.findDisplayableNode(refNode, context);
                                AbstractHit<?> hit = node.isNodeType(Constants.JAHIANT_PAGE) ? new PageHit(node, context) : new JCRNodeHit(
                                        node, context);
                                if (!node.equals(resource) && addedLinks.add(hit.getLink())) {
                                    usages.add(hit);
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
        if (node.getPath().matches("/sites/([^/]+)/(contents|files)/.*")) {
            shouldRetrieveUsages = true;
        }
        return shouldRetrieveUsages;
    }
    
    public JCRNodeWrapper getDisplayableNode() {
        if (displayableNode == null) {
            displayableNode = JCRContentUtils.findDisplayableNode(resource, context);
        }
        return displayableNode;
    }
    
    private String resolveURL() {
        return context.getURLGenerator().buildURL(getDisplayableNode(), getDisplayableNode().getLanguage(), null, "html");
    }

    public void setUsageFilterSites(Set<String> usageFilterSites) {
        this.usageFilterSites = usageFilterSites;
    }

}
