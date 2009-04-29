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
 package org.jahia.clipbuilder.html.struts;

import org.jahia.clipbuilder.html.struts.Util.JahiaAbstractWizardAction;
import org.jahia.clipbuilder.html.struts.Util.*;
import org.apache.struts.action.*;
import javax.servlet.http.*;
import org.jahia.clipbuilder.html.*;
import org.jahia.clipbuilder.html.bean.*;
import java.util.*;

/**
 *  Action linked whith editParams.jsp
 *
 *@author    Tlili Khaled
 */
public class EditAction extends JahiaAbstractWizardAction {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(EditAction.class);


    /**
     *  Gets the FormId attribute of the EditAction object
     *
     *@return    The FormId value
     */
    public int getFormId() {
        return JahiaClipBuilderConstants.EDITPARAM;
    }


    /**
     *  Gets the KeyMethodMap attribute of the BrowseAction object
     *
     *@return    The KeyMethodMap value
     */
    public Map getKeyMethodMap() {
        Map map = super.getKeyMethodMap();
        map.put("wizard", "view");
        map.put("edit.backToSelect", "backToSelect");
        map.put("edit.reset", "reset");
        map.put("edit.resetAll", "resetAll");
        map.put("edit.validate", "validate");
        map.put("edit.view.change", "changeView");

        return map;
    }


    /**
     *  Description of the Method
     *
     *@param  actionMapping        Description of Parameter
     *@param  actionForm           Description of Parameter
     *@param  httpServletRequest   Description of Parameter
     *@param  httpServletResponse  Description of Parameter
     *@return                      Description of the Returned Value
     */
    public ActionForward view(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        logger.debug("[ View ]");
        SessionManager.getWebBrowserForm(request).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_LAST_DOCUMENT);
        checkEditableParamaters(request);

        return actionMapping.getInputForward();
    }



    /**
     *  Called when back button is selected
     *
     *@param  actionMapping        Description of Parameter
     *@param  actionForm           Description of Parameter
     *@param  httpServletRequest   Description of Parameter
     *@param  httpServletResponse  Description of Parameter
     *@return                      Description of the Returned Value
     */
    public ActionForward backToSelect(ActionMapping actionMapping,
                                      ActionForm actionForm,
                                      HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse) {
        logger.debug("[ Back to select page ]");
        return actionMapping.findForward("viewSelectPart");
    }



    /**
     *  Description of the Method
     *
     *@param  actionMapping        Description of Parameter
     *@param  actionForm           Description of Parameter
     *@param  request              Description of Parameter
     *@param  httpServletResponse  Description of Parameter
     *@return                      Description of the Returned Value
     */
    public ActionForward init(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse httpServletResponse) {
        logger.debug("[ Init ]");
        super.init(actionMapping, actionForm, request, httpServletResponse);

        EditParamForm editParamForm = (EditParamForm) actionForm;
        ActionForward forward = actionMapping.getInputForward();

        checkEditableParamaters(request);

        // set the list of parameter to be show
        activateParamsList(editParamForm, request);

        // set the document html to be printed
        setWebBrowserContent(request, httpServletResponse, editParamForm);
        return forward;
    }


    /**
     *  Description of the Method
     *
     *@param  actionMapping        Description of Parameter
     *@param  actionForm           Description of Parameter
     *@param  httpServletRequest   Description of Parameter
     *@param  httpServletResponse  Description of Parameter
     *@return                      Description of the Returned Value
     */
    public ActionForward changeView(ActionMapping actionMapping,
                                    ActionForm actionForm,
                                    HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse) {
        logger.debug("[ changeView ]");

        EditParamForm editParamForm = (EditParamForm) actionForm;

        // set the list of parameter to be show
        activateParamsList(editParamForm, httpServletRequest);

        // set the document html to be printed
        setWebBrowserContent(httpServletRequest, httpServletResponse, editParamForm);

        return actionMapping.getInputForward();
    }


    /**
     *  Called when reset button is selected
     *
     *@param  actionMapping        Description of Parameter
     *@param  actionForm           Description of Parameter
     *@param  httpServletRequest   Description of Parameter
     *@param  httpServletResponse  Description of Parameter
     *@return                      Description of the Returned Value
     */
    public ActionForward reset(ActionMapping actionMapping, ActionForm actionForm,
                               HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) {
        EditParamForm editParamForm = (EditParamForm) actionForm;

        //Retrieve the clipper
        ClipperBean bean = SessionManager.getClipperBean(httpServletRequest);

        //Get the current url bean
        int posUrl = Integer.parseInt(editParamForm.getSelectedUrl());
        UrlBean uBean = bean.getUrlBean(posUrl);

        // process only the selected url
        resetFormParamMapping(uBean);

        logger.debug("[Param of url " + uBean.getAbsoluteURL() + " has beean reseted]");

        return actionMapping.findForward("editParams");
    }


    /**
     *  Called when resetAll button is selected
     *
     *@param  actionMapping        Description of Parameter
     *@param  actionForm           Description of Parameter
     *@param  httpServletRequest   Description of Parameter
     *@param  httpServletResponse  Description of Parameter
     *@return                      Description of the Returned Value
     */
    public ActionForward resetAll(ActionMapping actionMapping,
                                  ActionForm actionForm,
                                  HttpServletRequest httpServletRequest,
                                  HttpServletResponse httpServletResponse) {
        logger.debug("[ Reset all params ]");

        //Retrieve the clipper
        ClipperBean bean = SessionManager.getClipperBean(httpServletRequest);

        // updtae the mapping for each form param
        List uBeanList = bean.getUrlListBean();

        // perform reset for all bean
        for (int i = 0; i < uBeanList.size(); i++) {
            UrlBean uBean = (UrlBean) uBeanList.get(i);
            resetFormParamMapping(uBean);
        }

        return actionMapping.findForward("editParams");
    }


    /**
     *  Called when validate button is selected
     *
     *@param  actionMapping        Description of Parameter
     *@param  actionForm           Description of Parameter
     *@param  httpServletRequest   Description of Parameter
     *@param  httpServletResponse  Description of Parameter
     *@return                      Description of the Returned Value
     */
    public ActionForward validate(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        EditParamForm editParamForm = (EditParamForm) actionForm;

        //Retrieve the clipper
        ClipperBean bean = SessionManager.getClipperBean(httpServletRequest);

        //get all the parameter
        Map params = getParam(httpServletRequest);

        //Get the current url bean
        int posUrl = Integer.parseInt(editParamForm.getSelectedUrl());
        UrlBean uBean = bean.getUrlBean(posUrl);

        // updtae preferences for each form param
        List fBeanList = uBean.getFormParamBeanList();
        for (int i = 0; i < fBeanList.size(); i++) {
            FormParamBean fBean = (FormParamBean) fBeanList.get(i);
            String mapping = ((String[]) params.get("mapping"))[i];
            String visibility = ((String[]) params.get("visibility"))[i];
            String update = ((String[]) params.get("update"))[i];
            String useAsDefaultValue = ((String[]) params.get("useAsDefaultValue"))[i];
            //logger.debug("[ form param found: name," + name + " mapping " + mapping + "]");
            fBean.setMapping(mapping);
            fBean.setVisibility(visibility);
            fBean.setUpdate(update);
            fBean.setUseAsDefaultValue(useAsDefaultValue);
        }

        logger.debug("[ Validate action performed ]");

        return actionMapping.findForward("editParams");
    }


    /**
     *  process the selected Url
     *
     *@param  httpServletRequest   Description of Parameter
     *@param  httpServletResponse  The new BodyContent value
     *@param  editParamForm        Description of Parameter
     */
    private void setWebBrowserContent(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                      EditParamForm editParamForm) {

        // Get the position of url
        String position = editParamForm.getSelectedUrl();
        int posUrl = Integer.parseInt(position);

        //Show the html document
        SessionManager.getWebBrowserForm(httpServletRequest).setPositionUrl(posUrl);
        SessionManager.getWebBrowserForm(httpServletRequest).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_EDIPARAMS);
        if (editParamForm.getShowHTML() != null) {
            // Show label is selected: Get the document whit label
            if (editParamForm.getShowLabel() != null) {
                logger.debug("Show HTML Label");
                SessionManager.getWebBrowserForm(httpServletRequest).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_EDIPARAMS);

            }
            // Get the document whitout the label
            else {
                logger.debug("Show HTML");
                SessionManager.getWebBrowserForm(httpServletRequest).setShow(JahiaClipBuilderConstants.WEB_BROWSER_SHOW_LAST_DOCUMENT);

            }
        }
    }


    /**
     *  Description of the Method
     *
     *@param  request  Description of Parameter
     */
    private void checkEditableParamaters(HttpServletRequest request) {
        ClipperBean cBean = SessionManager.getClipperBean(request);
        if (cBean.getUrlListBean().size() < 2) {
            ActionMessages actionErrors = new ActionMessages();
            ActionMessage e = new ActionMessage("information.noParameters");
            actionErrors.add("information.noParameters", e);
            saveErrors(request, actionErrors);
        }
    }


    /**
     *  Description of the Method
     *
     *@param  uBean  Description of Parameter
     */
    private void resetFormParamMapping(UrlBean uBean) {
        List fBeanList = uBean.getFormParamBeanList();
        for (int i = 0; i < fBeanList.size(); i++) {
            FormParamBean fBean = (FormParamBean) fBeanList.get(i);
            String name = fBean.getName();
            fBean.setMapping(name);
        }

    }


    /**
     *  Description of the Method
     *
     *@param  editParamForm  Description of Parameter
     *@param  request        Description of Parameter
     */
    private void activateParamsList(EditParamForm editParamForm,
                                    HttpServletRequest request) {
        ClipperBean bean = SessionManager.getClipperBean(request);
        String position = editParamForm.getSelectedUrl();

        // this can occur the first time the edit page is accesed
        if (position == null) {
            //there is at leat one element in the urlListBean
            position = "0";
            editParamForm.setSelectedUrl(position);
            logger.debug("[ First url selected autommatically]");
        }

        //Activate form list
        int posUrl = Integer.parseInt(position);
        List fList = bean.getAllFormParam(posUrl);
        if (fList.isEmpty()) {
            logger.debug("[ Form Param list is empty ]");
        }
        editParamForm.setActifFormParamsList(fList);
        logger.debug("[ Actif Parameters List is set from first url at position " +
                posUrl + " ]");

        //Activate query list
        List qList = bean.getAllQueryParam(posUrl);
        if (qList.isEmpty()) {
            logger.debug("[ Query param list is empty ]");
        }

        editParamForm.setActifQueryParamsList(qList);

    }

}
