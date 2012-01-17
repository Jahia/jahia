/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.internal.uicomponents;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.services.render.RenderContext;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * <p>The Jahia Shared Modification is: Jahia Tag Libs</p>
 *
 * <p>Description:
 * Write a non localized HTML image tag defined in a common resource bundle
 * file.<br>
 *
 * Synopsis : < content:displayIcon src="resourceBundleKey" [alt="< text>"]
 *                      [resource="< relative path to an image>"]<br>
 *
 * src : resource bundle key.<br>
 * alt : Correspond to the < IMG> tag "alt" parameter.<br>
 * resource : Alternative resource defined by the relative path from the
 * JSP source file to an image file.</p>
 *
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 * @author MAP
 * @version 1.0
 *
 *
 * @jsp:tag name="displayIcon" body-content="empty"
 * description="Displays a non localized HTML image tag (i.e. &lt;IMG&gt;) defined in a common resource bundle
 * file.
 * <p><attriInfo>
 * <p><b>Example :</b>
 * <p> &lt;content:displayIcon src=\"hugh\" alt=\"Ardon\" align=\"absmiddle\" width=\"14\" height=\"10\"
 *     altBundle=\"mybundle\" altKey=\"com.acme.hugh.label\" /&gt;
 * </attriInfo>"
 */
@SuppressWarnings("serial")
public class DisplayIconTag extends AbstractJahiaTag {
    public DisplayIconTag () {
        try {
            jbInit();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * @param id the html tag id.
     *
     * @jsp:attribute name="id" required="false" rtexprvalue="true"
     * description="the html tag id".
     * <p><attriInfo>the html tag id.
     * </attriInfo>"
     */

    public void setId (String id) {
        _id = id;
    }

    /**
     * @param src The resource bundle key. Required but not take if 'resource'
     * is set.
     *
     * @jsp:attribute name="src" required="true" rtexprvalue="true"
     * description="the resource bundle key.
     * <p><attriInfo>Required but not used if 'resource' is set.
     * </attriInfo>"
     */
   public void setSrc (String src) {
        _src = src;
    }

    /**
     * @param resource The path to a resource image file.
     *
     * @jsp:attribute name="resource" required="false" rtexprvalue="true"
     * description="the path to a resource image file.
     * <p><attriInfo>The 'resource' path is defined by the
     * relative path from the JSP source file to an image file.
     * </attriInfo>"
     */

    public void setResource (String resource) {
        _resource = resource;
    }

    /**
     * @param height The image height
     *
     *
     * @jsp:attribute name="resource" required="false" rtexprvalue="true"
     * description="the image height.
     * <p><attriInfo>in pixels or %.
     * </attriInfo>"
     */
    public void setHeight (Integer height) {
        _height = height;
    }

    /**
     * @param width The image width
     *
     * @jsp:attribute name="width" required="false" rtexprvalue="true"
     * description="the image width.
     * <p><attriInfo>in pixels or %.
     * </attriInfo>"
     */
    public void setWidth (Integer width) {
        _width = width;
    }

    /**
     * @param alt The alt parameter from the flag image.
     *
     * @jsp:attribute name="alt" required="false" rtexprvalue="true"
     * description="Defines a short description of the image.
     * <p><attriInfo>Correspond to the &lt;IMG&gt; tag \"alt\" parameter.
     * </attriInfo>"
     */
    public void setAlt (String alt) {
        _alt = alt;
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
     * @param align The align parameter from the image tag.
     *
     *
     * @jsp:attribute name="align" required="false" rtexprvalue="true"
     * description="the align parameter from the image tag.
     * <p><attriInfo>Specifies how to align the image according to surrounding text e.g.
     * <p>align = left|center|right|justify
     * </attriInfo>"
     */
    public void setAlign (String align) {
        _align = align;
    }

    public void setLang(String lang) {
        this._lang = lang;
    }

    public int doStartTag () {

        // Recover 'jData'
        // @todo FIXME : This code is repeated in a lot of button taglibs
        HttpServletRequest request = (HttpServletRequest) pageContext.
                                     getRequest();
        /*
        JahiaData jData = (JahiaData) request.getAttribute(
            "org.jahia.data.JahiaData");
            */
        // final ProcessingContext jParams = jData.getProcessingContext();
        RenderContext renderContext = (RenderContext) pageContext.findAttribute("renderContext");

		final String contextPath = request.getContextPath();

        // now let's resolve the alt text if resource bundle keys are being
		// used.
        if (altKey != null) {
            _alt = JahiaResourceBundle.getString(altBundle, altKey, renderContext.getMainResourceLocale(), renderContext.getSite().getTemplatePackageName());
        }

        // Produce the HTML code
        try {
            JspWriter out = pageContext.getOut();
            StringBuffer str = new StringBuffer("");
            
            // Resolve file name
            final String imagePath;
    		boolean wfState = false;
    		if (_src.length() <= 2) {
				try {
					final int state = Integer.parseInt(_src);
					if (state >= -1 && state <= 3) {
						wfState = true;
					}
				} catch (Exception e) {
					// Ignore
				}
			}

            /*
            if (wfState) {
    			imagePath = new StringBuffer(64).append(contextPath).append(
    					"/ajaxaction/GetWorkflowState?params=/op/edit/pid/").append(
    					jParams.getPageID()).append("&key=").append(
    					jParams.getContentPage().getObjectKey()).append(
    					(_lang != null ? "&flaglang=" + _lang : "")).toString();
    		} else {
    		*/
    			imagePath = JahiaResourceBundle.getString(getResourceBundle(),_src,renderContext.getMainResourceLocale(),renderContext.getSite().getTemplatePackageName());
            /*
    		}
    		*/
            if ( ("".equals(_resource)) && (imagePath == null)) {
                str.append("<!-- couldn't find resource with key " + _src +
                           " -->");
            } else {
                // Write image HTML tag
                str.append("<img ");
                if (_id != null && !"".equals(_id.trim())) {
                    str.append(" id=\"");
                    str.append(_id);
                    str.append("\"");
                }
                str.append(" alt=\"");
                str.append(_alt);
                str.append("\" border=\"0\" src=\"");
                if ("".equals(_resource)) {
                    str.append(imagePath);
                } else {
                    str.append(_resource);
                }
                str.append("\"");
                if (_height != null) {
                    str.append(" height=\"");
                    str.append(_height.intValue());
                    str.append("\"");
                }
                if (_width != null) {
                    str.append(" width=\"");
                    str.append(_width.intValue());
                    str.append("\"");
                }
                if (_align != null) {
                    str.append(" align=\"");
                    str.append(_align);
                    str.append("\"");
                }
                str.append(">");
            }
            out.print(str.toString());
        } catch (IOException ioe) {
            logger.debug(ioe.toString());
        }
        return SKIP_BODY;
    }

    public int doEndTag ()
        throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        _src = "";
        _resource = "";
        _alt = "";
        altKey = null;
        altBundle = null;
        _height = null;
        _width = null;
        _align = "";
        _id = "";
        _lang = "";
        return EVAL_PAGE;
    }

    // Taglib parameters
    private String _src = "";
    private String _resource = "";
    private String _alt = "";
    private String altKey = null;
    private String altBundle = null;
    private Integer _height = null;
    private Integer _width = null;
    private String _align = "";
    private String _id = "";
    private String _lang = "";

    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(DisplayIconTag.class);
    private void jbInit ()
        throws Exception {
    }

}
