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

 package org.jahia.clipbuilder.html.struts;

import java.util.*;

import javax.servlet.http.*;

import org.jahia.clipbuilder.html.bean.*;
import org.jahia.clipbuilder.html.struts.Util.*;
import org.apache.struts.action.*;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class EditParamForm extends JahiaAbstractWizardForm {
    private String resetAll;
    private String selectedUrl;
    private List actifFormParamsList;
    private List actifQueryParamsList;
    private String bodyContent;
    private String showLabel;
    private String showHTML;
    private String update;
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(EditParamForm.class);



    /**
     *  Sets the ResetAll attribute of the EditParamForm object
     *
     *@param  resetAll  The new ResetAll value
     */
    public void setResetAll(String resetAll) {
        this.resetAll = resetAll;
    }


    /**
     *  Sets the SelectedUrl attribute of the EditParamForm object
     *
     *@param  selectedUrl  The new SelectedUrl value
     */
    public void setSelectedUrl(String selectedUrl) {
        this.selectedUrl = selectedUrl;
    }


    /**
     *  Sets the ActifParamsList attribute of the EditParamForm object
     *
     *@param  actifParamsList  The new ActifParamsList value
     */
    public void setActifFormParamsList(List actifParamsList) {
        this.actifFormParamsList = actifParamsList;
    }



    /**
     *  Sets the BodyContent attribute of the EditParamForm object
     *
     *@param  bodyContent  The new BodyContent value
     */
    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }


    /**
     *  Sets the ShowLabel attribute of the EditParamForm object
     *
     *@param  showLabel  The new ShowLabel value
     */
    public void setShowLabel(String showLabel) {
        this.showLabel = showLabel;
    }


    /**
     *  Sets the ShowHTML attribute of the EditParamForm object
     *
     *@param  showHTML  The new ShowHTML value
     */
    public void setShowHTML(String showHTML) {
        this.showHTML = showHTML;
    }


    /**
     *  Sets the ActifQueryParamsList attribute of the EditParamForm object
     *
     *@param  actifQueryParamsList  The new ActifQueryParamsList value
     */
    public void setActifQueryParamsList(List actifQueryParamsList) {
        this.actifQueryParamsList = actifQueryParamsList;
    }


    /**
     *  Sets the Update attribute of the EditParamForm object
     *
     *@param  update  The new Update value
     */
    public void setUpdate(String update) {
        this.update = update;
    }


    /**
     *  Gets the Id attribute of the EditParamForm object
     *
     *@return    The Id value
     */
    public int getId() {
        return JahiaClipBuilderConstants.EDITPARAM;
    }


    /**
     *  Gets the ResetAll attribute of the EditParamForm object
     *
     *@return    The ResetAll value
     */
    public String getResetAll() {
        return resetAll;
    }


    /**
     *  Gets the SelectedUrl attribute of the EditParamForm object
     *
     *@return    The SelectedUrl value
     */
    public String getSelectedUrl() {
        return selectedUrl;
    }


    /**
     *  Gets the ActifParamsList attribute of the EditParamForm object
     *
     *@return    The ActifParamsList value
     */
    public List getActifFormParamsList() {
        return actifFormParamsList;
    }


    /**
     *  Gets the BodyContent attribute of the EditParamForm object
     *
     *@return    The BodyContent value
     */
    public String getBodyContent() {
        return bodyContent;
    }


    /**
     *  Gets the ShowLabel attribute of the EditParamForm object
     *
     *@return    The ShowLabel value
     */
    public String getShowLabel() {
        return showLabel;
    }


    /**
     *  Gets the Errors attribute of the EditParamForm object
     *
     *@return    The Errors value
     */
    public ActionErrors getErrors() {
        ActionErrors errors = new ActionErrors();
        return errors;
    }



    /**
     *  Gets the ShowHTML attribute of the EditParamForm object
     *
     *@return    The ShowHTML value
     */
    public String getShowHTML() {
        return showHTML;
    }


    /**
     *  Gets the ActifQueryParamsList attribute of the EditParamForm object
     *
     *@return    The ActifQueryParamsList value
     */
    public List getActifQueryParamsList() {
        return actifQueryParamsList;
    }


    /**
     *  Gets the Update attribute of the EditParamForm object
     *
     *@return    The Update value
     */
    public String getUpdate() {
        return update;
    }


    /**
     *  Description of the Method
     *
     *@param  actionMapping       Description of Parameter
     *@param  httpServletRequest  Description of Parameter
     *@return                     Description of the Returned Value
     */
    public ActionErrors validate(ActionMapping actionMapping,
                                 HttpServletRequest httpServletRequest) {

        return null;
    }


    /**
     *  Description of the Method
     *
     *@param  actionMapping       Description of Parameter
     *@param  httpServletRequest  Description of Parameter
     */
    public void reset(ActionMapping actionMapping,
                      HttpServletRequest httpServletRequest) {
    }


    /**
     *  Description of the Method
     *
     *@param  cBean  Description of Parameter
     */
    public void loadFromClipperBean(ClipperBean cBean) {

        String position = getSelectedUrl();

        // this can occur the first time the edit page is accesed
        if (position == null) {
            //there is at leat one element in the urlListBean
            position = "0";
            setSelectedUrl(position);
            logger.debug("[ First url selected autommatically]");
        }

        //Activate form list
        int posUrl = Integer.parseInt(position);
        List fList = cBean.getAllFormParam(posUrl);
        if (fList.isEmpty()) {
            logger.debug("[ Form Param list is empty ]");
        }
        setActifFormParamsList(fList);
        logger.debug("[ Actif Parameters List is set from first url at position " +posUrl + " ]");

        //Activate query list
        List qList = cBean.getAllQueryParam(posUrl);
        if (qList.isEmpty()) {
            logger.debug("[ Query param list is empty ]");
        }

        setActifQueryParamsList(qList);
    }

}
