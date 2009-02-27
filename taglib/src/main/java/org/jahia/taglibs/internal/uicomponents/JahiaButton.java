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

 package org.jahia.taglibs.internal.uicomponents;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.JahiaConsole;
import org.jahia.utils.JahiaTools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.MissingResourceException;

/**
 * Create a graphic rollover button defining by three parameters.
 *
 * All these following parameters are required ('JahiaTags.tld' file) :
 *
 * img :    The image button start name defined in the resource bundle properties
 files. For example if the button defined in the resource bundle file
 is 'sortOffButtonImg' and 'sortOnButtonIMg', the start name is 'sort'.
            This tag will retrieve automatically the end of string identifier.
 * href :   This is the anchor tag <A> "href" parameter.
 * alt :    Correspond to the <IMG> tag "alt" parameter.
 *
 *
 * @jsp:tag name="jahiaButton" body-content="empty"
 * description="Draw a graphic rollover button defined by three parameters.
 *
 * <p><attriInfo>NOTE: SAME CODE AS iconButtonTag.java ...
 *
 * <p><b>Example :</b>
 * <p>
 *
          &lt; content:jahiaButton img=\"cancel\"
               href=\"javascript:sendFormCancel();\"
               altBundle=\"engine\" altKey=\"org.jahia.altCloseWithoutSave.label\" /&gt;
 *
 * </attriInfo>"
 */
public class JahiaButton extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaButton.class);

   /**
     *
     * @jsp:attribute name="img" required="true" rtexprvalue="true"
     * description="the image button start name defined in the resource bundle properties files..
     * <p><attriInfo>For example if the button
     * defined in the resource bundle file is 'sortOffButtonImg' and 'sortOnButtonIMg', the start name is 'sort'. This tag will
     * retrieve automatically the end of string identifier.
     * </attriInfo>"
     */
    public void setImg (String img) {
        this.img = img;
    }

    /**
     *
     * @jsp:attribute name="href" required="true" rtexprvalue="true"
     * description="the anchor href parameter.
     * <p><attriInfo>This is the anchor tag &lt;A&gt; \"href\" parameter.
     * </attriInfo>"
     */
    public void setHref (String href) {
        this.href = href;
    }

    /**
     *
     * @jsp:attribute name="alt" required="false" rtexprvalue="true"
     * description="Defines a short description of the image.
     * <p><attriInfo>Correspond to the &lt;IMG&gt; tag \"alt\" parameter.
     * </attriInfo>"
     */
    public void setAlt (String alt) {
        this.alt = alt;
    }

    /**
     *
     * @jsp:attribute name="altKey" required="false" rtexprvalue="true"
     * description="the key in the resource bundle for the internationalized version of the 'alt' text.
     * <p><attriInfo>This key uniquely identifies the locale-specific alt text in the associated 'altBundle' bundle appropriate
     * for the current user's locale.
     * <br>Note that if altKey's value cannot be found, the value in the 'alt' attribute (if defined) is used instead. If
     * 'altKey' and 'alt' are both defined, then precedence is given to 'altKey'.
     * </attriInfo>"
     */
    public void setAltKey (String altKey) {
        this.altKey = altKey;
    }

    /**
     * @jsp:attribute name="altBundle" required="false" rtexprvalue="true"
     * description="the identifier of the resource bundle where altKey's value is looked up.
     * <p><attriInfo>If the 'altBundle' attribute isn't specified but 'altKey' is, then the default resource bundle
     * is used instead. <br>
     * Resource bundles contain key/value pairs for specific locales and are used for internalization.
     * Go <a href=http://java.sun.com/j2se/1.4.2/docs/api/java/util/ResourceBundle.html>here</a> for more details.
     * <p>Note that Jahia finds the .properties file associated to a given bundleKey by doing a lookup in resourcebundles_config.xml
     * located in \WEB-IN\etc\config. The resourcebundles_config.xml file is generated automatically during template deployment. For example,
     * given the the following resourcebundles_config.xml:
     * <p><br>&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot; ?&gt;
     * <br>&nbsp;&lt;registry&gt;
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;resource-bundle&gt;
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;key&gt;myjahiasite_CORPORATE_PORTAL_TEMPLATES&lt;/key&gt;
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;file&gt;jahiatemplates.Corporate_portal_templates&lt;/file&gt;
     * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/resource-bundle&gt;
     * <br>&lt;/registry&gt;</p>
     * <p>Then given a bundleKey of myjahiasite_CORPORATE_PORTAL_TEMPLATES, the key/value lookup will be in Corporate_portal_templates.propreties
     * (located in directory \WEB-INF\classes\jahiatemplates) for the default language; or for example in  Corporate_portal_templates_de.propreties
     * if the current user's locale is German.
     * </attriInfo>"
     */
    public void setAltBundle (String altBundle) {
        this.altBundle = altBundle;
    }

    /**
     *
     * @jsp:attribute name="onclick" required="false" rtexprvalue="true"
     * description="Defines the \"onclick\" tag &lt;A&gt; event.
     * <p><attriInfo>.
     * </attriInfo>"
     */
    public void setOnclick (String onclick) {
        this.onclick = onclick;
    }

    public int doStartTag () {

        // Recover 'jData'
        HttpServletRequest request = (HttpServletRequest) pageContext.
                                     getRequest();
        JahiaData jData = (JahiaData) request.getAttribute(
            "org.jahia.data.JahiaData");

        // Build the resource bundle image parameters
        String bImageOff = "";
        String bImageOn = "";
        try {
            bImageOff = parseResourceValue(
                JahiaResourceBundle.getEngineResource("org.jahia." + img +
                "Off.button",
                jData.getProcessingContext(),
                jData.getProcessingContext().getLocale()), jData.getProcessingContext());
            bImageOn = parseResourceValue(
                JahiaResourceBundle.getEngineResource("org.jahia." + img +
                "On.button",
                jData.getProcessingContext(),
                jData.getProcessingContext().getLocale()), jData.getProcessingContext());

        } catch (MissingResourceException mre) {
            logger.debug("Error while trying to retrieve resource for image :" +
                         img, mre);
        }

        // FIXME : Why does the "JahiaResourceBundle.getEngineResource" method
        // return a null String when resource is not found ????
        if (bImageOff == null) {
            bImageOff = "";
        }
        if (bImageOn == null) {
            bImageOn = "";
        }
        bImageOff = getServerHttpPath(jData.getProcessingContext()) + bImageOff;
        bImageOn = getServerHttpPath(jData.getProcessingContext()) + bImageOn;
        // Define a unique ID that identify the rollover
        String sImgID = "img" + String.valueOf(imgID++);

        // now let's resolve the alt text if resource bundle keys are being used.
        if (altKey != null) {
            alt = JahiaResourceBundle.getResource(altBundle, altKey,
                                                  jData.getProcessingContext().getLocale(),
                                                  jData.getProcessingContext());
        }

        // Produce the HTML code
        try {
            JspWriter out = pageContext.getOut();
            StringBuffer str = new StringBuffer("\n");
            if (debug) {
                str.append(
                    "<!-- ============================================================= -->\n");
                str.append(
                    "<!-- The following HTML code is generated by 'jahiaButton' taglib -->\n");
                str.append("<!-- Parameters : img = ");
                str.append(img);
                str.append("\n");
                str.append("                : href = ");
                str.append(href);
                str.append("\n");
                str.append("                : alt = ");
                str.append(alt);
                str.append(
                    "\n-->\n");
            }
            str.append("<a href=\"");
            str.append(href);
            str.append("\"\n");
            str.append("   onMouseOut=\"MM_swapImgRestore()\"\n");
            str.append("   onMouseOver=\"MM_swapImage('");
            str.append(sImgID);
            str.append("','','");
            str.append(bImageOn);
            str.append("',1)\"\n");
            if (onclick != null) {
                str.append("   onclick=\"");
                str.append(onclick);
                str.append("\"\n");
            }
            str.append("   ><img name=\"");
            str.append(sImgID);
            str.append("\" alt=\"");
            str.append(alt);
            str.append("\"\n");
            str.append("        src=\"");
            str.append(bImageOff);
            str.append("\" border=\"0\"></a>");
            // @todo : an optional focus parameter (yes/no) are to be implemented
            //      1. give a "name" the anchor <a>.
            //      2. use this name to implement the following focus script.
            // str.append(<script language="javascript">a62.focus();</script>);
            if (debug) {
                str.append("<!-- End 'jahiaButton' taglib ===== -->");
            }
            out.print(str.toString());
        } catch (IOException ioe) {
            JahiaConsole.println("JahiaButton.doStartTag", ioe.toString());
        }
        return SKIP_BODY;
    }

    /**
     * Build an http path containing the server name and port,
     * instead of the path from JahiaPrivateSettings.
     *
     * @return An http path leading to Jahia, built with the server name, and
     *         the server port if nonstandard.
     *
     * FIXME : This method duplicate the method already defined in ServerHttpPathTag
     */
    private String getServerHttpPath (ProcessingContext jParams) {
        return jParams.getContextPath();
    }

    public int doEndTag ()
        throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        img = "";
        href = "";
        alt = "";
        onclick = null;
        altKey = null;
        altBundle = null;
        return EVAL_PAGE;
    }


    /**
     * parse the resource value and replace :
     * <jahiaEngine> 	: by /engines/
     * <siteTemplate>	: by /templates/<mysite>/
	 *                                       	  where <mysite> is the sitekey of the current site
     *
     * @param val, the String to parse
     * @param jParams, the paramBean
     * @return String , the parsed value
     */
    protected String parseResourceValue(String val,ProcessingContext jParams){

        if ( (val == null) || (jParams == null) || (jParams.getSite()== null) )
            return val;
        val = JahiaTools.replacePattern(val,JAHIA_ENGINE_TAG,
                                                         "/engines/");

        val = JahiaTools.replacePattern(val,SITE_TEMPLATE_TAG,
                "/templates/" + jParams.getSite().getSiteKey() + "/");

        return val;

    }

    public static final String JAHIA_ENGINE_TAG 				= "<jahiaEngine>";
    public static final String SITE_TEMPLATE_TAG 				= "<siteTemplate>";

    // Taglib parameters
    private String img = "";
    private String href = "";
    private String alt = "";
    private String onclick = null;

    private String altKey = null;
    private String altBundle = null;

    // Internal members
    private static long imgID = 0;
    private boolean debug = false;

}
