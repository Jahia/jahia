package org.jahia.bin;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.json.JSONObject;

import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
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
            List<Map> results = new ArrayList<Map>();
            while (rows.hasNext()) {
                Row currentRow = rows.nextRow();
                final PropertyIterator stringMap = currentRow.getNode().getProperties();
                Map<String,String > map = new HashMap<String, String>();
                while (stringMap.hasNext()) {
                    JCRPropertyWrapper propertyWrapper = (JCRPropertyWrapper) stringMap.next();
                    final int type = propertyWrapper.getType();
                    final String name = propertyWrapper.getName().replace(":", "_");
                    if(type == PropertyType.WEAKREFERENCE || type == PropertyType.REFERENCE) {
                        if(!propertyWrapper.isMultiple()){
                            map.put(name,((JCRNodeWrapper)propertyWrapper.getNode()).getWebdavUrl());
                        }
                    } else {
                        if(!propertyWrapper.isMultiple()){
                            map.put(name,propertyWrapper.getValue().getString());
                        }
                    }
                }
            }
            JSONObject nodeJSON = new JSONObject(results);
            nodeJSON.write(resp.getWriter());

        }
    }

}
