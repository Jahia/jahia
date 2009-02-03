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

 package org.jahia.services.webdav;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.LanguageCodeConverters;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 18, 2003
 * Time: 12:51:15 AM
 * To change this template use Options | File Templates.
 */
public class UsageEntry {
    private static final transient Logger logger = Logger.getLogger(UsageEntry.class);
    private int id;
    private int version;
    private int workflow;
    private String lang;
    private String val;
    private JahiaField aField;
    private ProcessingContext jParams;
    private EntryLoadRequest loadRequest;
    private ContentPage aPage;
    private String url;

    public UsageEntry (int id, int version, int workflow, String lang, String val, ProcessingContext jParams)
            throws JahiaException {
        this.id = id;
        this.version = version;
        this.workflow = workflow;
        this.lang = lang;
        this.val = val;
        this.jParams = jParams;

    }

    public JahiaField getField() {
        if (aField == null && jParams != null) {
            try {
                List<Locale> list = new ArrayList<Locale>();
                list.add(LanguageCodeConverters.languageCodeToLocale(lang));
                loadRequest = new EntryLoadRequest(workflow, version, list);
                ServicesRegistry sReg = ServicesRegistry.getInstance ();

                aField = sReg.getJahiaFieldService ().loadField (id, LoadFlags.NOTHING, jParams, loadRequest);
            } catch (JahiaException e) {
                logger.error("Cannot get file usage " +val,e);
            }
        }
        return aField;
    }

    public int getId () {
        return id;
    }

    public int getVersion () {
        return version;
    }

    public int getWorkflow () {
        return workflow;
    }

    public String getExtendedWorkflowState() {
        try {
            return WorkflowService.getInstance().getExtendedWorkflowState(getPage(), lang) ;
        } catch (Exception e) {
            logger.error(e.getMessage(), e) ;
            return "000" ;
        }
    }

    public String getLang () {
        return lang;
    }

    public String getVal () {
        return val;
    }


    public ContentPage getPage() {
        if (aPage == null && getField() != null) {
            try {
                aPage = ContentPage.getPage(aField.getPageID ());
            } catch (JahiaException e) {
                logger.error("Error getting page",e);
            }
        }
        return aPage;
    }

    public String getUrl () {
        if (url == null && getPage() != null) {
            try {
                String opMode = jParams.getOperationMode ();
                if (workflow == EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                    jParams.setOperationMode (ProcessingContext.EDIT);
                } else if (workflow == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    jParams.setOperationMode (ProcessingContext.NORMAL);
                } else if (workflow == EntryLoadRequest.WAITING_WORKFLOW_STATE) {
                    jParams.setOperationMode (ProcessingContext.PREVIEW);
                }
                url = getPage().getURL (jParams);
                jParams.setOperationMode (opMode);
            } catch (JahiaException e) {
                logger.error("Error getting url",e);
            }
        }
        return url;
    }

    public String getPageTitle () {
        if (aField == null) {
            getField();
        }
        if (getPage() != null) {
            return getPage().getTitle (loadRequest);
        }
        return null;
    }

    public String toString() {
        return "UsageEntry for ContentField_"+ id;
    }

}
