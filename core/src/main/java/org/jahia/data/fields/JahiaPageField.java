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

//  JahiaPageField
//  YG      17.07.2001

package org.jahia.data.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.data.FormDataManager;
import org.jahia.data.events.JahiaEvent;
import org.jahia.engines.login.Login_Engine;
import org.jahia.engines.logout.Logout_Engine;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.search.indexingscheduler.IndexationRuleInterface;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.TextHtml;

public class JahiaPageField extends JahiaField {

    private static final long serialVersionUID = -8931526637297593990L;
    
    final private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.
            getLogger(JahiaPageField.class);

    /**
     * constructor
     * YG    17.07.2001
     */
    public JahiaPageField(Integer ID,
                          Integer jahiaID,
                          Integer pageID,
                          Integer ctnid,
                          Integer fieldDefID,
                          Integer fieldType,
                          Integer connectType,
                          String fieldValue,
                          Integer rank,
                          Integer aclID,
                          Integer versionID,
                          Integer versionStatus,
                          String languageCode) {
        super(ID, jahiaID, pageID, ctnid, fieldDefID, fieldType, connectType,
                fieldValue, rank, aclID, versionID, versionStatus, languageCode);

        if (isShared()) {
            this.languageCode = ContentField.SHARED_LANGUAGE;
        }
    } // end constructor


    public void load(int loadFlag, ProcessingContext jParams, EntryLoadRequest loadRequest)
            throws JahiaException {


        if ((loadFlag) != 0) {

            ContentPageField contentPageField = (ContentPageField) ContentField.getField(getID());
            String val = contentPageField.getValue(jParams, loadRequest);
            this.setValue(FormDataManager.htmlEncode(val));
            this.setRawValue(val);

            int pageID = 0;
            if (!this.getValue().equals("<NoPage>")) {
                try {
                    pageID = Integer.parseInt(this.getValue());
                } catch (NumberFormatException nfe) {
                    pageID = -1;
                }
                if (pageID != -1) {
                    if ( ProcessingContext.COMPARE.equals(jParams.getOpMode()) ){
                        // check for moved page
                        /* We deactivate this check in order to have the page returned in compare mode,
                         * so that we can highlight the moved page in Compare mode

                        ContentPage contentPage = ContentPage.getPage(pageID);
                        ContentPageField cPageField = (ContentPageField)contentPage.getParent(loadRequest);
                        if ( (contentPage.hasSameParentID() != ContentPage.SAME_PARENT
                            && this.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE )
                                || (this.getID() != cPageField.getID()) ){
                            // not the parent field
                            this.setObject(null);
                            return;
                        }*/
                    }
                    JahiaPage thePage = null;
                    try {
                        /*
                       thePage = ServicesRegistry.getInstance().
                                 getJahiaPageService().lookupPage (pageID, jParams);
                        */
                        thePage = contentPageField.getPage(jParams, loadRequest);
                    } catch (JahiaException je) {
                        // an unexpected error occured with requested page
                        String msg = "An exception occured with requested pageid="
                                + pageID + "\n" + je.getMessage();
                        logger.debug(msg, je);
                        thePage = null;
                    }
                    if (thePage != null && thePage.getPageType() != -1) {
                        this.setObject(thePage);
                    }
                    if (thePage != null) {

                        switch (thePage.getPageType()) {
                            case JahiaPage.TYPE_DIRECT :

                                // MJ 01.04.2001
                                if (jParams == null) {
                                    //this.setObject( null );
                                    //if (this.getValue().toUpperCase().indexOf("JAHIA_LINKONLY") == -1)
                                    //{
                                    //   this.setValue( "");
                                    //}
                                    break;
                                }

                                if (thePage.checkReadAccess(jParams.getUser())) {
                                    // check if the page definition of this page is visible or not
                                    // if not deactivate the page !

                                    JahiaPageDefinition def = thePage.getPageTemplate();

                                    if (def != null) {
                                        //if ((def != null) && (def.isVisible())) {
                                        this.setValue(thePage.getURL(jParams));
                                    } else {
                                        // FIXME : NK
                                        // If a direct page has its template deleted,
                                        // it should not mean we have to reset the page ????????
                                        /*
                                        this.setObject( null );
                                        if (this.getValue().toUpperCase().indexOf("JAHIA_LINKONLY") == -1)
                                        {
                                            this.setValue( "");
                                        }*/

                                        // set the url to the page , but we sure have a page template not found when accessing it
                                        this.setValue(thePage.getURL(jParams));

                                    }
                                    if ( ProcessingContext.COMPARE.equals(jParams.getOpMode()) ){
                                        // Check for Moved page
                                        ContentPage contentPage = ContentPage.getPage(pageID);
                                        ContentPageField cPageField = (ContentPageField)contentPage.getParent(jParams.getEntryLoadRequest());
                                        if ( (contentPage.hasSameParentID() != ContentPage.SAME_PARENT
                                            && this.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE )
                                                || (cPageField != null && (this.getID() != cPageField.getID())) ){
                                            // not the parent field
                                            // flag the page as moved
                                            thePage.setMoved(true);
                                        }
                                    }                                        
                                } else {
                                    this.setObject(null);
                                    if (this.getValue().toUpperCase().indexOf("JAHIA_LINKONLY") == -1) {
                                        this.setValue("");
                                    }
                                }

                                break;

                            case JahiaPage.TYPE_LINK :
                                JahiaPage tempPage = null;
                                try {
                                    tempPage =
                                            ServicesRegistry.getInstance().
                                                    getJahiaPageService().lookupPage(
                                                    thePage.
                                                            getPageLinkID(), jParams);
                                } catch (JahiaPageNotFoundException jpnfe) {
                                    logger.debug("Page linked (pageID=" + thePage.getPageLinkID() + ") not found in page field " + getID() + ", id_jahia_pages_data=" + thePage.getID(), jpnfe);
                                    this.setObject(null);
                                    this.setValue("Page linked (pageID=" + thePage.getPageLinkID() + ") not found in page field " + getID());
                                }

                                if ((tempPage != null) && (jParams != null) && (jParams.getUser() != null)) {
                                    // no it's not, let's return null.
                                    if (!tempPage.checkReadAccess(jParams.getUser())) {
                                        this.setObject(null);
                                        if (this.getValue().toUpperCase().indexOf("JAHIA_LINKONLY") == -1) {
                                            this.setValue("");
                                        }
                                    }
                                } else {
                                    if (jParams != null) {
                                        if (jParams.getEngine().equals(ProcessingContext.CORE_ENGINE_NAME) ||
                                                jParams.getEngine().equals(Login_Engine.ENGINE_NAME) ||
                                                jParams.getEngine().equals(Logout_Engine.ENGINE_NAME)) {
                                            this.setObject(null);
                                            if (this.getValue().toUpperCase().
                                                    indexOf("JAHIA_LINKONLY") == -1) {
                                                this.setValue("");
                                            }
                                        }
                                    }
                                }

                                break;
                        }

                    } else {
                        if (this.getValue().toUpperCase().indexOf("JAHIA_LINKONLY") == -1) {
                            this.setValue("");
                        }
                    }
                } else {
                    if (this.getValue().toUpperCase().indexOf("JAHIA_LINKONLY") == -1) {
                        this.setValue("");
                    }
                }
            } else {
                if (this.getValue().toUpperCase().indexOf("JAHIA_LINKONLY") == -1) {
                    this.setValue("");
                }
            }
        }
        //System.out.println("######## end loadField fieldValue: "+this.getValue()+"#########");

    }

    public boolean save(ProcessingContext jParams)
            throws JahiaException {

        try {
            WorkflowService.getInstance().flushCacheForPageCreatedOrDeleted(new ContentPageKey(Integer.parseInt(getValue())));
        } catch (NumberFormatException e) {
        }

        ContentPageField contentField = (ContentPageField) ContentField.getField(this.getID());
        boolean isNew = false;
        if (contentField == null) {
            contentField = (ContentPageField) ContentFieldTools.getInstance().createContentFieldInstance(0,getJahiaID(), getPageID(), getctnid(),
                    getFieldDefID(), getType(), getConnectType(), getAclID(), new ArrayList<ContentObjectEntryState>(), new HashMap<ContentObjectEntryState, String>());
            contentField.setMetadataOwnerObjectKey(getMetadataOwnerObjectKey());
            isNew = true;
        }
        final String value = getValue();
        final String savedValue = contentField.getValue(jParams);

        if (((value == null && savedValue == null && !isNew) || (value != null && getValue().equals(savedValue)))) {
            return true;
        }

        jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
        logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");

        try {
            WorkflowService.getInstance().flushCacheForPageCreatedOrDeleted(new ContentPageKey(Integer.parseInt(contentField.getValue(jParams))));
        } catch (NumberFormatException e) {
        }

        String tmpVal = this.getValue();
        if (!tmpVal.equals("<empty>")) {
            int pageID = -1;
            try {
                pageID = Integer.parseInt(tmpVal);
            } catch (NumberFormatException nfe) {
                pageID = -1;
            }
            if (pageID != -1) {
                // checks to see if the page already exists
                JahiaPage thePage = (JahiaPage) this.getObject();
                if (thePage != null) {

                    thePage.commitChanges(true, jParams.getUser());
                    ContentPage contentPage = ContentPage.getPage(thePage.getID());
                    JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, contentPage);
                    ServicesRegistry.getInstance().getJahiaEventService()
                            .fireContentObjectUpdated(objectCreatedEvent);
                    ContentObjectKey.flushCache( contentPage );                    
                    if (contentField.getID() != 0) {
                        WorkflowService.getInstance().pageMoved((ContentObjectKey) contentField.getObjectKey(), (ContentPageKey) contentPage.getObjectKey(), jParams);
                    } else {
                        WorkflowService.getInstance().pageMoved(new ContentContainerKey(getctnid()), (ContentPageKey) contentPage.getObjectKey(), jParams);                        
                    }
                    //ServicesRegistry.getInstance().
                    //        getJahiaPageService().savePageInfo( thePage );

                    this.setValue(Integer.toString(thePage.getID()));
                } else {
                    this.setValue("");
                }
            }
        }
        int pageIDToSet;
        try {
            pageIDToSet = Integer.parseInt(this.getValue());
        } catch (NumberFormatException nfe) {
            pageIDToSet = -1;
        }
        contentField.setPageID(pageIDToSet, jParams.getUser(), isNew);

        if (getID() == 0) {
            setID(contentField.getID());
        }
        /* handled by contentPageField.setPageID() call
        // index page
        if ( pageUpdated ){
            ServicesRegistry.getInstance().getJahiaSearchService()
                .indexPage(pageIDToSet, jParams.getUser());
        }*/

        //ServicesRegistry.getInstance().getJahiaSearchService().indexContainer(this.getctnid(), jParams.getUser());

        return true;
    }

    public String getEngineName() {
        return "org.jahia.engines.shared.Page_Field";
    }

    public String getFieldContent4Ranking() {
        String fieldInfo = "";
        JahiaPage page = (JahiaPage) this.getObject();
        if (page != null) {
            fieldInfo = page.getTitle();
        } else {
            fieldInfo = this.getValue();
        }
        return fieldInfo;
    }

    public String getIconNameOff() {
        return "page";
    }

    public String getIconNameOn() {
        return "page_on";
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
     */
    public boolean isShared() {
        return true;
    }

    /**
     * Copy the internal value of current language to another language.
     * Must be implemented by conctrete field for specific implementation.
     *
     * @param aField A same field in another language
     *               //@todo : complete specific
     */
    public void copyValueInAnotherLanguage(JahiaField aField, ProcessingContext jParams)
            throws JahiaException {
        if (aField == null) {
            return;
        }
        aField.setValue(this.getValue());
        aField.setRawValue(this.getRawValue());
        aField.setObject(this.getObject());
    }

    /**
     * Returns an array of values for the given language Code.
     * By Default, return the field values in the field current language code.
     *
     * @param languageCode
     * @return
     * @throws JahiaException
     */
    public String[] getValuesForSearch(String languageCode, ProcessingContext context, boolean expand) throws JahiaException {

        String[] values = this.getValues();
        if (values == null || values.length == 0) {
            values = EMPTY_STRING_ARRAY;
        }
        for (int i = 0; i < values.length; i++) {
            values[i] = TextHtml.html2text(values[i]);
        }
        JahiaPage page = (JahiaPage)this.getObject();
        if ( page != null ){
            ContentPage contentPage = page.getContentPage();
            RuleEvaluationContext ctx = new RuleEvaluationContext(contentPage.getObjectKey(),contentPage,
                    context,context.getUser());
            IndexationRuleInterface rule = null;
            try {
                rule = ServicesRegistry.getInstance()
                    .getJahiaSearchIndexationService()
                    .evaluateContentIndexationRules(ctx);
            } catch ( Exception t ){
                logger.debug("Exception evaluation page field indexation rule",t);
            }
            if ( rule != null && rule.getIndexationMode() == IndexationRuleInterface.DONT_INDEX ){
                return EMPTY_STRING_ARRAY;
            }
            String title = page.getTitle();
            if ( title != null && !"".equals(title.trim()) ){
                List<String> valuesList = new ArrayList<String>();
                valuesList.addAll(Arrays.asList(values));
                valuesList.add(title);
                values = valuesList.toArray(values);
            }

            String[] urlKeys = new String[1];
            try {
                String urlKey = page.getContentPage().getProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
                if ( urlKey != null ){
                    urlKey = urlKey.trim();
                } else {
                    urlKey = "";
                }
                urlKeys[0] = urlKey;
            } catch ( Exception t ){
                logger.debug(t);
            }

            List<String> valuesList = new ArrayList<String>();
            valuesList.addAll(Arrays.asList(values));
            valuesList.addAll(Arrays.asList(urlKeys));
            values = valuesList.toArray(values);
        }
        return values;
    }

}
