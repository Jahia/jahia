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
