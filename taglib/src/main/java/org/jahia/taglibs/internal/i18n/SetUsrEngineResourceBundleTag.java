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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.taglibs.internal.i18n;

import org.jahia.data.JahiaData;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.GrpUsrEngineResourceBundle;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.security.Principal;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * This tag should be called on top of each template JSP file to associate a res bun. with a given grp.
 * It is used to allow template designer to provide a different engine resource bunlde
 * that Jahia will use to give different look to the engines
 * popup that are opened from pages using this template and for a given user.
 *
 * @author Khue Nguyen
 *
 * @jsp:tag name="setUsrResourceBundle" body-content="empty"
 * description="change the default engine resourcebundle for a given user name.
 *
 * <p><attriInfo>It is used to allow template designers to change the default engine resource bundle
 * used by engine popups opened from pages using this template on a per user basis. This therefore enables for different popups
 * styles for given users.
 *
 * <p>This tag should be called on top of the template JSP file.
 *
 * <p>Please refer to the 'Engine Popup Customization' documentation for more details.
 *
 * <p><b>Example :</b>
 *
 * <p>
 * &lt;content:setUsrEngineResourceBundle resourceBundle=\"albertResource\" userName=\"albert\" /&gt;
 *
 * <p>where albertResource is the name of Resource Bundle file for the user \"albert\"
 * so that we get albertResource.properties .
 *
 * </attriInfo>"
 */
public class SetUsrEngineResourceBundleTag extends TagSupport {

    private String resourceBundle = "";
    private String localeLangage  = "";
    private String localeCountry  = "";
    private String localeVariant  = "";
    private Locale locale = null;
    private String userName = null;

    /**
     * @jsp:attribute name="resourceBundle" required="false" rtexprvalue="true"
     * description="the resource bundle identifier to assign to the user.
     *
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setResourceBundle(String resourceBundle) {
        if ( resourceBundle == null )
            resourceBundle = "";
        this.resourceBundle = resourceBundle.trim();
    }

    /**
     * @jsp:attribute name="localeLanguage" required="false" rtexprvalue="true"
     * description="the ISO Language Code to use for the resource bundle.
     *
     * <p><attriInfo><a href="http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt">
     * <code>iso639 details</code></a>
     * </attriInfo>"
     */
    public void setLocaleLanguage(String localeLanguage) {
        if ( localeLangage != null )
            this.localeLangage = localeLangage.trim();
    }

    /**
      * @jsp:attribute name="localeCountry" required="false" rtexprvalue="true"
      * description="the ISO Country Code to use for the resource bundle.
      *
      * <p><attriInfo><a href="http://www.chemie.fu-berlin.de/diverse/doc/ISO_3166.html">
      * <code>iso3166 details</code></a>
      * </attriInfo>"
     */
    public void setLocaleCountry(String localeCountry) {
        if ( localeCountry != null )
            this.localeCountry = localeCountry.trim();
    }

    /**
      * @jsp:attribute name="localeVariant" required="false" rtexprvalue="true"
      * description="The variant argument is a vendor or browser-specific code.
      *
      * <p><attriInfo>For example, use WIN for Windows, MAC for Macintosh, and POSIX for POSIX. See
      * java.util.Locale for more info.
      * </attriInfo>"
     */
    public void setLocaleVariant(String localeVariant) {
        if ( localeVariant != null )
            this.localeVariant = localeVariant.trim();
    }

    /**
      * @jsp:attribute name="userName" required="true" rtexprvalue="true"
      * description="the user name to associate the specified resource bundle with.
      *
      * <p><attriInfo>
      * </attriInfo>"
     */
    public void setUserName(String userName) {
        if ( userName != null )
            this.userName = userName.trim();
    }

    public int doStartTag() {


        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        ResourceBundle res = null;

        if ( localeLangage.equals("") ){
            locale = jData.getProcessingContext().getLocale();
        } else {
            locale = new Locale(localeLangage,localeCountry,localeVariant);
        }

        res = ResourceBundle.getBundle(resourceBundle,locale);

        if (res != null) {
            try {
                JahiaUser usr = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(this.userName);
                if ( usr == null ){
                    // check if the user is the root admin.
                    usr = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(this.userName);
                }
                if ( usr != null ){
                    GrpUsrEngineResourceBundle.getInstance().addResourceBundle( jData.getProcessingContext().getContentPage(), (Principal)usr, res );
                }
            } catch (Exception t){
            }
        }

        return SKIP_BODY;

    }
    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        resourceBundle = "";
        localeLangage  = "";
        localeCountry  = "";
        localeVariant  = "";
        locale = null;
        userName = null;
        return EVAL_PAGE;
    }
}
