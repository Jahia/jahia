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
package org.jahia.ajax.gwt.linkchecker.server;

import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.jahia.admin.AdministrationModule;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaCheckedLink;
import org.jahia.ajax.gwt.client.data.linkchecker.GWTJahiaLinkCheckerStatus;
import org.jahia.ajax.gwt.client.service.linkchecker.LinkCheckerService;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentPageKey;
import org.jahia.data.JahiaData;
import org.jahia.data.fields.FieldTypes;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.fields.ContentField;
import org.jahia.services.integrity.Link;
import org.jahia.services.integrity.LinkValidationResult;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;

/**
 * User: romain
 * Date: 11 juin 2009
 * Time: 15:09:20
 */
public class LinkCheckerServiceImpl extends JahiaRemoteService implements LinkCheckerService {
    
    private final static Logger logger = Logger.getLogger(LinkCheckerServiceImpl.class);
    
    private LinkChecker linkChecker;
    
    private static GWTJahiaCheckedLink getGWTViewObject(Link link,
            LinkValidationResult validationResult, JahiaData jData) {

        GWTJahiaCheckedLink viewObject = new GWTJahiaCheckedLink(link.getUrl(),
                link.getSource().getObjectKey().toString(), "#", link
                        .getSource().getWorkflowState(), link.getSource()
                        .getLanguageCode(), validationResult.getErrorCode(),
                validationResult.getErrorMessage());

        viewObject.setCodeText(HttpStatus.getStatusText(validationResult
                .getErrorCode()));
        
        ProcessingContext ctx = jData.getProcessingContext();
        
        ContentObject contentObj;
        try {
            contentObj = (ContentObject) ContentObject.getInstance(link.getSource().getObjectKey());
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
        
        if (contentObj != null) {
            viewObject.setPageId(contentObj.getPageID());
            ContentPage contentPage = null;
            try {
                contentPage = ContentPage
                        .getPage(contentObj.getPageID(), false);
            } catch (JahiaException ex) {
                logger.warn("Unable to retrieve page with ID: "
                        + contentObj.getPageID(), ex);
            }

            if (contentPage != null) {
                String pageTitle = contentPage.getTitle(ctx
                        .getEntryLoadRequest(), false);
                if (pageTitle != null) {
                    viewObject.setPageTitle(pageTitle);
                }
                try {
                    String pageUrl = null;
                    if (contentObj instanceof ContentPage) {
                        int parentPageId = ((ContentPage) contentObj)
                                .getParentID(ctx);
                        ContentPage parent = ContentPage.getPage(parentPageId,
                                false);
                        if (parent != null) {
                            pageUrl = parent.getUrl(ctx);
                            viewObject.setPageId(parent.getPageID());
                        }
                    } else {
                        pageUrl = contentPage.getUrl(ctx);
                    }
                    viewObject.setPageUrl(pageUrl);
                } catch (JahiaException e) {
                    logger.warn(e.getMessage(), e);
                }
            }

            String updateUrl = null;
            try {
                ContentField fld = null;
                if (ContentFieldKey.FIELD_TYPE.equals(contentObj.getObjectKey()
                        .getType())) {
                    fld = (ContentField) contentObj;
                } else if (ContentPageKey.PAGE_TYPE.equals(contentObj
                        .getObjectKey().getType())) {

                    fld = (ContentField) contentObj.getParent(ctx
                            .getEntryLoadRequest());
                }
//                if (fld != null) {
//                    ContentContainer cnt = null;
//                    if (fld.getContainerID() != 0) {
//                        try {
//                            cnt = ContentContainer.getContainer(fld
//                                    .getContainerID());
//                        } catch (Exception ex) {
//                            // ignore
//                        }
//                    }
//                    updateUrl = (cnt != null ? jData.gui()
//                            .drawUpdateContainerUrl(cnt, fld.getID()) : jData
//                            .gui().drawUpdateFieldUrl(fld))
//                            + "&engine_lang="
//                            + link.getSource().getLanguageCode();
//                    viewObject.setFieldId(fld.getID());
//                    viewObject.setFieldType(FieldTypes.typeName[fld.getType()]);
//                }
            } catch (JahiaException e) {
                logger.error("Unable to compose update URL for the object: "
                        + link.getSource().getObjectKey() + ". Cause: "
                        + e.getMessage(), e);
            }
            if (updateUrl != null) {
                viewObject.setUpdateUrl(updateUrl);
            }
        }
        
        return viewObject;
    }
    
    private JahiaACLManagerService aclManagerService;
    
    private AdministrationModule linkCheckerAdministrationModule;
    
    public Boolean checkLinks() {
        ProcessingContext ctx = retrieveParamBean();
        if (ctx.getUser().isAdminMember(ctx.getSiteID()) || hasAccess(ctx.getUser(), ctx.getSiteID())) {
            linkChecker.startCheckingLinks(ctx.getSiteID());
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    private boolean hasAccess(JahiaUser user, int siteID) {
        return aclManagerService.getSiteActionPermission(
                linkCheckerAdministrationModule.getPermissionName(),
                user,
                JahiaBaseACL.READ_RIGHTS,
                siteID) > 0;
    }

    /**
     * Retrieves the status and list of links to be displayed in the report.
     * 
     * @return the status and list of links to be displayed in the report
     */
    public GWTJahiaLinkCheckerStatus lookForCheckedLinks() {
        GWTJahiaLinkCheckerStatus status = new GWTJahiaLinkCheckerStatus();
        status.setProperties(linkChecker.getStatus().getProperties());

        List<Object[]> invalidLinks = linkChecker.getLinks();
        if (!invalidLinks.isEmpty()) {
            ProcessingContext ctx = retrieveParamBean();
            String opMode = ctx.getOperationMode();
            if (!ProcessingContext.EDIT.equals(opMode)) {
                try {
                    ctx.setOperationMode(ProcessingContext.EDIT);
                } catch (JahiaException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            try {
                JahiaData jData = retrieveJahiaData(new GWTJahiaPageContext(ctx
                        .getPageID(), ctx.getOperationMode()));
                for (Object[] processedLink : invalidLinks) {
                    status.getLinks().add(
                            getGWTViewObject((Link) processedLink[0],
                                    (LinkValidationResult) processedLink[1],
                                    jData));
                }
            } finally {
                if (!ProcessingContext.EDIT.equals(opMode)) {
                    try {
                        ctx.setOperationMode(opMode);
                    } catch (JahiaException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        return status;
    }
    
    public void setAclManagerService(JahiaACLManagerService aclManagerService) {
        this.aclManagerService = aclManagerService;
    }

    public void setLinkCheckerAdministrationModule(
            AdministrationModule linkCheckerAdministrationModule) {
        this.linkCheckerAdministrationModule = linkCheckerAdministrationModule;
    }

    public void stopCheckingLinks() {
        linkChecker.stopCheckingLinks();
    }

    public void setLinkChecker(LinkChecker linkChecker) {
        this.linkChecker = linkChecker;
    }
    
}
