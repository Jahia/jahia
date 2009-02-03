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

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.utils.JahiaTools;

/**
 * Support for Jahia Admin's ResourceBundle within Jahia
 *
 * Returns the requested resource.
 *
 * If the requested resource bundle is missing and useDefault is true,
 * Jahia will look for another resource bundle in that order :
 *
 *	1.  Look for the site's default admin resource bundle.
 *  	    Each site can have a default admin resource bundle. It's name
 *	    must be of this form : "JahiaAdminResourcesMYJAHIASITE"
 *	    where MYJAHIASITE is the virtual site's sitekey in uppercase.
 *
 *	2.  Finally if none of the previous resource bundles are available,
 *	    Jahia will return the default resource bundle
 *	    named "JahiaAdminResources".
 *
 *
 * @see JahiaResourceBundle
 * @see SetAdminResourceBundleTag
 * @see JahiaEnginesResources.properties
 *
 * @author Khue Nguyen
 *
 *
 * @jsp:tag name="adminResourceBundle" body-content="empty"
 * description="display a specific value from Jahia Admin's ResourceBundle.
 *
 * <p><attriInfo>This tag can be used to dynamically request a given resource from the admin resource bundle.
 *
 * <p>See <a href='resourceBundle.html' target='tagFrame'>content:resourceBundle</a>.
 *
 * <p>If the requested resource bundle is missing and useDefault is true,
 * Jahia will look for another resource bundle in that order :
 *
 * <p>1.  Look for the site's default admin resource bundle.
 *  	    Each site can have a default admin resource bundle. It's name
 *	    must be of this form : \"JahiaAdminResourcesMYJAHIASITE\"
 *	    where MYJAHIASITE is the virtual site's sitekey in uppercase.
 *
 * <p>2.  Finally if none of the previous resource bundles are available,
 *	    Jahia will return the default resource bundle
 *	    named \"JahiaAdminResources\".
 *
 * <p><b>Example :</b>
 *
 * &lt;content:adminResourceBundle resourceName=\"org.jahia.admin.unknownErrorOccured.label\"/&gt;
 *
 * </attriInfo>"
 */
public class AdminResourceBundleTag extends TagSupport {

    private static final org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(AdminResourceBundleTag.class);

    public static final String JAHIA_ENGINE_TAG = "<jahiaEngine>";
    public static final String SITE_TEMPLATE_TAG = "<siteTemplate>";

    private String resourceName = "";
    private String defaultValue = "";

    /**
     * @jsp:attribute name="resourceName" required="true" rtexprvalue="true"
     * description="the key of the resource to fetch in the admin resource bundle.
     *
     * <p><attriInfo>The resourceName attribute is parsed for two special tags and are replaced by the appropriate value,
     *  such that:
     *
     * <p>&lt;jahiaEngine&gt; 	: by /jsp/jahia/engines/
     * <p>&lt;siteTemplate&lt;	: by /jsp/jahia/templates/&lt;mysite&gt;/
     *					          where &lt;mysite&lt; is the sitekey of the current site
     *

     * </attriInfo>"
     */
    public void setResourceName (String resourceName) {
        if (resourceName == null) {
            resourceName = "";
        }
        this.resourceName = resourceName;
    }

    /**
       * @jsp:attribute name="defaultValue" required="false" rtexprvalue="true"
       * description="the value to use if the resource couldn't be accessed.
       *
       * <p><attriInfo>such as if the resourceBundle doesn't exist, couldn't be read or the resource key entry
       * couldn't be found.
       * </attriInfo>"
     */
    public void setDefaultValue (String value) {
        this.defaultValue = value;
    }

    public int doStartTag () {

        HttpServletRequest request = (HttpServletRequest) pageContext.
                                     getRequest();
        JahiaData jData = (JahiaData) request.getAttribute(
            "org.jahia.data.JahiaData");

        Locale currentLocale = request.getLocale();
        HttpSession session = pageContext.getSession();
        if (session != null) {
            if (session.getAttribute(ProcessingContext.SESSION_LOCALE) != null) {
                currentLocale = (Locale) session.getAttribute(ProcessingContext.
                    SESSION_LOCALE);
            }
        }

        String resValue = null;

        try {

            if (jData != null) {
                resValue = JahiaResourceBundle
                           .getAdminResource(resourceName,
                                             jData.getProcessingContext(),
                                             jData.getProcessingContext().getLocale());
            } else {
                // for any reason the jData wasn't loaded correctly
                ResourceBundle resBundle = JahiaResourceBundle
                                           .getAdminDefaultResourceBundle(null,
                    currentLocale);
                resValue = JahiaResourceBundle.getString(resBundle,
                    resourceName, currentLocale);
            }
        } catch (MissingResourceException mre) {
            logger.error(mre.toString(), mre);
        }

        if (resValue == null) {
            resValue = this.defaultValue;
        }

        try {
            JspWriter out = pageContext.getOut();
            if (jData != null) {
                out.print(parseResourceValue(resValue, jData.getProcessingContext()));
            } else {
                out.print(parseResourceValue(resValue, null));
            }
        } catch (IOException ioe) {
            logger.error(ioe.toString(), ioe);
        }

        return SKIP_BODY;

    }

    //--------------------------------------------------------------------------
    /**
     * parse the resource value and replace :
     * <jahiaEngine> 	: by /jsp/jahia/engines/
     * <siteTemplate>	: by /jsp/jahia/templates/<mysite>/
     *					  where <mysite> is the sitekey of the current site
     *
     * @param String val, the String to parse
     * @param ParamBean, the paramBean
     * @return String , the parsed value
     */
    public static String parseResourceValue (String val, ProcessingContext jParams) {

        if (val == null) {
            return val;
        }
        val = JahiaTools.replacePattern(val, JAHIA_ENGINE_TAG,
                                        "/jsp/jahia/engines/");
        if (jParams != null) {
            val = JahiaTools.replacePattern(val, SITE_TEMPLATE_TAG,
                                            "/jsp/jahia/templates/" +
                                            jParams.getSite().getSiteKey() +
                                            "/");
        }
        return val;
    }

    public int doEndTag ()
        throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        resourceName = "";
        return EVAL_PAGE;
    }

}
