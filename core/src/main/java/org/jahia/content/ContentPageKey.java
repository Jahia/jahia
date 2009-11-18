/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Title:        Jahia
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 * @author Serge Huber
 * @version 1.0
 */

public class  ContentPageKey extends ContentObjectKey {

    private static final long serialVersionUID = -3710517391444085081L;
    
    private static final transient Logger logger = Logger.getLogger(ContentPageKey.class);    

    public static final String PAGE_TYPE = "ContentPage";

    private transient int pageType = -1;

    /**
     * Protected constructor to use this class also as a factory by calling
     * the getChildInstance method
     */
    protected ContentPageKey() {
    }
    
    public ContentPageKey(int pageID, String objectKey) {
        super(PAGE_TYPE, Integer.toString(pageID), objectKey);
    }

    public ContentPageKey(int pageID) {
        super(PAGE_TYPE, Integer.toString(pageID) );
    }

    public int getPageID() {
        return getIdInType();
    }

    public ObjectKey getChildInstance(String IDInType, String objectKey) {
        return new ContentPageKey(Integer.parseInt(IDInType), objectKey);
    }

    public String getType() {
        return PAGE_TYPE;
    }

    public int getPageType() {
        return pageType;
    }

    void setPageType(int pageType) {
        this.pageType = pageType;
    }

    public ContentObjectKey getParent(EntryLoadRequest request) {
        checkCache();
        ContentObjectKey p = (ContentObjectKey) treeCache.get("parent"+toString()+"-"+request.getWorkflowState());
        if (p != null) {
            return p;
        }

        int parentPageFieldId = ServicesRegistry.getInstance().getJahiaPageService().getParentPageFieldId(getIdInType(), request);
        if (parentPageFieldId > 0) {
            p = new ContentFieldKey(parentPageFieldId);
            treeCache.put("parent"+toString()+"-"+request.getWorkflowState(), p);
        }
        return p;
    }

    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request) {
        return null;
    }
    
    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request, int loadFlag) {
        Collection<ContentObjectKey> results = new ArrayList<ContentObjectKey>();
        return results;
    }

    public Collection<ContentObjectKey> getChildLists(EntryLoadRequest request) {
        List<ContentObjectKey> results = new ArrayList<ContentObjectKey>();
        return results;
    }    
    
    public static String toObjectKeyString(String idInType) {
        return toObjectKeyString(PAGE_TYPE, idInType);
    }

    public static String toObjectKeyString(int idInType) {
        return toObjectKeyString(PAGE_TYPE, Integer.toString(idInType));
    }

}