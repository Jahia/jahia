package org.jahia.modules.tags.webflow;

import org.jahia.services.content.*;
import org.jahia.services.query.ScrollableQuery;
import org.jahia.services.query.ScrollableQueryCallback;
import org.jahia.services.render.RenderContext;
import org.slf4j.Logger;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.io.Serializable;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by dgaillard on 05/11/14.
 */
public class TagsFlowHandler implements Serializable {

    private static final long serialVersionUID = -3325519642397714386L;

    private static final Logger logger = getLogger(TagsFlowHandler.class);

    public Map<String,Integer> getTagsList(RenderContext renderContext) {
        String query = "SELECT * FROM [nt:base] AS result WHERE ISDESCENDANTNODE(result, '" + renderContext.getSite().getPath() + "') AND (result.[j:tagList] IS NOT NULL)";

        try {
            JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query q = qm.createQuery(query, Query.JCR_SQL2);
            ScrollableQuery scrollableQuery = new ScrollableQuery(100, q);

            return scrollableQuery.execute(new ScrollableQueryCallback<Map<String, Integer>>() {
                Map<String, Integer> result = new HashMap<String, Integer>();
                @Override
                public boolean scroll() throws RepositoryException {
                    NodeIterator nodeIterator = stepResult.getNodes();
                    while (nodeIterator.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.next();
                        JCRValueWrapper[] tags = nodeWrapper.getProperty("j:tagList").getValues();
                        for (JCRValueWrapper tag : tags) {
                            String tagValue = tag.getString();

                            if (result.containsKey(tagValue)) {
                                result.put(tagValue, result.get(tagValue) + 1);
                            }else {
                                // limit reached
                                result.put(tagValue, 1);
                            }
                        }
                    }
                    return true;
                }

                @Override
                protected Map<String, Integer> getResult() {
                    return result;
                }
            });
        } catch (RepositoryException e) {
            logger.error("getTags() cannot get Tags List");
            return new HashMap<String, Integer>();
        }
    }

    public void renameAllTags(RenderContext renderContext, String selectedTag, String tagNewName) {
        logger.info("Selected Tag : " + selectedTag);
        logger.info("Tag New Name : " + tagNewName);

        try {
            JCRSessionWrapper session = renderContext.getMainResource().getNode().getSession();

            String query = "SELECT * FROM [nt:base] AS result WHERE ISDESCENDANTNODE(result, '" + renderContext.getSite().getPath() + "') AND (result.[j:tagList] = '" + selectedTag + "')";
            QueryManager qm = session.getWorkspace().getQueryManager();
            Query q = qm.createQuery(query, Query.JCR_SQL2);
            NodeIterator ni = q.execute().getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper node = (JCRNodeWrapper)ni.nextNode();
                Set<String> newValues = new TreeSet<String>();
                JCRValueWrapper[] tags = node.getProperty("j:tagList").getValues();
                for (JCRValueWrapper tag : tags) {
                    String tagValue = tag.getString();
                    if (!tagValue.equals(selectedTag)) {
                        newValues.add(tagValue);
                    }
                }
                if (!newValues.contains(tagNewName)) {
                    newValues.add(tagNewName);
                }
                node.setProperty("j:tagList", newValues.toArray(new String[newValues.size()]));
            }
            session.save();
        } catch (RepositoryException e) {
            logger.error("renameAllTags() cannot rename all tags '" + selectedTag + "' by '" + tagNewName + "'");
        }
    }
}
