/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.search.jcr;

import org.apache.lucene.document.Field;
//import org.apache.slide.content.NodeProperty;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.CompassException;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneProperty;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.util.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;

import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 21 f�vr. 2006
 * Time: 11:56:11
 * To change this template use File | Settings | File Templates.
 */
public class JcrResourceForHighLighting implements Resource {

    private Map properties = new HashMap();

    private String aliasProperty;

    private transient LuceneSearchEngine searchEngine;

    private float boost = 1.0f;

    public JcrResourceForHighLighting(JCRNodeWrapper file,
                                        LuceneSearchEngine searchEngine) {
        this.searchEngine = searchEngine;
        this.aliasProperty = searchEngine.getSearchEngineFactory()
                .getLuceneSettings().getAliasProperty();

        if (file.isValid() && !file.isCollection()) {

//            try {
//                long lastModifiedDate = System.currentTimeMillis();
//                String contentType = "";
//                properties = file.getProperties();
//                NodeProperty nodeProperty = (NodeProperty)properties.get("DAV:getcontenttype");
//                if ( nodeProperty != null ){
//                    contentType = (String)nodeProperty.getValue();
//                }
//                nodeProperty = (NodeProperty)properties.get("DAV:"+NodeRevisionDescriptor.LAST_MODIFIED);
//                if ( nodeProperty != null ){
//                    String lastModified  = (String)nodeProperty.getValue();
//                    try {
//                    } catch ( Exception t){
//                    }
//                }
//                if ( contentType != null ){
//                    InputStream ins = file.downloadFile();
//                    ExtractedDocument extDoc = ServicesRegistry.getInstance().getFileExtractionService()
//                        .getExtractedDocument(contentType, file.getPath(), lastModifiedDate,
//                                true, ins);
//                    ins.close();
//                    if (extDoc != null){
//                        properties.put(org.apache.slide.index.lucene.Index.CONTENT_FIELD_NAME,
//                                new NodeProperty(org.apache.slide.index.lucene.Index.CONTENT_FIELD_NAME, extDoc.getContentAsString().replaceAll("\\<.*?\\>","")));
//                    }
//                }
//            } catch (Exception e) {
//              logger.error(e.getMessage(), e);
//            }
        }
    }

    public Map getDAVProperties(){
        return this.properties;
    }

    public String get(String name) {
//        NodeProperty nodeProp = (NodeProperty)properties.get(name);
//        if ( nodeProp != null ){
//            return (String)nodeProp.getValue();
//        }
        return null;
    }

    public String[] getValues(String name) {
        String[] vals = new String[1];
        String val = get(name);
        if ( val == null ){
            return new String[0];
        }
        vals[0]=val;
        return vals;
    }

    public String getAlias() {
        Property alias = getProperty(aliasProperty);
        if (alias == null) {
            return null;
        }
        return alias.getStringValue();
    }

    public Resource setAlias(String alias) {
        removeProperties(aliasProperty);
        Property aliasProp = new LuceneProperty(new Field(aliasProperty, alias, Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        addProperty(aliasProp);
        return this;
    }

    public Resource addProperty(String name, Object value) throws SearchEngineException {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Resource addProperty(String name, Reader value) throws SearchEngineException {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Resource addProperty(Property property) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Resource removeProperty(String name) {
        this.properties.remove(name);
        return this;
    }

    public Resource removeProperties(String name) {
        removeProperty(name);
        return this;
    }

    public Property getProperty(String name) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Property[] getProperties(String name) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Property[] getProperties() {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public float getBoost() {
        return this.boost;
    }

    public Resource setBoost(float boost) {
        this.boost = boost;
        return this;
    }

    public String toString() {
        return "{" + getAlias() + "} " + StringUtils.arrayToCommaDelimitedString(getProperties());
    }

    // methods from the Map interface
    // ------------------------------

    public void clear() {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Set entrySet() {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public void putAll(Map t) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Set keySet() {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public boolean containsKey(Object key) {
        return getProperty(key.toString()) != null;
    }

    public int size() {
        return this.properties.size();
    }

    public boolean isEmpty() {
        return this.properties.isEmpty();
    }

    public Collection values() {
        return this.properties.values();
    }

    public Object get(Object key) {
        return getProperties(key.toString());
    }

    public void copy(org.compass.core.Resource resource){
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }
    
    public Object getObject(String key) {
//        NodeProperty nodeProp = (NodeProperty)properties.get(key);
//        if ( nodeProp != null ){
//            return nodeProp.getValue();
//        }
        return null;        
    }      
    
    public String getId() {
        throw new UnsupportedOperationException("Should not be called");
    }

    public Property[] getIdProperties() {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Property getIdProperty() {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public String[] getIds() {
        throw new UnsupportedOperationException("Should not be called");
    }

    public String getUID() throws CompassException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getValue(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object[] getObjects(String s) {
        return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Resource setProperty(String s, Object o) throws SearchEngineException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Resource setProperty(String s, Reader reader) throws SearchEngineException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Resource setProperty(Property property) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
