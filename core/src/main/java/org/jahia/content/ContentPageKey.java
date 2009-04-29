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
 package org.jahia.content;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.exceptions.JahiaException;
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

    static {
        ObjectKey.registerType(PAGE_TYPE, ContentPageKey.class);
    }

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

    /**
     * @deprecated This method should not be called directly, but rather it
     * should be replace by a call to the constructor with the proper IDInType.
     * This has been deprecated because the new getChildInstance() is much
     * faster
     * @param IDInType the IDInType
     * @return the ObjectKey corresponding to the ID for this class type
     */
    public static ObjectKey getChildInstance(String IDInType) {
        return new ContentPageKey(Integer.parseInt(IDInType));
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
        return getChilds(request, JahiaContainerStructure.ALL_TYPES);
    }
    
    public Collection<ContentObjectKey> getChilds(EntryLoadRequest request, int loadFlag) {
        Collection<ContentObjectKey> results = new ArrayList<ContentObjectKey>();
        if ((loadFlag & JahiaContainerStructure.JAHIA_CONTAINER) != 0) {
            results = getChildLists(request);
        }
        if ((loadFlag & JahiaContainerStructure.JAHIA_FIELD) != 0
                && org.jahia.settings.SettingsBean.getInstance().areDeprecatedNonContainerFieldsUsed()) {
            try {
                for (Object[] o : ServicesRegistry.getInstance().getJahiaFieldService()
                        .getNonContainerFieldIDsAndTypesInPageByWorkflowState(getIdInType(), request)) {
                    ContentFieldKey key = new ContentFieldKey(((Integer) o[0])
                            .intValue());
                    key.setFieldType(((Integer) o[1]).intValue());
                    key.setParent(this);
                    results.add(key);
                }
            } catch (JahiaException e) {
                logger.warn("Exception getting page children", e);
            }
        }
        return results;
    }

    public Collection<ContentObjectKey> getChildLists(EntryLoadRequest request) {
        List<ContentObjectKey> results = new ArrayList<ContentObjectKey>();
        try {
            for (Integer integer : ServicesRegistry.getInstance().getJahiaContainersService()
                    .getAllPageTopLevelContainerListIDs(getIdInType(), request)) {
                ContentContainerListKey cclk = new ContentContainerListKey(integer.intValue());
                cclk.setParent(this);
                results.add(cclk);
            }
        } catch (JahiaException e) {
            logger.warn("Exception getting page children container lists", e);
        }
        return results;
    }    
    
    public static String toObjectKeyString(String idInType) {
        return toObjectKeyString(PAGE_TYPE, idInType);
    }

    public static String toObjectKeyString(int idInType) {
        return toObjectKeyString(PAGE_TYPE, Integer.toString(idInType));
    }

}