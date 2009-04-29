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
 package org.jahia.data.viewhelper.sitemap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.content.PageReferenceableInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowService;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 16 nov. 2004
 * Time: 18:35:55
 * <p/>
 * $Author$
 * $Date$
 * $Id$
 * $RCSfile: WorkflowSiteMapViewHelper.java,v $
 * $Revision$
 * $Source: /home/cvs/repository/jahia/core/src/java/org/jahia/data/viewhelper/sitemap/WorkflowSiteMapViewHelper.java,v $
 * $State: Exp $
 */
public class WorkflowSiteMapViewHelper extends TreeSiteMapViewHelper {
    private static final transient Logger logger = Logger.getLogger(WorkflowSiteMapViewHelper.class);

    private static final WorkflowService workflowService = ServicesRegistry.getInstance().
            getWorkflowService();

    public WorkflowSiteMapViewHelper(JahiaUser user,
                                     ContentPage startPage,
                                     int pageInfosFlag,
                                     String languageCode,
                                     int defaultMaxLevel,
                                     PagesFilter pagesFilter) {
        super(user, startPage, pageInfosFlag, languageCode, defaultMaxLevel, true, pagesFilter, true);
    }

    protected List getPageChilds(ContentObject object) {
        try {
            int thisPageId = -1;
            if (object instanceof ContentPage) {
                thisPageId = object.getID();
            } else if (object instanceof PageReferenceableInterface) {
                thisPageId = (object).getPageID();
            }

            final List linked = workflowService.getLinkedContentObjects(object, false);
            final List linkedPages = new ArrayList();
            for (final Iterator iterator = linked.iterator(); iterator.hasNext();) {
                final ContentObject contentObject = (ContentObject) iterator.next();
                if (contentObject instanceof ContentPage && ((ContentPage)contentObject).getPageType(EntryLoadRequest.STAGED)== JahiaPage.TYPE_DIRECT) {
                    linkedPages.add(contentObject);
                }
            }

            final List fieldsHere = new ArrayList();
            final List unlinked = workflowService.getUnlinkedContentObjects(object);
            unlinked.addAll(linkedPages);
            final Iterator iterator = unlinked.iterator(); 
            while (iterator.hasNext() ) {
                final ContentObject contentObject = (ContentObject) iterator.next();
                int pageId = -1;
                if (contentObject instanceof ContentPage) {
                    pageId = ((ContentPage)contentObject).getParentID((EntryLoadRequest) null);
                } else if (contentObject instanceof PageReferenceableInterface) {
                    pageId = (contentObject).getPageID();
                }
                if (thisPageId == pageId) {
                    fieldsHere.add(contentObject);
                }
            }

            final List v = new ArrayList();
            final Iterator iterator2 = fieldsHere.iterator();
            while (iterator2.hasNext()) {
                final ContentObject contentObject = (ContentObject) iterator2.next();
                if (contentObject.checkWriteAccess(getUser()) && contentObject.hasActiveEntries()) {
                    if (!(contentObject instanceof ContentPage)) {
                        ContentObject main = workflowService.getMainLinkObject(contentObject);
                        v.add(main);
                    } else {
                        v.add(contentObject);
                    }
                }
            }

            logger.debug(v);

            return v;
        } catch (JahiaException e) {
            logger.debug("Unable to find '" + object.getID() + "' child pages");
            return new ArrayList();
        }
    }
}
