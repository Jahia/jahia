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

import org.apache.struts.action.*;
import org.jahia.clipbuilder.html.struts.Util.JahiaAbstractWizardForm;
import javax.servlet.http.*;
import org.jahia.clipbuilder.html.bean.*;
import org.jahia.clipbuilder.html.struts.Util.*;

/**
 *  Description of the Class
 *
 *@author    Tlili Khaled
 */
public class DescriptionClipperForm extends JahiaAbstractWizardForm {
    private String webClippingDescription;
    private String webClippingName;
    private String webClippingTargetUrl;
    /**
     *  Sets the WebClippingTargetUrl attribute of the DescriptionClipperForm
     *  object
     *
     *@param  webClippingTargetUrl  The new WebClippingTargetUrl value
     */
    public void setWebClippingTargetUrl(String webClippingTargetUrl) {
        this.webClippingTargetUrl = webClippingTargetUrl;
    }


    /**
     *  Sets the WebClippingDescription attribute of the DescriptionClipperForm
     *  object
     *
     *@param  webClippingDescription  The new WebClippingDescription value
     */
    public void setWebClippingDescription(String webClippingDescription) {
        this.webClippingDescription = webClippingDescription;
    }


    /**
     *  Sets the WebClippingName attribute of the DescriptionClipperForm object
     *
     *@param  webClippingName  The new WebClippingName value
     */
    public void setWebClippingName(String webClippingName) {
        this.webClippingName = webClippingName;
    }



    /**
     *  Gets the WebClippingTargetUrl attribute of the DescriptionClipperForm
     *  object
     *
     *@return    The WebClippingTargetUrl value
     */
    public String getWebClippingTargetUrl() {
        return webClippingTargetUrl;
    }


    /**
     *  Gets the WebClippingDescription attribute of the DescriptionClipperForm
     *  object
     *
     *@return    The WebClippingDescription value
     */
    public String getWebClippingDescription() {
        return webClippingDescription;
    }



    /**
     *  Gets the WebClippingName attribute of the DescriptionClipperForm object
     *
     *@return    The WebClippingName value
     */
    public String getWebClippingName() {
        return webClippingName;
    }



    /**
     *  Gets the Id attribute of the DescriptionClipperForm object
     *
     *@return    The Id value
     */
    public int getId() {
        return JahiaClipBuilderConstants.DESCRIPTION;
    }


    /**
     *  Description of the Method
     *
     *@param  cBean  Description of Parameter
     */
    public void loadFromClipperBean(ClipperBean cBean) {
        setWebClippingName(cBean.getName());
        setWebClippingTargetUrl(cBean.getTargetUrl());
        setWebClippingDescription(cBean.getName());
    }


    /**
     *  Description of the Method
     *
     *@param  mapping  Description of Parameter
     *@param  request  Description of Parameter
     *@return          Description of the Returned Value
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }

}
