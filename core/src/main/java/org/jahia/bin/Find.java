package org.jahia.bin;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.*;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * A small servlet to allow us to perform queries on the JCR. 
 * User: loom
 * Date: Jan 26, 2010
 * Time: 5:55:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class Find extends Render {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp, RenderContext renderContext, String path, String workspace, Locale locale) throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale);
        List<String> users = new ArrayList<String>();
        if (session.getWorkspace().getQueryManager() != null) {
            String query = req.getParameter("query");
            String language = req.getParameter("language");
            if (language == null) {
                language = javax.jcr.query.Query.JCR_SQL2; 
            }
            javax.jcr.query.Query q = session.getWorkspace().getQueryManager().createQuery(query, javax.jcr.query.Query.JCR_SQL2);
            QueryResult qr = q.execute();
            RowIterator rows = qr.getRows();
            JSONArray results = new JSONArray();
            while (rows.hasNext()) {
                Row currentRow = rows.nextRow();
                JSONObject serializedMap = serializeNode(currentRow.getNode());
                results.put(serializedMap);
            }
            results.write(resp.getWriter());

        }
    }

    private JSONObject serializeNode(Node currentNode) throws RepositoryException, JSONException {
        final PropertyIterator stringMap = currentNode.getProperties();
        JSONObject jsonObject = new JSONObject();
        // Map<String,Object> map = new HashMap<String, Object>();
        while (stringMap.hasNext()) {
            JCRPropertyWrapper propertyWrapper = (JCRPropertyWrapper) stringMap.next();
            final int type = propertyWrapper.getType();
            final String name = propertyWrapper.getName().replace(":", "_");
            if(type == PropertyType.WEAKREFERENCE || type == PropertyType.REFERENCE) {
                if(!propertyWrapper.isMultiple()){
                    jsonObject.put(name,((JCRNodeWrapper)propertyWrapper.getNode()).getWebdavUrl());
                }
            } else {
                if(!propertyWrapper.isMultiple()){
                    jsonObject.put(name,propertyWrapper.getValue().getString());
                }
            }
        }
        final NodeIterator childNodeIterator = currentNode.getNodes();
        JSONArray childMapList = new JSONArray();
        while (childNodeIterator.hasNext()) {
            Node currentChildNode = childNodeIterator.nextNode();
            JSONObject childSerializedMap = serializeNode(currentChildNode);
            childMapList.put(childSerializedMap);
        }
        jsonObject.put("childNodes", childMapList);
        return jsonObject;
    }

}
