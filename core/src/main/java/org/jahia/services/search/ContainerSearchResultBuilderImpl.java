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

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.data.search.JahiaContainerSearchHit;
import org.jahia.data.search.JahiaSearchHit;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.search.lucene.JahiaHitCollector;
import org.jahia.utils.OrderedBitSet;

import java.util.*;

/**
 * Default implementation to build JahiaSearchResult from a collection of ParsedObject
 * 
 * @author NK
 */
public class ContainerSearchResultBuilderImpl extends
        JahiaAbstractSearchResultBuilder {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(ContainerSearchResultBuilderImpl.class);

    private boolean oneHitByContainer = false;

    private GroupingHitsHandler groupingHitsHandler = new GroupingHitsHandler();

    private boolean checkACL = true;

    private boolean checkParentPageIntegrity = true;

    public ContainerSearchResultBuilderImpl() {
        this.setHitCollector(new JahiaHitCollector(false, -1, -1, -1, -1));
        this.getHitCollector().setSearchResultBuilder(this);
    }

    public boolean getOneHitByContainer() {
        return oneHitByContainer;
    }

    public void setOneHitByContainer(boolean oneHitByContainer) {
        this.oneHitByContainer = oneHitByContainer;
    }

    /**
     * 
     * @param parsedObjects
     * @param jParams
     * @return
     */
    public JahiaSearchResult buildResult(
            Collection<ParsedObject> parsedObjects, ProcessingContext jParams, String[] queriesAr) {

        return buildContainerSearchResult(parsedObjects, jParams);
    }

    public Map<Integer, List<JahiaSearchHit>> groupHitsByObject(int objectType,
            JahiaSearchResult jahiaSearchResult) {
        return this.groupingHitsHandler.groupHitsByObject(objectType,
                jahiaSearchResult);
    }

    // --------------------------------------------------------------------------
    /**
     * build Search result from a collection of ParsedObject instance A JahiaSearchHit is mapped to a Container
     * 
     * @param parsedObjects
     *            Collection
     * @param jParams
     *            ProcessingContext
     * @return JahiaSearchResult
     */
    private JahiaSearchResult buildContainerSearchResult(
            Collection<ParsedObject> parsedObjects, ProcessingContext jParams) {

        JahiaSearchResult result = new JahiaSearchResult(this, parsedObjects);
        List<Integer> orderedBits = null;
        if (this.getSorter() != null
                && (result.bits() instanceof OrderedBitSet)) {
            ((OrderedBitSet) result.bits()).setOrdered(true);
            orderedBits = ((OrderedBitSet) result.bits()).getOrderedBits();
        }

        if (parsedObjects == null || parsedObjects.isEmpty())
            return result;

        for (ParsedObject parsedObject : parsedObjects) {
            ContentObjectKey objectKey = null;
            String pageID;
            int jahiaSearchHitType = 0;
            try {
                String ctnKeyVal = parsedObject
                        .getValue(JahiaSearchConstant.OBJECT_KEY);
                String contentType = parsedObject
                        .getValue(JahiaSearchConstant.CONTENT_TYPE);
                String parentID = parsedObject
                        .getValue(JahiaSearchConstant.PARENT_ID);
                pageID = parsedObject.getValue(JahiaSearchConstant.PAGE_ID);
                jahiaSearchHitType = JahiaSearchConstant
                        .getJahiaSearchHitTypeFromSearchConstantType(contentType);
                if (ctnKeyVal != null) {
                    objectKey = (ContentObjectKey) ContentObjectKey
                            .getInstance(ctnKeyVal);
                    if (objectKey instanceof ContentFieldKey) {
                        objectKey = new ContentContainerKey(Integer
                                .parseInt(parentID));
                    } else if (objectKey instanceof ContentPageKey) {
                        pageID = objectKey.getIDInType();
                        while (objectKey != null
                                && !(objectKey instanceof ContentContainerKey)) {
                            objectKey = objectKey.getParent(jParams
                                    .getEntryLoadRequest());
                        }
                    }
                    if (this.checkParentPageIntegrity) {
                        ContentContainer contentContainer = (ContentContainer) ContentObject
                                .getInstance(objectKey);
                        if (!processContainer(contentContainer, jParams)) {
                            continue;
                        }
                    }
                }
            } catch (Exception t) {
                logger.debug("Exception occured with container search hit", t);
                continue;
            }

            if (objectKey != null) {
                boolean found = false;
                if (oneHitByContainer) {
                    for (JahiaSearchHit searchHit : result.results()) { // page already found
                        if (searchHit.hashCode() == objectKey.getIdInType()) {
                            searchHit.setScore(searchHit.getScore() + 1);
                            found = true;
                            break;
                        }
                    }
                }

                int objID = objectKey.getIdInType();

                if (!found || (oneHitByContainer && !found)) {
                    JahiaContainerSearchHit info = new JahiaContainerSearchHit(
                            parsedObject);
                    if (oneHitByContainer) {
                        info.setScore(1);
                    } else {
                        info.setScore((new Float(
                                        parsedObject.getScore() * 100))
                                        .intValue());
                    }

                    info.setIntegerID(objID);
                    info.setType(jahiaSearchHitType);
                    info.setPageId(Integer.parseInt(pageID)); // FIXME : should we set the page id of the container of of the field ? What
                                                              // with datasourcing ?

                    String value = "@todo lucene hit highlighter";
                    if (value != null) {
                        try {
                            value = (new RE("<(.*?)>")).subst(value, "");
                        } catch (RESyntaxException e) {
                            logger.error("Error while removing tags", e);
                        } catch (Exception t) {
                            logger.error(t);
                        }
                        info.setTeaser(GuiBean.glueTitle(value,
                                JahiaSearchConstant.TEASER_LENGTH));

                    } else {
                        info.setTeaser("");
                    }
                    result.addHit(info);
                    if (orderedBits != null) {
                        orderedBits.add(new Integer(objID));
                    }
                }
            }
        }

        // sort the result
        if (this.getSorter() == null) {
            if (result.results().size() > 1) {
                Collections.sort(result.results());
            }
        }

        return result;
    }

    private boolean processContainer(ContentContainer contentContainer,
            ProcessingContext context) throws JahiaException {
        if (contentContainer.getActiveAndStagingEntryStates().size() == 0) {
            return false;
        }
        ContentPage contentPage = null;
        try {
            contentPage = contentContainer.getPage();
        } catch (Exception t) {
            logger.debug("Error with ContentPage ", t);
        }
        if (contentPage == null || !contentPage.hasActiveOrStagingEntries()) {
            return false;
        }
        /*
         * FIXME : If we do that check and that the user has no right access to the page, the container is not returned even that we have
         * access to it. JahiaPage jahiaPage = null; try { jahiaPage = contentPage.getPage(context); } catch ( Exception t){ } if (
         * jahiaPage == null ) { return false; }
         */
        String languageCode = context.getLocale().toString();
        boolean isNormalMode = ProcessingContext.NORMAL.equals(context
                .getOperationMode());
        if (!isNormalMode && contentPage.isMarkedForDelete(languageCode)) {
            return false;
        }
        /*
         * String languageCode = context.getLocale().toString(); String pageTitle = jahiaPage.getTitle(); if ( pageTitle == null ){ return
         * false; } else if ( !isNormalMode && contentPage.isMarkedForDelete(languageCode) ){ return false; }
         */
        return true;
    }

    public void setCheckACL(boolean checkACL) {
        this.checkACL = checkACL;
    }

    public boolean getCheckACL() {
        return this.checkACL;
    }

    public boolean isCheckParentPageIntegrity() {
        return checkParentPageIntegrity;
    }

    /**
     * Check or not if the parent page of the container exists
     * 
     * @param checkParentPageIntegrity
     */
    public void setCheckParentPageIntegrity(boolean checkParentPageIntegrity) {
        this.checkParentPageIntegrity = checkParentPageIntegrity;
    }

}
