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
 package org.jahia.services.search;


/**
 * Hold informations about a document to remove
 *
 */
public class RemovableDocumentImpl extends IndexableDocumentImpl implements RemovableDocument {

    private static final long serialVersionUID = -8560800710888424767L;
    public RemovableDocumentImpl(){
        super();
    }
    public RemovableDocumentImpl(String keyFieldName, String key){
        super(keyFieldName,key);
    }

}