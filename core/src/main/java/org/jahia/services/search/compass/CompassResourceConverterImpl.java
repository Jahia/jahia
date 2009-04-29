/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.search.compass;

import org.compass.core.*;
import org.jahia.services.search.*;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 18 janv. 2006
 * Time: 16:00:47
 * To change this template use File | Settings | File Templates.
 */
public class CompassResourceConverterImpl implements CompassResourceConverter {

    private Compass compass;
    
    public CompassResourceConverterImpl(){
    }

    public Compass getCompass() {
        return compass;
    }

    public void setCompass(Compass compass) {
        this.compass = compass;
    }

    public Resource getResourceFromIndexableDocument(IndexableDocument doc) {

        ResourceFactory factory = compass.getResourceFactory();

        Resource res = null;
        if ( doc instanceof JahiaContainerIndexableDocument ) {
            res = factory.createResource("jahiacontainer");
            buildJahiaResource(res,doc,factory);
        } else if ( doc instanceof JahiaPageIndexableDocument ){
            res = factory.createResource("jahiapage");
            buildJahiaResource(res,doc,factory);
        } else if ( doc instanceof JahiaFieldIndexableDocument ){
            res = factory.createResource("jahiafield");
            buildJahiaResource(res,doc,factory);
        }
        buildJahiaResource(res,doc,factory);
        return res;
    }

    protected void buildJahiaResource(Resource res,
                                      IndexableDocument doc,
                                      ResourceFactory factory){

        Property prop = factory.createProperty(doc.getKeyFieldName(),
            NumberPadding.pad(doc.getKey()),
            Property.Store.YES,Property.Index.UN_TOKENIZED);
        res.addProperty(prop);

        Map<String, DocumentField> fields = doc.getFields ();
        if (fields != null) {
            for (DocumentField docField : fields.values()) {
                for ( String strVal : docField.getValues() ){
                    String name = docField.getName();
                    strVal = NumberPadding.pad(strVal);

                    if ( strVal != null ){
                        strVal = strVal.toLowerCase();
                    }
                    if (docField.isKeyword() ) {
                        prop = factory.createProperty(name.toLowerCase(),
                                strVal,docField.isUnstored() ? Property.Store.NO : Property.Store.YES,Property.Index.UN_TOKENIZED);
                    } else if ( docField.isText() ) {
                        prop = factory.createProperty(name.toLowerCase(),
                                strVal,docField.isUnstored() ? Property.Store.NO : Property.Store.YES,Property.Index.TOKENIZED);
                    } else if ( docField.isUnindexed() ){
                        prop = factory.createProperty(name.toLowerCase(),
                                strVal,Property.Store.YES,Property.Index.NO);
                    }
                    res.addProperty(prop);
                }
            }
        }
    }
}
