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

package org.jahia.engines.validation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.JahiaBigTextField;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.engines.JahiaEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaGroupManagerDBProvider;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Utility class for performing link integrity checking in the big text fields.
 * 
 * @author Sergiy Shyrkov
 */
public final class LinkIntegrityChecker {

    private static Logger logger = Logger.getLogger(LinkIntegrityChecker.class);

    /**
     * Returns the ContentObject of the given field or its closest parent.
     */
    private static JahiaBaseACL getFieldACLObject(JahiaField field,
            Map engineMap) throws JahiaException {
        if (field.getID() <= 0) { // this means the field has not yet been saved
            // fetch the parent container ACL entry instead...
            final JahiaContainer container = (JahiaContainer) engineMap
                    .get("theContainer");
            if (logger.isDebugEnabled())
                logger
                        .debug("Trying to use parent Container ACL instead: ID = "
                                + container.getID());

            if (container.getID() <= 0) { // this means the container has not
                                          // yet been saved
                // fetch the parent container list ACL entry instead...
                final int ctnListID = container.getListID();
                if (logger.isDebugEnabled())
                    logger
                            .debug("Trying to use parent ContainerList ACL instead: ID = "
                                    + ctnListID);

                if (ctnListID <= 0) { // this means the containerList is empty
                    // fetch the page ACL entry instead
                    final ContentPage fieldPage = ContentPage.getPage(field
                            .getPageID());
                    if (logger.isDebugEnabled())
                        logger
                                .debug("Trying to use parent ContentPage ACL instead: ID = "
                                        + field.getPageID());

                    if (fieldPage == null) {
                        logger.error("Field ContentPage is null !!!");
                        throw new JahiaException(
                                "Field ContentPage is null !!!",
                                "Field ContentPage is null !!!",
                                JahiaException.APPLICATION_ERROR,
                                JahiaException.ERROR_SEVERITY);
                    }

                    return fieldPage.getACL();

                } else {
                    final JahiaContainerList list = ServicesRegistry
                            .getInstance().getJahiaContainersService()
                            .loadContainerListInfo(ctnListID);
                    return list.getACL();
                }

            } else {
                return container.getACL();
            }

        } else {
            return field.getACL();
        }
    }

    /**
     * Returns a Collection of JahiaGroupIDs not having Read Access on the given
     * target page, but that do have access on the current BigText field.
     * 
     * @param pageID
     *            The target page ID
     * @param field
     *            The current BigText field
     * @param jParams
     *            ProcessingContext instance of this request
     * @param engineMap
     *            Map instance for this engine
     * @return A Collection of JahiaGroupIDs (String objects)
     * @throws JahiaException
     *             If Something goes wrong
     */
    private static Collection getFieldGroupsNotHavingAccessOnPage(int pageID,
            JahiaField field, ProcessingContext jParams, Map engineMap)
            throws JahiaException {
        if (logger.isDebugEnabled())
            logger.debug("Comparing ACLs of field " + field.getID()
                    + " and page " + pageID);
        final ContentPage page = ContentPage.getPage(pageID);
        final JahiaBaseACL aclObject = getFieldACLObject(field, engineMap);

        // Define an ACLEntry for "Read" Rights
        final JahiaAclEntry aclEntry = new JahiaAclEntry();
        aclEntry.setPermission(JahiaBaseACL.READ_RIGHTS, JahiaAclEntry.ACL_YES);

        // Get all the groups which can see the field
        final List fieldACLGroups = aclObject
                .getGroupnameListNoAdmin(aclEntry);
        if (logger.isDebugEnabled())
            logger.debug("fieldACLGroups: " + fieldACLGroups);

        if (page == null) {
            return fieldACLGroups;
        }

        // Get all the groups which can see the target page
        final List pageACLGroups = page.getACL().getGroupnameListNoAdmin(
                aclEntry);
        if (logger.isDebugEnabled())
            logger.debug("pageACLGroups: " + pageACLGroups);

        final List result = new ArrayList(fieldACLGroups.size());

        final Iterator ite = fieldACLGroups.iterator();
        while (ite.hasNext()) {
            String groupID = (String) ite.next();

            if (!pageACLGroups.contains(groupID)) {
                // get rid of the site ID
                groupID = groupID.split(":")[0];

                // check for special case - a page from another site and a guest
                // group
                if (page.getSiteID() != jParams.getSiteID()
                        && JahiaGroupManagerDBProvider.GUEST_GROUPNAME
                                .equals(groupID)
                        && pageACLGroups
                                .contains(JahiaGroupManagerDBProvider.GUEST_GROUPNAME
                                        + ":" + page.getSiteID())) {
                    continue;
                }

                // only add the group name once
                if (!result.contains(groupID)) {
                    result.add(groupID);
                    if (logger.isDebugEnabled())
                        logger.debug("Adding group " + groupID + " to result");
                }
            }
        }

        return result;
    }

    /**
     * Returns a Collection of String of comma separated JahiaGroupIDs not
     * having Read Access on the given target page, but that do have access on
     * the current BigText field.
     * 
     * @param pageID
     *            The target page ID
     * @param field
     *            The current BigText field
     * @param jParams
     *            ProcessingContext instance of this request
     * @param engineMap
     *            Map instance for this engine
     * @return a String of comma separated JahiaGroupIDs
     * @throws JahiaException
     *             If Something goes wrong
     */
    public static String getFieldGroupsNotHavingAccessOnPageAsString(
            int pageID, JahiaField field, ProcessingContext jParams,
            Map engineMap) throws JahiaException {

        final Collection c = getFieldGroupsNotHavingAccessOnPage(pageID, field,
                jParams, engineMap);
        final Iterator ite = c.iterator();

        StringBuffer buff = new StringBuffer();
        while (ite.hasNext()) {
            final String groupID = (String) ite.next();
            buff.append(groupID).append(",");
        }

        if (buff.length() > 0) {
            buff.deleteCharAt(buff.length() - 1);
        }

        return buff.toString();
    }

    /**
     * Returns a String representing the state of a page. The String returned is
     * the name of a CSS class which will be used by the editor to format the
     * page title according to the CSS class. Thus, you should not modify the
     * return values of this method, without changing the CSS class names of the
     * JahiaLinker plugin (jahia-linker.css).
     * 
     * @param pageId
     *            The pageId to get the state of
     * @param currentLanguageCode
     *            The language of the page to get the state from
     * @return A String representing the state of the page and a CSS class name
     * @throws JahiaException
     *             If Something goes wrong
     */
    private static String getPageState(int pageId, String currentLanguageCode)
            throws JahiaException {
        final ContentPage page = ContentPage.getPage(pageId);
        if (page == null) {
            return "staging";
        }

        final Map languagesStates = ServicesRegistry.getInstance()
                .getWorkflowService().getLanguagesStates(page);
        final Integer state = ((Integer) languagesStates
                .get(currentLanguageCode));

        if (state == null) {
            return "staging";
        }

        if (page.isMarkedForDelete()) {
            return "markForDeleted";
        }

        if (page.getActiveVersionID() > 0) {
            return "staging_OK";
        }

        switch (state) {
        case EntryLoadRequest.ACTIVE_WORKFLOW_STATE:
            return "active";

        case EntryLoadRequest.WAITING_WORKFLOW_STATE:
            return "waiting";

        default:
            return "staging";
        }
    }

    /**
     * Initializes an instance of this class.
     */
    private LinkIntegrityChecker() {
        super();
    }

    public static EngineMessages checkField(final ProcessingContext jParams,
            final Map engineMap, final JahiaBigTextField theField,
            final boolean allLangs) throws JahiaException {
        final EngineLanguageHelper elh = (EngineLanguageHelper) engineMap
                .get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
        final Locale oldLoc;
        if (elh == null) {
            oldLoc = jParams.getLocale();
        } else {
            oldLoc = elh.getPreviousLocale();
        }
        final String value = theField.getValue();

        theField.cleanUpHardCodedLinks(value, jParams, elh != null ? elh
                .getCurrentLocale() : jParams.getLocale(), null);
        
        final Set pids = theField.getInternalLinks();
        final Set wrongKeys = theField.getWrongURLKeys();
        final EngineMessages result = new EngineMessages();

        if (pids.size() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("PIDs are: " + pids);
            }

            final Iterator ite = pids.iterator();
            final List langs;
            final String currentLang;
            // counter for the "for" loop coming up
            final int count;

            if (allLangs) {
                langs = jParams.getSite().getLanguageSettings(true);
                count = langs.size();
                currentLang = null;

            } else {
                currentLang = jParams.getLocale().toString();
                count = 1;
                langs = null;
            }

            // For each page referenced in a hardcoded link
            while (ite.hasNext()) {
                final int pid = (Integer) ite.next();
                String pageId = Integer.toString(pid);
                final ContentPage page;
                try {
                    page = ContentPage.getPage(pid);
                } catch (JahiaPageNotFoundException e) {
                    final EngineMessage msg = new EngineMessage(
                            "org.jahia.engines.shared.BigText_Field.notExistingWarning",
                            pageId);
                    result.add("BigText.notExisting", msg);
                    continue;
                }

                if (page == null) {
                    final EngineMessage msg = new EngineMessage(
                            "org.jahia.engines.shared.BigText_Field.notExistingWarning",
                            pageId);
                    result.add("BigText.notExisting", msg);
                    continue;
                }
                String pageTitle = page.getTitle(jParams.getEntryLoadRequest());
                pageTitle = pageTitle != null ? pageTitle : "N.A.";
                if (page.getSiteID() == jParams.getSiteID()) {
                    // page from this site
                    pageId = new StringBuffer(pageTitle.length() + 16).append(
                            pageTitle).append(" (id=").append(pid).append(")")
                            .toString();
                } else {
                    // page from another site
                    JahiaSite anotherSite = page.getSite();
                    pageId = new StringBuffer(pageTitle.length() + 32).append(
                            pageTitle).append(" (id=").append(pid).append(
                            ", site[").append(anotherSite.getID())
                            .append("]='").append(anotherSite.getTitle())
                            .append("')").toString();
                }

                final String noAccess = getFieldGroupsNotHavingAccessOnPageAsString(
                        pid, theField, jParams, engineMap);

                if (noAccess != null && noAccess.length() > 0) {
                    final EngineMessage msg = new EngineMessage(
                            "org.jahia.engines.shared.BigText_Field.lessAccessWarning",
                            new String[] { pageId, noAccess });
                    result.add("BigText.staging", msg);
                }

                // For each active site language or only the current language
                for (int i = 0; i < count; i++) {
                    final String lang;
                    final String state;

                    if (allLangs) {
                        lang = ((SiteLanguageSettings) langs.get(i))
                                .getCode();

                    } else {
                        lang = currentLang;
                    }

                    if (pid != jParams.getPageID()) { // do not check links to same page
                        state = getPageState(pid, lang);
    
                        if ("active".equals(state) || "staging_OK".equals(state)) {
                            continue;
                        }
                        if ("staging".equals(state)) {
                            if (page.getActiveVersionID() <= 0) {
                                final EngineMessage msg = new EngineMessage(
                                        "org.jahia.engines.shared.BigText_Field.stagingWarning",
                                        pageId);
                                result.add("BigText.staging", msg);
    
                            }
    
                        } else if ("markForDeleted".equals(state)) {
                            final EngineMessage msg = new EngineMessage(
                                    "org.jahia.engines.shared.BigText_Field.markForDeleteWarning",
                                    pageId);
                            result.add("BigText.markForDeleted", msg);
    
                        } else if ("waiting".equals(state)) {
                            if (page.getActiveVersionID() <= 0) {
                                final EngineMessage msg = new EngineMessage(
                                        "org.jahia.engines.shared.BigText_Field.approvalWarning",
                                        new String[] {
                                                pageId,
                                                new Locale(lang)
                                                        .getDisplayLanguage(oldLoc) });
                                result.add("BigText.waiting", msg);
    
                            }
    
                        } else {
                            jParams.setCurrentLocale(oldLoc);
                            throw new JahiaException("Unknown state value: "
                                    + state, "Unknown state value: " + state,
                                    JahiaException.ENGINE_ERROR,
                                    JahiaException.ERROR_SEVERITY);
                        }
                    }
                }
            }
        }

        if (wrongKeys.size() > 0) {
            final Iterator ite = wrongKeys.iterator();
            while (ite.hasNext()) {
                final EngineMessage msg = new EngineMessage(
                        "org.jahia.engines.shared.BigText_Field.wrongKeys", ite
                                .next());
                result.add("BigText.wrongURLKeys", msg);
            }
        }
        if (logger.isDebugEnabled())
            logger.debug("Returning " + result.getSize()
                    + " warning message(s): " + result);
        jParams.setCurrentLocale(oldLoc);

        return result;
    }

}
