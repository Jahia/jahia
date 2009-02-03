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

 package org.jahia.services.search.compass;

import org.compass.core.*;
import org.jahia.services.search.*;
import org.springframework.jms.connection.ConnectionFactoryUtils;

import java.util.Iterator;
import java.util.List;
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

        Map fields = doc.getFields ();
        if (fields != null) {
            Iterator iterator = fields.keySet().iterator();
            DocumentField docField = null;
            List vals;
            while (iterator.hasNext()) {
                docField = (DocumentField) fields.get(iterator.next());
                vals = docField.getValues();
                Iterator valsIterator = vals.iterator();
                while ( valsIterator.hasNext() ){
                    String strVal = (String) valsIterator.next();
                    String name = docField.getName();
                    strVal = NumberPadding.pad(strVal);

                    if ( strVal != null ){
                        strVal = strVal.toLowerCase();
                    }
                    if (docField.isKeyword() ) {
                        prop = factory.createProperty(name.toLowerCase(),
                                strVal,Property.Store.YES,Property.Index.UN_TOKENIZED);
                    } else if ( docField.isText() ) {
                        prop = factory.createProperty(name.toLowerCase(),
                                strVal,Property.Store.YES,Property.Index.TOKENIZED);
                    } else if ( docField.isUnindexed() ){
                        prop = factory.createProperty(name.toLowerCase(),
                                strVal,Property.Store.YES,Property.Index.NO);
                    } else if ( docField.isUnstoredText() ){
                        // to simplify highlighting, we actually store field content in index
                        /*
                        prop = template.createProperty(name.toLowerCase(),
                                strVal,Property.Store.NO,Property.Index.TOKENIZED);
                        */
                        prop = factory.createProperty(name.toLowerCase(),
                                strVal,Property.Store.YES,Property.Index.TOKENIZED);
                    }
                    res.addProperty(prop);
                }
            }
        }
    }
}
