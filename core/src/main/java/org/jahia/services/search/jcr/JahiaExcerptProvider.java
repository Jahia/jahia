/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.services.search.jcr;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.query.lucene.ExcerptProvider;
import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.ItemStateManager;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Iterator;

/**
 * User: david
 */
public class JahiaExcerptProvider implements ExcerptProvider {

    private static final Logger log = LoggerFactory.getLogger(ExcerptProvider.class);

    
    private final static String JAHIA_NS = "http://www.jahia.org/jahia/1.0";
    public final static String CATEGORY_TYPE = "defaultCategory";
    public final static String TAG_TYPE = "tags";

    private ItemStateManager ism;
    private Query query;

    /**
     * {@inheritDoc}
     */
    public void init(Query query, SearchIndex index) throws IOException {
        ism = index.getContext().getItemStateManager();
        this.query = query;
    }

    /**
     * {@inheritDoc}
     */
    public String getExcerpt(NodeId id, int maxFragments, int maxFragmentSize)
            throws IOException {
        StringBuilder text = new StringBuilder();
        try {
            NodeState nodeState = (NodeState) ism.getItemState(id);
            String separator = "";
            Iterator it = nodeState.getPropertyNames().iterator();
            while (it.hasNext()) {
                PropertyId propId = new PropertyId(id, (Name) it.next());
                PropertyState propState = (PropertyState) ism.getItemState(propId);
                // keep only tags and categories
                Name propStateName = propState.getName();
                if (propStateName.getNamespaceURI().equals(JAHIA_NS) && (propStateName.getLocalName().equals(TAG_TYPE)) || propStateName.getLocalName().equals(CATEGORY_TYPE)) {
                    InternalValue[] values = propState.getValues();
                    for (InternalValue value : values) {
                        text.append(separator);
                        StringBuilder s = new StringBuilder("###"+(propStateName.getLocalName().equals(TAG_TYPE)?TAG_TYPE:CATEGORY_TYPE)+"#");
                        if (query.toString().contains(value.getString())) {
                            s.append("<span class=\"searchHighlightedText\">");
                        }
                        s.append(value.getString());
                        if (query.toString().contains(value.getString())) {
                            s.append("</span>");
                        }
                        s.append("###");
                        separator = ",";
                        text.append(s);
                    }

                }
            }
        } catch (ItemStateException e) {
            // ignore
        } catch (RepositoryException e) {
            log.debug("Error while reading tags or category in search excerpt",e.getMessage());
        }
        return text.toString();
    }
}
