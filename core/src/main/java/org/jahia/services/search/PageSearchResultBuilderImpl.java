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
 package org.jahia.services.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.PageReferenceableInterface;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.search.JahiaSearchHit;
import org.jahia.data.search.JahiaSearchHitInterface;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.fields.ContentBigTextField;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFileField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.search.lucene.JahiaHitCollector;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;

/**
 * Default implementation to build JahiaSearchResult from a collection of ParsedObject
 * 
 * @author NK
 */
public class PageSearchResultBuilderImpl extends
        JahiaAbstractSearchResultBuilder {

    private static Logger logger = Logger
            .getLogger(PageSearchResultBuilderImpl.class);

    public static final String ONLY_ONE_HIT_BY_PAGE = "PageSearchResultBuilder.onlyOneHitByPage";

    public static final String ONE_HIT_BY_PAGE_PARAMETER_NAME = ONLY_ONE_HIT_BY_PAGE;

    private boolean onlyOneHitByPage = true;
    private boolean checkPageACL = true;

    private boolean checkContentObjectACL = true;

    private GroupingHitsHandler groupingHitsHandler = new GroupingHitsHandler();

    public PageSearchResultBuilderImpl() {
        this(true);
    }

    public PageSearchResultBuilderImpl(boolean onlyOneHitByPage) {
        this.setDiscardLuceneDoc(false);
        this.onlyOneHitByPage = onlyOneHitByPage;
        Properties searchConfig = ServicesRegistry.getInstance()
                .getJahiaSearchService().getConfig();

        this.setHitCollector(new JahiaHitCollector(this.onlyOneHitByPage,
                Integer.parseInt(searchConfig
                        .getProperty("searchMaxLuceneDocs")), Integer
                        .parseInt(searchConfig.getProperty("searchMaxHits")),
                Integer.parseInt(searchConfig
                        .getProperty("searchMaxHitsByPage")), Integer
                        .parseInt(searchConfig.getProperty("searchMaxPages"))));
        this.getHitCollector().setSearchResultBuilder(this);
    }

    public boolean getOnlyOneHitByPage() {
        return onlyOneHitByPage;
    }

    public void setOnlyOneHitByPage(boolean onlyOneHitByPage) {
        this.onlyOneHitByPage = onlyOneHitByPage;
    }

    public boolean getCheckPageACL() {
        return checkPageACL;
    }

    public void setCheckPageACL(boolean checkPageACL) {
        this.checkPageACL = checkPageACL;
    }

    public boolean getCheckContentObjectACL() {
        return checkContentObjectACL;
    }

    public void setCheckContentObjectACL(boolean checkContentObjectACL) {
        this.checkContentObjectACL = checkContentObjectACL;
    }

    /**
     * @param parsedObjects
     * @param jParams
     * @return
     */
    public JahiaSearchResult buildResult(
            Collection<ParsedObject> parsedObjects, ProcessingContext jParams, String[] queryArray) {
        return buildResult(parsedObjects, jParams, JahiaBaseACL.READ_RIGHTS, queryArray);
    }

    /**
     * @param parsedObjects
     * @param jParams
     * @param permission
     * @return
     */
    public JahiaSearchResult buildResult(
            Collection<ParsedObject> parsedObjects, ProcessingContext jParams,
            int permission, String[] queryArray) {
        
        JahiaSearchResult result = new JahiaSearchResult(this, parsedObjects);

        if (parsedObjects == null || parsedObjects.isEmpty())
            return result;

        ServicesRegistry sReg = ServicesRegistry.getInstance();

        JahiaUser currentUser = jParams.getUser();

        Map<Integer, List<JahiaSearchHit>> pageHitsMap = new HashMap<Integer, List<JahiaSearchHit>>();
        Map<String, List<JahiaSearchHit>> fileHitsMap = new HashMap<String, List<JahiaSearchHit>>();

        int pageHitListMaxSize = 0;
        int fileHitListMaxSize = 0;

        for (ParsedObject parsedObject : parsedObjects) {
            JahiaSearchHit info = new JahiaSearchHit(parsedObject);
            boolean isFile = false;
            try {
                String objIDVal = parsedObject.getValue(JahiaSearchConstant.ID);
                String contentType = parsedObject
                        .getValue(JahiaSearchConstant.CONTENT_TYPE);
                int jahiaSearchHitType = JahiaSearchConstant
                        .getJahiaSearchHitTypeFromSearchConstantType(contentType);
                String objectKey = parsedObject
                        .getValue(JahiaSearchConstant.OBJECT_KEY);

                String fieldIdVal = parsedObject
                        .getValue(JahiaSearchConstant.FIELD_FIELDID);
                ContentField contentField = null;
                String jcrName = null;
                if (fieldIdVal != null && fieldIdVal.length() > 0) {
                    try {
                        contentField = ContentField.getField(Integer.parseInt(fieldIdVal));
                        isFile = (contentField != null && contentField instanceof ContentFileField);
                        if (!isFile && contentField instanceof ContentBigTextField) {
                            jcrName = parsedObject.getValue(JahiaSearchConstant.FILE_REALNAME);
                            isFile = jcrName != null && jcrName.length() > 0;
                        }
                    } catch (Exception t) {
                        logger.warn("Error obtaining field-id hit details", t);
                    }
                }

                if (objIDVal != null && objectKey != null) {
                    ContentObject contentObject = (ContentObject) ContentObject
                            .getInstanceAsObject(ObjectKey.getInstance(objectKey));
                    if (processObject(contentObject, jParams)) {
                        info.setObject(contentObject);
                        int pageId = 0;
                        if (contentObject instanceof ContentContainer) {
                            pageId = contentObject.getPageID();
                            info.setContainerType(parsedObject.getValue(JahiaSearchConstant.CONTAINER_DEFINITION_PRIMARYTYPE));
                        } else if (contentObject instanceof ContentPage) {
                            pageId = contentObject.getID();
                        }
                        JahiaPage aPage = null;
                        try {
                            if (pageId != 0) {
                                aPage = sReg.getJahiaPageService().lookupPage(
                                        pageId, jParams);
                                if (aPage.getPageType() != JahiaPage.TYPE_DIRECT) {
                                    // its an url, use the parent page to display the link
                                    aPage = sReg.getJahiaPageService()
                                            .lookupPage(aPage.getParentID(),
                                                    jParams);
                                }
                            }
                        } catch (Exception t) {
                            logger.debug("Page not found pageId=" + pageId);
                        }

                        // check right at page
                        if (this.checkPageACL
                                && (aPage == null || !aPage
                                        .checkReadAccess(currentUser))) {
                            continue;
                        }

                        // check right at content object hit
                        if (this.checkContentObjectACL) {
                            JahiaBaseACL acl = contentObject.getACL();
                            if (acl == null
                                    || !acl.getPermission(currentUser,
                                            JahiaBaseACL.READ_RIGHTS)) {
                                continue;
                            }
                        }


                        info.setType(isFile ? JahiaSearchHitInterface.FILE_TYPE : jahiaSearchHitType);
                        info.setIntegerID(contentObject.getID());
                        info.setPageId(aPage.getID());
                        info.setPage(aPage);
                        
                        String url = "#";
                        if (aPage != null) {
                            if (isFile) {
                                String mimeType = null;
                                if (jcrName == null) {
                                    JahiaField jahiaField = contentField.getJahiaField(jParams.getEntryLoadRequest());
                                    jcrName = jahiaField.getValue();
                                }
                                String providerKey = jcrName.substring(0,jcrName.indexOf(':'));
                                String uuid = jcrName.substring(jcrName.indexOf(':') + 1);
                                JCRNodeWrapper file = JCRStoreService.getInstance().getNodeByUUID(
                                        providerKey, uuid, currentUser);
                                info.setObject(file);

                                if (mimeType == null) {
                                    parsedObject.getLazyFieldValue(JahiaSearchConstant.FILE_CONTENT_TYPE);
                                } else {
                                    parsedObject.getFields().put(JahiaSearchConstant.FILE_CONTENT_TYPE,
                                            new String[] { mimeType });
                                }

                                parsedObject.getLazyFieldValue(JahiaSearchConstant.FILE_CREATOR);
                                parsedObject.getLazyFieldValue(JahiaSearchConstant.FILE_LAST_CONTRIBUTOR);
                                parsedObject.getLazyFieldValue(JahiaSearchConstant.FILE_LAST_MODIFICATION_DATE);
                            }
                            try {
                                if (SettingsBean.getInstance().isSiteIDInSearchHitPageURL()
                                        && aPage.getJahiaID() != jParams.getJahiaID()) {
                                    jParams.setForceAppendSiteKey(true);
                                }
                                url = aPage.getPageType() == JahiaPage.TYPE_URL ? aPage.getRemoteURL() : aPage
                                        .getURL(jParams);
                            } catch (Exception t) {
                                logger.warn("Error obtaining page search hit details", t);
                            } finally {
                                jParams.setForceAppendSiteKey(false);
                            }
                        }

                        info.setURL(url == null ? "#" : url);
                        String languageCode = null;
                        try {
                            languageCode = parsedObject
                                    .getValue(JahiaSearchConstant.LANGUAGE_CODE);
                            if (!info.getLanguageCodes().contains(languageCode)) {
                                info.getLanguageCodes().add(languageCode);
                            }
                        } catch (Exception t) {
                            logger.warn("Error obtaining language code for hit details", t);
                        }

                        // handle one hit by page
                        if (shouldAddHit(info, result, languageCode)){
                            result.addHit(info);
                        }

                        if (info != null) {
                            Integer pageIdKey = new Integer(aPage.getID());
                            info.setScore(parsedObject.getScore() * 100);
                            /*
                             * if ( !this.onlyOneHitByPage ){ info.setScore(parsedObject.getScore() 100); } else {
                             * info.setScore(parsedObject.getScore()); }
                             */
                            List<JahiaSearchHit> pageHitsList = pageHitsMap
                                    .get(pageIdKey);
                            if (pageHitsList == null) {
                                pageHitsList = new ArrayList<JahiaSearchHit>();
                                pageHitsMap.put(pageIdKey, pageHitsList);
                            }
                            pageHitsList.add(info);
                            if (pageHitsList.size() > pageHitListMaxSize) {
                                pageHitListMaxSize = pageHitsList.size();
                            }
                        }

                        if (info != null && isFile) {
                            if (!"#".equals(info.getURL())) {
                                List<JahiaSearchHit> fileHitsList = fileHitsMap
                                        .get(info.getURL());
                                if (fileHitsList == null) {
                                    fileHitsList = new ArrayList<JahiaSearchHit>();
                                    fileHitsMap
                                            .put(info.getURL(), fileHitsList);
                                }
                                fileHitsList.add(info);
                                if (fileHitsList.size() > fileHitListMaxSize) {
                                    fileHitListMaxSize = fileHitsList.size();
                                }
                            }
                        }
                    }
                }
            } catch (Exception t) {
                logger.warn(t);
            }
        }

        result.setPageHitsMap(pageHitsMap);
        result.setFileHitsMap(fileHitsMap);

        int fileRefOrPageCountMax = pageHitListMaxSize;

        if (fileHitListMaxSize > fileRefOrPageCountMax) {
            fileRefOrPageCountMax = fileHitListMaxSize;
        }

        // compute score average
        if (onlyOneHitByPage) {

            // for pages
            float hitPerPageMaxScore = 1;
            // for files
            float fileReferenceMaxScore = 1;

            Map<Integer, Float> pageHitsScoreSum = new HashMap<Integer, Float>();
            for (JahiaSearchHit searchHit : result.results()) {
                int score = 0;
                if (searchHit.getType() != JahiaSearchHit.FILE_TYPE) {
                    int nbHitPerPage = 1;
                    Integer pageIdKey = new Integer(searchHit.getPageId());
                    Float pageScore = pageHitsScoreSum.get(pageIdKey);
                    List<JahiaSearchHit> pageHitsList = pageHitsMap
                            .get(pageIdKey);
                    if (pageScore == null) {
                        if (pageHitsList != null) {
                            for (JahiaSearchHit info : pageHitsList) {
                                score += info.getParsedObject().getScore();
                            }
                            pageScore = new Float(score);
                            pageHitsScoreSum.put(pageIdKey, pageScore);
                        }
                    }
                    if (pageHitsList != null) {
                        nbHitPerPage = pageHitsList.size();
                    }
                    float hitPerPageScore = nbHitPerPage * hitPerPageMaxScore
                            / fileRefOrPageCountMax;
                    if (pageScore != null) {
                        searchHit
                                .setScore(((pageScore.floatValue() / nbHitPerPage) + hitPerPageScore * 2) / 3);
                    }
                    searchHit.setScore(Math.round(searchHit.getScore() * 100));
                } else {
                    int nbFileReference = 1;
                    List<JahiaSearchHit> fileHitsList = fileHitsMap
                            .get(searchHit.getURL());
                    if (fileHitsList != null) {
                        nbFileReference = fileHitsList.size();
                    }
                    if (nbFileReference > 1) {
                        float fileReferenceScore = nbFileReference
                                * fileReferenceMaxScore / fileRefOrPageCountMax;
                        searchHit
                                .setScore(Math
                                        .round((searchHit.getParsedObject()
                                                .getScore() + fileReferenceScore * 2) / 3 * 100));
                    } else {
                        searchHit.setScore(Math.round(searchHit
                                .getParsedObject().getScore() * 100));
                    }
                }
                if (searchHit.getScore() == 0) {
                    searchHit.setScore(1);
                }
            }
        }

        // sort the result
        if (result.getHitCount() > 1) {
            Collections.sort(result.results());
        }

        return JahiaSearchBaseService.executeURLModificationRules(result, jParams);
    }
    
    private boolean shouldAddHit (JahiaSearchHit info, JahiaSearchResult result, String languageCode) {
        boolean shouldAdd = true;
        if (this.onlyOneHitByPage) {
            boolean removePreviousHit = false;
            boolean duplicateFound = false;
            int hitIndex = 0;
            JahiaSearchHit searchHit = null;
            for (int j = 0; j < result.getHitCount(); j++) { // page already found
                searchHit = result.results().get(j);
                boolean biggerScore = searchHit.getScore() < info.getScore();
                hitIndex = j;
                if (info.getType() == JahiaSearchHitInterface.FILE_TYPE) {
                    if (searchHit.getType() == JahiaSearchHitInterface.FILE_TYPE
                            && !"#".equals(searchHit.getURL())
                            && searchHit.getObject() != null
                            && info.getObject() != null
                            && ((JCRNodeWrapper) searchHit.getObject()).getUrl().equals(
                                    ((JCRNodeWrapper) info.getObject()).getUrl())) {
                        // its a same linked file
                        duplicateFound = true;
                    } 
                } else if (info.getPageId() == searchHit.getPageId()) {
                    if (searchHit.getType() != JahiaSearchHitInterface.FILE_TYPE) {
                        duplicateFound = true;
                    } 
                }
                if (duplicateFound) {
                    if (biggerScore) {
                        removePreviousHit = true;
                    } else {
                        shouldAdd = false;
                    }
                }
                if (removePreviousHit || !shouldAdd) {
                    break;
                }
            }
            if (removePreviousHit) {
                // discard preview hit with lesser score
                if (languageCode != null) {
                    searchHit = result.results().get(hitIndex);
                    searchHit.getLanguageCodes().remove(
                            languageCode);
                }
                info.getLanguageCodes().addAll(
                        searchHit.getLanguageCodes());
                result.removeHit(hitIndex);
            }
            if (!shouldAdd) {
                try {
                    if (!searchHit.getLanguageCodes().contains(
                            languageCode)) {
                        searchHit.getLanguageCodes().add(
                                languageCode);
                    }
                } catch (Exception t) {
                    logger.warn("Error obtaining language code hit details", t);
                }
            }
        }
        return shouldAdd;        
    }
    
    public Map<Integer, List<JahiaSearchHit>> groupHitsByObject(int objectType,
            JahiaSearchResult jahiaSearchResult) {
        return this.groupingHitsHandler.groupHitsByObject(objectType,
                jahiaSearchResult);
    }

    protected boolean processObject(ContentObject contentObject,
            ProcessingContext context) throws JahiaException {
        if (!contentObject.hasActiveOrStagingEntries()) {
            return false;
        }
        ContentPage contentPage = null;
        if (contentObject instanceof PageReferenceableInterface) {
            try {
                contentPage = ((PageReferenceableInterface) contentObject)
                        .getPage();
            } catch (Exception t) {
                logger.debug("Error with ContentPage ", t);
            }
        } else if (contentObject instanceof ContentPage) {
            contentPage = (ContentPage) contentObject;
        }
        if (contentPage == null || !contentPage.hasActiveOrStagingEntries()) {
            return false;
        }
        JahiaPage jahiaPage = null;
        try {
            jahiaPage = contentPage.getPage(context);
        } catch (Exception t) {
            logger.info("Error obtaining content page", t);
        }
        if (jahiaPage == null) {
            return false;
        }
        boolean isNormalMode = ProcessingContext.NORMAL.equals(context
                .getOperationMode());
        String languageCode = context.getLocale().toString();
        /*
         * String pageTitle = jahiaPage.getTitle(); 
         * if ( pageTitle == null ){ 
         *     return false; 
         * } else if (!isNormalMode && contentPage.isMarkedForDelete(languageCode) ){ 
         *     return false; 
         * }
         */
        if (!isNormalMode && contentPage.isMarkedForDelete(languageCode)) {
            return false;
        }
        return true;
    }

}
