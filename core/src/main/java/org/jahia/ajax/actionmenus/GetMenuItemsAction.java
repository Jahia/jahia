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
package org.jahia.ajax.actionmenus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.content.ContentObject;
import org.jahia.data.beans.ActionURIBean;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.beans.ContentBean;
import org.jahia.data.beans.FieldBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.gui.GuiBean;
import org.jahia.gui.HTMLToolBox;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 * Gets the items of a specific Action Menu
 *
 * @author Xavier Lawrence
 */
public class GetMenuItemsAction extends AjaxAction {

    private static final String AJAX_FIELDSET = "fieldset";
    private static final String AJAX_LAUNCHER = "launcher";
    private static final String AJAX_IMAGE = "image";
    private static final String AJAX_METHOD = "method";

    private static final String KEY = "key";
    private static final String TYPE = "type";
    private static final String DEF = "def";
    private static final String PARENT = "parent";
    private static final String DOMID = "domid";
    private static final String CONTEXTUALCONTAINERLISTID = "contextualContainerListId";

    private static final String DEFAULT_LOCK_IMAGE = "lock_grey.gif";

    private static final String SLASH = "/";
    private static final String GIF = ".gif";

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetMenuItemsAction.class);

    /**
     * Returns the menu items for the specified Action Menu.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward  (null)
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {

        if (logger.isDebugEnabled()) logger.debug("GetMenuItemsAction - execute");

        try {
            // Contains params that will have to given to the ParamBean Constructor for correct
            // initialization of the latter
            final ProcessingContext jParams = retrieveProcessingContext(request, response, true);

            final JahiaPage currentPage = jParams.getPage();
            final JahiaUser currentUser = jParams.getUser();
            final String objectType = getParameter(request, TYPE);
            final String objectID = getParameter(request, KEY);
            final String domID = getParameter(request, DOMID);
            final String contextualContainerListIDStr = getParameter(request, CONTEXTUALCONTAINERLISTID,"0");
            int contextualContainerListID = 0;
            if (contextualContainerListIDStr!=null){
                try {
                    contextualContainerListID = Integer.parseInt(contextualContainerListIDStr);
                } catch ( Exception e ){
                }
            }
            final ContentObject object = getContentObjectFromString(objectType + "_" + objectID);

            if (currentUser == null || object == null || ! object.checkWriteAccess(currentUser)) {
                if (currentUser == null) logger.debug("currentUser is null");
                if (object == null) logger.debug("object is null: " + objectType + "_" + objectID);

                if (jParams.getPage().checkWriteAccess(currentUser)) {
                    logger.debug("user has write access on currentPage: -> OK");
                } else {
                    throw new JahiaForbiddenAccessException(
                            "Unauthorized attempt to use AJAX Struts Action - GetMenuItemsAction."
                                    + " Must be logged in and have 'Write' access");
                }
            }

            final int pid = jParams.getPageID();
            if (logger.isDebugEnabled()) logger.debug("jParams: pid = " + pid +
                    ", user = " + currentUser.getName() +
                    ", mode = " + jParams.getOperationMode() +
                    ", lang = " + jParams.getLocale() +
                    ", site: " + jParams.getSite());

            final String definitionID = getParameter(request, DEF);
            final String parentID = getParameter(request, PARENT);

            if (logger.isDebugEnabled()) logger.debug("ProcessMenuRequest: objectType=" + objectType +
                    ", objectID=" + objectID +
                    ", definitionID=" + definitionID +
                    ", parentID=" + parentID +
                    ", pageID=" + pid);

            request.getSession().setAttribute("Select_Page_Entry", parentID);

            // The unique contentObject ID
            final int objID = Integer.parseInt(objectID);

            final EntryLoadRequest elr = new EntryLoadRequest(EntryLoadRequest.
                    STAGING_WORKFLOW_STATE,
                    0,
                    jParams.getLocales(),
                    org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
            EntryLoadRequest savedEntryLoadRequest = jParams.getSubstituteEntryLoadRequest();
            jParams.setSubstituteEntryLoadRequest(elr);

            final ContentBean bean;

            // Action Menu for a page
            if (PageBean.TYPE.equals(objectType)) {
                bean = new PageBean(currentPage, jParams);

                // Action Menu for a ContainerList
            } else if (ContainerListBean.TYPE.equals(objectType)) {
                final JahiaContainerList list;
                if (objID > 0) {
                    list = servicesRegistry.getJahiaContainersService().
                            loadContainerListInfo(objID, elr);
                } else {
                    final int parentid = Integer.parseInt(parentID);
                    if (parentid == pid) {
                        list = new JahiaContainerList(0, 0,
                                pid, Integer.parseInt(definitionID), 0);
                    } else {
                        list = new JahiaContainerList(0, parentid,
                                pid, Integer.parseInt(definitionID), 0);
                    }
                }
                if (logger.isDebugEnabled()) logger.debug("ContainerListID: " + list.getID() + ", def: " +
                        list.getDefinition().getID());
                bean = new ContainerListBean(list, jParams);

                // Action Menu for a Container
            } else if (ContainerBean.TYPE.equals(objectType)) {
                final JahiaContainer container = servicesRegistry.getJahiaContainersService().
                        loadContainer(objID, LoadFlags.ALL, jParams, elr);
                container.setContextualContainerListID(contextualContainerListID);
                bean = new ContainerBean(container, jParams);

                // Action Menu for a Field
            } else if (FieldBean.TYPE.equals(objectType)) {
                final JahiaField field = servicesRegistry.getJahiaFieldService().
                        loadField(objID, LoadFlags.ALL, jParams, elr);
                bean = new FieldBean(field, jParams);

            } else {
                throw new JahiaBadRequestException("Unknown 'ObjectType' value ! 'ObjectType' value should be '" +
                        PageBean.TYPE + "', '" + ContainerListBean.TYPE + "', '" +
                        ContainerBean.TYPE + "' or '" + FieldBean.TYPE + "'.");
            }

            // Get all the information regarding the Action entries
            final Map info = getActionsInfo(bean, jParams);
            // to display ajax response sended
            if (logger.isDebugEnabled()) logger.debug(getReadableInfo(info));

            jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);

            // Fill up the tag names (Order matters !!!)
            final List<String> xmlTagNames = new ArrayList<String>();
            xmlTagNames.add(DOMID);
            if (info.containsKey(AJAX_FIELDSET)) {
                xmlTagNames.add(AJAX_FIELDSET);
            }
            xmlTagNames.add(AJAX_LAUNCHER);
            xmlTagNames.add(AJAX_IMAGE);
            xmlTagNames.add(AJAX_METHOD);

            // Fill up the tag values (Order matters !!!)
            final List<Object> xmlTagValues = new ArrayList<Object>();
            xmlTagValues.add(domID);
            if (info.containsKey(AJAX_FIELDSET)) {
                xmlTagValues.add(info.get(AJAX_FIELDSET));
            }
            xmlTagValues.add(info.get(AJAX_LAUNCHER));
            xmlTagValues.add(info.get(AJAX_IMAGE));
            xmlTagValues.add(info.get(AJAX_METHOD));

            // Build and send the response message...
            sendResponse(xmlTagNames, xmlTagValues, response);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }

    /**
     * Retrieves all the information required to build the actual action menu.
     *
     * @param bean    The ContentBean for which the menu will be built
     * @param jParams ProcessingContext
     * @throws JahiaException
     */
    protected Map getActionsInfo(final ContentBean bean,
                                 final ProcessingContext jParams) throws JahiaException {
        final GuiBean gui = new GuiBean(jParams);
        final HTMLToolBox box = new HTMLToolBox(gui, jParams);
        final Map<String, String> result = new HashMap<String, String>();
        final Locale locale = jParams.getLocale();
        final String domid = jParams.getParameter(DOMID);
        final String[] domidParams = domid.split(HTMLToolBox.ID_SEPARATOR);
        //see HTMLToolBox.buildUniqueContentID() for param and ordering details
        String lockIcon = ("null".equals(domidParams[5])) ? null : domidParams[5];
        if (lockIcon != null)
            lockIcon = (String) HTMLToolBox.lockIconStore.get(new Integer(lockIcon));
        final String useFieldSet = domidParams[6];
        String resourceBundle = new String((new Base64()).decode(domidParams[7].getBytes()));
        String namePostFix = domidParams[8];
        if (namePostFix == null || "null".equals(namePostFix)) {
            namePostFix = "";
        }
//        if (resourceBundle != null) {
//            resourceBundle = (String) HTMLToolBox.resourceBundleStore.get(new Integer(resourceBundle));
//        }

        if (logger.isDebugEnabled()) {
            logger.debug("lockIcon for ContentBean " + bean.getID() + ": " + lockIcon);
            logger.debug("useFieldSet for ContentBean " + bean.getID() + ": " + useFieldSet);
            logger.debug("resourceBundle for ContentBean " + bean.getID() + ": " + resourceBundle);
            logger.debug("namePostFix for ContentBean " + bean.getID() + ": " + namePostFix);
        }

        if ("true".equals(useFieldSet)) {
            final String param;
            if (bean.isCompletelyLocked()) {
                param = "complete";

            } else if (bean.isPartiallyLocked()) {
                param = "partial";

            } else {
                param = null;
            }

            result.put(AJAX_FIELDSET, param);
        }

        final Iterator actionURIIter = bean.getActionURIBeans().entrySet().iterator();
        final StringBuffer launchers = new StringBuffer();
        final StringBuffer methods = new StringBuffer();
        final StringBuffer images = new StringBuffer();

        while (actionURIIter.hasNext()) {
            final Map.Entry curEntry = (Map.Entry) actionURIIter.next();
            final ActionURIBean curActionURIBean = (ActionURIBean) curEntry.getValue();

            final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();

            // Check if we can edit the container in the given language
            if ("update".equals(curActionURIBean.getName()) && ContainerBean.class == bean.getClass() &&
                    aclService.getSiteActionPermission("engines.languages." + jParams.getLocale().toString(),
                            jParams.getUser(),
                            JahiaBaseACL.READ_RIGHTS,
                            jParams.getSiteID()) <= 0) {
                continue;
            }

            if (curActionURIBean.isAuthorized() &&
                    (aclService.getSiteActionPermission("engines.actions." + curActionURIBean.getName(),
                            jParams.getUser(),
                            JahiaBaseACL.READ_RIGHTS,
                            jParams.getSiteID()) > 0)) {
                launchers.append(curActionURIBean.getLauncherUri()).append(DELIMITER);

                //check for the key and postfix without warnings
                String name = "";
                try {
                    name = ResourceBundle.getBundle(resourceBundle, locale).getString(curActionURIBean.getName() + namePostFix);
                } catch (MissingResourceException mre) {
                    name = null;
                }
                if (name == null || name.length() == 0) {
                    name = box.getResource(resourceBundle, curActionURIBean.getName());
                }
                if (name == null || name.length() == 0) {
                    name = curActionURIBean.getName();
                }
                methods.append(name).append(DELIMITER);
                images.append(box.getURLImageContext());

                if (curActionURIBean.isLocked()) {
                    if (lockIcon == null) {
                        images.append(SLASH).append(DEFAULT_LOCK_IMAGE);
                    } else {
                        images.append(SLASH).append(lockIcon);
                    }

                } else {
                    images.append(SLASH).append(curActionURIBean.getName()).append(GIF);
                }

                images.append(DELIMITER);
            }
        }

        if (methods.length() > 0) {
            result.put(AJAX_LAUNCHER, launchers.toString());
            result.put(AJAX_METHOD, methods.toString());
            result.put(AJAX_IMAGE, images.toString());
        }
        return result;
    }

    /**
     * internal method to debug the ajax responses
     *
     * @param map
     * @return a string
     */
    private String getReadableInfo(Map map) {
        String launchers = (String) map.get(AJAX_LAUNCHER);
        String methods = (String) map.get(AJAX_METHOD);
        String images = (String) map.get(AJAX_IMAGE);

        StringBuffer result = new StringBuffer();
        if (launchers != null && methods != null && images != null) {
            String[] launcherarrays = launchers.split(";{2}");
            String[] methodarrays = methods.split(";{2}");
            String[] imagearrays = images.split(";{2}");
            result.append("ACTIONS INFO\n");
            for (int i = 0; i < launcherarrays.length; i++) {
                result.append("[").append(i).append("] - LAUNCHERS: ").append(launcherarrays[i].replaceAll("\\n", ""));
                if (i < methodarrays.length) result.append(" METHODS: ").append(methodarrays[i].replaceAll("\\n", ""));
                else result.append(" METHODS: NA");
                if (i < imagearrays.length) result.append(" IMAGES: ").append(imagearrays[i].replaceAll("\\n", ""));
                else result.append(" IMAGES:NA");
                result.append("\n");
            }
        }
        return result.toString();
    }
}
