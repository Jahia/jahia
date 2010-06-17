package org.jahia.module.social;

import org.apache.log4j.Logger;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Action that retrieves the list of all the activities for the current user and all his connections, sorted by date
 * and limited in size.
 *
 * @author loom
 *         Date: Jun 17, 2010
 *         Time: 2:32:24 PM
 */
public class GetActivitiesAction implements Action {

    private static Logger logger = Logger.getLogger(GetActivitiesAction.class);

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {

        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(
                resource.getWorkspace(), resource.getLocale());

        final JCRNodeWrapper node = resource.getNode();

        QueryManager queryManager = jcrSessionWrapper.getWorkspace().getQueryManager();

        Query myActivitiesQuery = queryManager.createQuery("select * from [jnt:userActivity] as uA where isdescendantnode(uA,['"+node.getPath()+"']) order by [jcr:created] desc", Query.JCR_SQL2);
        myActivitiesQuery.setLimit(100);
        QueryResult myActivitiesResult = myActivitiesQuery.execute();

        NodeIterator myActivitiesNodeIterator = myActivitiesResult.getNodes();

        SortedSet<JCRNodeWrapper> activitiesSet = new TreeSet(new Comparator<JCRNodeWrapper>() {

            public int compare(JCRNodeWrapper activityNode1, JCRNodeWrapper activityNode2) {
                try {
                    return activityNode1.getProperty("jcr:created").getDate().compareTo(activityNode2.getProperty("jcr:created").getDate());
                } catch (RepositoryException e) {
                    logger.error("Error while comparing creation date on two activities, returning them as equal", e);
                    return 0;
                }
            }
            
        });

        while (myActivitiesNodeIterator.hasNext()) {
            activitiesSet.add((JCRNodeWrapper) myActivitiesNodeIterator.nextNode());
        }

        Query myConnectionsQuery = queryManager.createQuery("select * from [jnt:userConnection] as uC where isdescendantnode(uC,['"+node.getPath()+"'])", Query.JCR_SQL2);
        QueryResult myConnectionsResult = myConnectionsQuery.execute();

        NodeIterator myConnectionsIterator = myConnectionsResult.getNodes();
        while (myConnectionsIterator.hasNext()) {
            JCRNodeWrapper myConnectionNode = (JCRNodeWrapper) myConnectionsIterator.nextNode();
            JCRNodeWrapper connectedToNode = (JCRNodeWrapper) myConnectionNode.getProperty("j:connectedTo").getNode();
            Query myConnectionActivitiesQuery = queryManager.createQuery("select * from [jnt:userActivity] as uA where isdescendantnode(uA,['"+connectedToNode.getPath()+"']) order by [jcr:created] desc", Query.JCR_SQL2);
            myConnectionActivitiesQuery.setLimit(100);
            QueryResult myConnectionActivitiesResult = myConnectionActivitiesQuery.execute();

            NodeIterator myConnectionActivitiesIterator = myConnectionActivitiesResult.getNodes();
            while (myConnectionActivitiesIterator.hasNext()) {
                activitiesSet.add((JCRNodeWrapper) myConnectionActivitiesIterator.nextNode());
            }
        }

        JSONArray resultArray = new JSONArray();
        for (JCRNodeWrapper jcrNodeWrapper : activitiesSet) {
            resultArray.put(serializeNode(jcrNodeWrapper, 1, false, null, new HashMap<String, String>()));
        }

        JSONObject results = new JSONObject();
        results.put("resultCount", activitiesSet.size());
        results.put("activities", resultArray);

        return new ActionResult(HttpServletResponse.SC_OK, null, results);
    }

    private JSONObject serializeNode(Node currentNode, int depthLimit, boolean escapeColon, String propertyMatchRegexp, Map<String, String> alreadyIncludedPropertyValues) throws RepositoryException,
            JSONException {
        final PropertyIterator stringMap = currentNode.getProperties();
        JSONObject jsonObject = new JSONObject();
        // Map<String,Object> map = new HashMap<String, Object>();
        Set<String> matchingProperties = new HashSet<String>();
        while (stringMap.hasNext()) {
            JCRPropertyWrapper propertyWrapper = (JCRPropertyWrapper) stringMap.next();
            final int type = propertyWrapper.getType();
            final String name = escapeColon ? propertyWrapper.getName().replace(":", "_") : propertyWrapper.getName();
            if (type == PropertyType.WEAKREFERENCE || type == PropertyType.REFERENCE) {
                if (!propertyWrapper.isMultiple()) {
                    jsonObject.put(name, ((JCRNodeWrapper) propertyWrapper.getNode()).getUrl());
                }
            } else {
                if (!propertyWrapper.isMultiple()) {
                    jsonObject.put(name, propertyWrapper.getValue().getString());
                    // @todo this code is duplicated for multiple values, we need to clean this up.
                    if ((propertyMatchRegexp != null) && (propertyWrapper.getValue().getString().matches(propertyMatchRegexp))) {
                        if (alreadyIncludedPropertyValues != null) {
                            String nodeIdentifier = alreadyIncludedPropertyValues.get(propertyWrapper.getValue().getString());
                            if (nodeIdentifier != null) {
                                if (!nodeIdentifier.equals(currentNode.getIdentifier())) {
                                    // This property value already exists and comes from another node.
                                    return null;
                                }
                            } else {
                                alreadyIncludedPropertyValues.put(propertyWrapper.getValue().getString(), currentNode.getIdentifier());
                            }
                        }
                        // property starts with the propertyMatchRegexp, let's add it to the list of matching properties.
                        matchingProperties.add(name);
                    }
                } else {
                    JSONArray jsonArray = new JSONArray();
                    Value[] propValues = propertyWrapper.getValues();
                    for (Value propValue : propValues) {
                        jsonArray.put(propValue.getString());
                        if ((propertyMatchRegexp != null) && (propValue.getString().matches(propertyMatchRegexp))) {
                            if (alreadyIncludedPropertyValues != null) {
                                String nodeIdentifier = alreadyIncludedPropertyValues.get(propValue.getString());
                                if (nodeIdentifier != null) {
                                    if (!nodeIdentifier.equals(currentNode.getIdentifier())) {
                                        // This property value already exists and comes from another node.
                                        return null;
                                    }
                                } else {
                                    alreadyIncludedPropertyValues.put(propValue.getString(), currentNode.getIdentifier());
                                }
                            }
                            // property starts with the propertyMatchRegexp, let's add it to the list of matching properties.
                            matchingProperties.add(name);
                        }
                    }
                    jsonObject.put(name, jsonArray);
                }
            }
        }
        // now let's output some node information.
        jsonObject.put("path", currentNode.getPath());
        jsonObject.put("identifier", currentNode.getIdentifier());
        jsonObject.put("index", currentNode.getIndex());
        jsonObject.put("depth", currentNode.getDepth());
        jsonObject.put("primaryNodeType", currentNode.getPrimaryNodeType().getName());
        if (propertyMatchRegexp != null) {
            jsonObject.put("matchingProperties", new JSONArray(matchingProperties));
        }

        // now let's output the children until we reach the depth limit.
        if ((depthLimit - 1) > 0) {
            final NodeIterator childNodeIterator = currentNode.getNodes();
            JSONArray childMapList = new JSONArray();
            while (childNodeIterator.hasNext()) {
                Node currentChildNode = childNodeIterator.nextNode();
                JSONObject childSerializedMap = serializeNode(currentChildNode, depthLimit - 1, escapeColon, propertyMatchRegexp, alreadyIncludedPropertyValues);
                childMapList.put(childSerializedMap);
            }
            jsonObject.put("childNodes", childMapList);
        }
        return jsonObject;
    }

}
