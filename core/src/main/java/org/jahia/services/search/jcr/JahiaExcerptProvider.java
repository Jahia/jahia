/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
