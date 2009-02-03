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
import org.jahia.utils.JahiaConsole;
import org.jahia.utils.JahiaTools;


/**
 * Support for Engine's ResourceBundle within Jahia
 *
 * Returns the requested resource.
 *
 * If the requested resource bundle is missing and useDefault is true,
 * Jahia will look for another engine resource bundle in that order :
 *
 *		1. 	Look for the engine resource bundle of the page.
 *			This resource bundle can be set in the template used by the page
 *			with the SetEngineResourceBundleTag.
 *
 *		2.  Look for the site's default engine resource bundle.
 *  	    Each site can have a default engine resource bundle. It's name
 *			must be of this form : "JahiaEnginesResourcesMYJAHIASITE"
 *			where MYJAHIASITE is the virtual site's sitekey in uppercase.
 *
 *		3.  Finally if none of the previous resource bundle are available,
 *			Jahia will return the internal engine's default resource bundle
 *			named "JahiaEnginesResources".
 *
 *
 * @see JahiaResourceBundle
 * @see SetEngineResourceBundleTag
 * @see JahiaEnginesResources.properties
 *
 * @author Khue Nguyen
 *
 *
 * @jsp:tag name="engineResourceBundle" body-content="empty"
 * description="display a specific value from Jahia Engine's ResourceBundle.
 *
 * <p><attriInfo>This tag can be used to dynamically request a given resource from the engine resource bundle.
 *
 *
 * <p>See <a href='resourceBundle.html' target='tagFrame'>content:resourceBundle</a>.
 *
 * <p>If the requested resource bundle is missing and useDefault is true,
 * Jahia will look for another engine resource bundle in that order :
 *
 * <p>1.  Look for the site's default engine resource bundle.
 *  	    Each site can have a default engine resource bundle. Its name
 *	    must be of this form : \"JahiaEnginesResourcesMYJAHIASITE\"
 *	    where MYJAHIASITE is the virtual site's sitekey in uppercase.
 *
 * <p>2.  Finally if none of the previous resource bundles are available,
 *	    Jahia will return the default resource bundle
 *	    named \"JahiaEnginesResources\".
 *
 * <p><b>Example :</b>
 *
 * <p>To request the value of the resource named \"stylesheet\", you only need to call the tag and provide the resource name:
 *
 * <p>&lt;content:engineResourceBundle resourceName=\"stylesheet\"/&gt;
 *
 * <p>In the same way, for a resource that is an image, we have something like that:
 *
 * <p>&lt;img src=\"&lt;jahia:serverHttpPath /&gt;
          &lt;jahia:engineResourceBundle resourceName=\"headerLogoImg\" /&gt;\"
           width=\"126\" height=\"63\"&gt;

 * <p>The generated html code is:
 *
 * <p>&lt;img src=�http://127.0.0.1:8080/jahia/jsp/jahia/engines/images/header.gif� width=\"126\" height=\"63\"&gt;
 *
 * <p>Note the Tag &lt;jahia:serverHttpPath /&gt; is needed to build the full image url.
 *
 * <p>This Tag returns the server request URI part of the URL: \"http://127.0.0.1:8080/jahia\".
 *
 * </attriInfo>"
 */
public class EngineResourceBundleTag extends TagSupport {

    private static final String CLASS_NAME = EngineResourceBundleTag.class.getName();

    public static final String JAHIA_ENGINE_TAG 				= "<jahiaEngine>";
    public static final String SITE_TEMPLATE_TAG 				= "<siteTemplate>";

    private String resourceName   = "";
    private String defaultValue   = "";

    /**
       * @jsp:attribute name="defaultValue" required="false" rtexprvalue="true"
       * description="the value to use if the resource couldn't be accessed.
       *
       * <p><attriInfo>such as if the resourceBundle doesn't exist, couldn't be read or the resource key entry
       * couldn't be found.
       * </attriInfo>"
     */
    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    /**
      * @jsp:attribute name="resourceName" required="true" rtexprvalue="true"
      * description="the key of the resource to fetch in the engine resource bundle.
      *
      * <p><attriInfo>The resourceName attribute is parsed for two special tags and are replaced by the appropriate value,
      *  such that:
      *
      * <p>&lt;jahiaEngine&gt; 	: by /jsp/jahia/engines/
      * <p>&lt;siteTemplate&lt;	: by /jsp/jahia/templates/&lt;mysite&gt;/
      *					          where &lt;mysite&gt; is the sitekey of the current site
      *

      * </attriInfo>"
     */
    public void setResourceName(String resourceName) {
        if ( resourceName == null )
            resourceName = "";
        this.resourceName = resourceName;
    }

    public void displayError(String message) {
        try {
            JspWriter out = pageContext.getOut();
            out.print( "<!--" + CLASS_NAME + ":" + message + "-->" );
        } catch (IOException ioe) {
            JahiaConsole.println(CLASS_NAME+"doStartTag", ioe.toString());
        }
    }

    public int doStartTag() {

        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

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
            if ( jData != null ){
                resValue = JahiaResourceBundle
                        .getEngineResource(  resourceName,
                                             jData.getProcessingContext(),
                                             jData.getProcessingContext().getLocale() );
            } else {
                // for any reason the jData wasn't loaded correctly
                ResourceBundle resBundle = JahiaResourceBundle
                         .getEngineDefaultResourceBundle(null,currentLocale);
                resValue = JahiaResourceBundle.getString(resBundle, resourceName, currentLocale);
            }
        } catch ( MissingResourceException mre ) {
            JahiaConsole.println(CLASS_NAME+"doStartTag", mre.toString());
        }

        if (resValue == null) {
            resValue = this.defaultValue;
        }

        try {
            JspWriter out = pageContext.getOut();
            if ( jData != null ){
                out.print( parseResourceValue(resValue,jData.getProcessingContext()) );
            } else {
                out.print(parseResourceValue(resValue,null));
            }
        } catch (IOException ioe) {
            JahiaConsole.println(CLASS_NAME+"doStartTag", ioe.toString());
        }

        return SKIP_BODY;

    }

    //--------------------------------------------------------------------------
    /**
     * parse the resource value and replace :
     * <jahiaEngine> 	: by /jsp/jahia/engines/
     * <siteTemplate>	: by /jsp/jahia/templates/<mysite>/
	 *                                       	  where <mysite> is the sitekey of the current site
     *
     * @param String val, the String to parse
     * @param ParamBean, the paramBean
     * @return String , the parsed value
     */
    public static String parseResourceValue(String val,ProcessingContext jParams){

        if ( (val == null) || (jParams == null) || (jParams.getSite()== null) )
            return val;
        val = JahiaTools.replacePattern(val,JAHIA_ENGINE_TAG,
                                                         "/jsp/jahia/engines/");

        val = JahiaTools.replacePattern(val,SITE_TEMPLATE_TAG,
                "/jsp/jahia/templates/" + jParams.getSite().getSiteKey() + "/");

        return val;

    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        resourceName   = "";
        return EVAL_PAGE;
    }


}
