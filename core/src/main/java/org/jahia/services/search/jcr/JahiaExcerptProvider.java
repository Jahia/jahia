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
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
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
