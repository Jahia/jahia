package org.jahia.modules.social;

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
import org.jahia.utils.i18n.JahiaResourceBundle;
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

    public static final String TREATASRESOURCEKEY_PARAMNAME = "treatAsResourceKey";
    public static final String MODULE_NAME = "Jahia Social Module";

    private String name;

    private SocialService socialService;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SocialService getSocialService() {
        return socialService;
    }

    public void setSocialService(SocialService socialService) {
        this.socialService = socialService;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {

        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(
                resource.getWorkspace(), resource.getLocale());

        final JCRNodeWrapper node = resource.getNode();
        ResourceBundle jahiaResourceBundle = new JahiaResourceBundle(resource.getLocale(), MODULE_NAME);

        SortedSet<JCRNodeWrapper> activitiesSet = socialService.getActivities(jcrSessionWrapper, node);

        String treatAsResourceKeyValue = req.getParameter(TREATASRESOURCEKEY_PARAMNAME);
        Set<String> treatAsResourceKeyPropertyNames = new HashSet<String>();
        if (treatAsResourceKeyValue != null) {
            String[] resourceKeyPropNames = treatAsResourceKeyValue.split(",");
            for (String curPropName : resourceKeyPropNames) {
                treatAsResourceKeyPropertyNames.add(curPropName);
            }
        }

        JSONArray resultArray = new JSONArray();
        for (JCRNodeWrapper jcrNodeWrapper : activitiesSet) {
            resultArray.put(serializeNode(jcrNodeWrapper, 1, false, null, new HashMap<String, String>(), treatAsResourceKeyPropertyNames, jahiaResourceBundle));
        }

        JSONObject results = new JSONObject();
        results.put("resultCount", activitiesSet.size());
        results.put("activities", resultArray);

        return new ActionResult(HttpServletResponse.SC_OK, null, results);
    }


    private JSONObject serializeNode(Node currentNode, int depthLimit, boolean escapeColon, String propertyMatchRegexp, Map<String, String> alreadyIncludedPropertyValues, Set<String> treatAsResourceKeyPropertyNames, ResourceBundle bundle) throws RepositoryException,
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
                    jsonObject.put(name, ((JCRNodeWrapper) propertyWrapper.getNode()).getPath());
                }
            } else {
                if (!propertyWrapper.isMultiple()) {
                    if (propertyWrapper.getType() == PropertyType.DATE) {
                        jsonObject.put(name, Long.toString(propertyWrapper.getValue().getDate().getTimeInMillis()));
                    } else {
                        String propValue = propertyWrapper.getValue().getString();
                        if (treatAsResourceKeyPropertyNames.contains(name)) {
                            String resourceValue = bundle.getString(propValue);
                            if (resourceValue != null) {
                                propValue = resourceValue;
                            }
                        }
                        jsonObject.put(name, propValue);
                    }
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
                        if (propValue.getType() == PropertyType.DATE) {
                            jsonArray.put(Long.toString(propValue.getDate().getTimeInMillis()));
                        } else {
                            String propValueString = propValue.getString();
                            if (treatAsResourceKeyPropertyNames.contains(name)) {
                                String resourceValue = bundle.getString(propValueString);
                                if (resourceValue != null) {
                                    propValueString = resourceValue;
                                }
                            }
                            jsonArray.put(propValueString);
                        }
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
                JSONObject childSerializedMap = serializeNode(currentChildNode, depthLimit - 1, escapeColon, propertyMatchRegexp, alreadyIncludedPropertyValues, treatAsResourceKeyPropertyNames, bundle);
                childMapList.put(childSerializedMap);
            }
            jsonObject.put("childNodes", childMapList);
        }
        return jsonObject;
    }

}
