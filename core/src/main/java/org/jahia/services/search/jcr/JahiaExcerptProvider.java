/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 12/13/11
 * Time: 1:35 PM
 * To change this template use File | Settings | File Templates.
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
        StringBuffer text = new StringBuffer();
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
                        String s = "###"+(propStateName.getLocalName().equals(TAG_TYPE)?TAG_TYPE:CATEGORY_TYPE)+"#";
                        if (query.toString().contains(value.getString())) {
                            s +="<span class=\"searchHighlightedText\">";
                        }
                        s += value.getString();
                        if (query.toString().contains(value.getString())) {
                            s+="</span>";
                        }
                        s += "###";
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
