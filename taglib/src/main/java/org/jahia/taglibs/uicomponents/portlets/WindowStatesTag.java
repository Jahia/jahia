/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.uicomponents.portlets;

import org.jahia.services.render.RenderContext;
import org.jahia.settings.SettingsBean;
import org.jahia.taglibs.utility.Utils;
import org.jahia.utils.i18n.ResourceBundles;
import org.jahia.api.Constants;
import org.jahia.data.beans.portlets.PortletWindowBean;
import org.jahia.data.beans.portlets.WindowStateBean;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * <p>Title: Renders list of window states.</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 *
 * @jsp:tag name="windowStates" body-content="empty"
 * description="Displays the GUI interface for window state changes, as well
          as the currently selected state.
 *
 * <p><attriInfo>Use this tag to display the associated portlet menu to enable various operations such as Maximizing,
 * Minimizing or Closing a portlet (supported operations are dependant on each the portlet). It will also highlight the icon/label
 * which represents the current selected state.
 *
 * <p>This tag is typically used in conjunction with <a href='PortletModes.html' target='tagFrame'>jahiaHtml:PortletModes</a>.
 *
 * <p><b>Example :</b> Displays all the webapps in webappsContainerList along with their associated menus.
 *
 * <p>
 *
&lt;content:containerList name='&lt;%=\"webappsContainer\" + id%&gt;' id=\"webappsContainerList\"  parentContainerName=\"boxContainer\"&gt; <br>
 &nbsp;&nbsp;    &lt;content:container&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;content:applicationField name='&lt;%=\"webapp\" + id%&gt;' id=\"webapp\" display=\"false\" /&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;bean:define id=\"portletWindowBean\" name=\"webapp\" property=\"object\" /&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;jahia-htmlwindowStates name=\"portletWindowBean\" /&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;jahia-htmlportletModes name=\"portletWindowBean\" /&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;br/&gt; <br>
 &lt;bean:write name=\"webapp\" property=\"value\" filter=\"false\" /&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;content:updateContainerURL id=\"updateWebappsContainerURL\" display=\"false\"/&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;content:deleteContainerURL id=\"deleteWebappsContainerURL\" display=\"false\"/&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;logic:present name=\"updateWebappsContainerURL\"&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;           &lt;a href=\"&lt;bean:write name='updateWebappsContainerURL'/&gt;\"&gt;&lt;%=updateButton%&gt;&lt;/a&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;           &lt;a href=\"&lt;bean:write name='deleteWebappsContainerURL'/&gt;\"&gt;&lt;%=deleteButton%&gt;&lt;/a&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;/logic:present&gt; <br>
 &nbsp;&nbsp;   &lt;/content:container&gt; <br>
 &nbsp;&nbsp;   &lt;logic:equal name=\"webappsContainerList\" property=\"size\" value=\"0\"&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;content:addContainerURL id=\"addWebappsContainerURL\" display=\"false\"/&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;logic:present name=\"addWebappsContainerURL\"&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;          &lt;br/&gt;&lt;a href=\"&lt;bean:write name='addWebappsContainerURL'/&gt;\"&gt;&lt;%=addButton%&gt;&nbsp;&lt;jahia:resourceBundle <br>
resourceBundle=\"jahiatemplates.Corporate_portal_templates\" <br>
 &nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;              resourceName=\"addWebapps\"/&gt;&lt;/a&gt; <br>
 &nbsp;&nbsp;&nbsp;&nbsp;       &lt;/logic:present&gt; <br>
 &nbsp;&nbsp;   &lt;/logic:equal&gt; <br>
 &lt;/content:containerList&gt; <br>

 *
 *<p>Introduced as from Jahia version 4.5
 *
 * </attriInfo>"
 */

@SuppressWarnings("serial")
public class WindowStatesTag extends TagSupport {

    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(WindowStatesTag.class);

    private String namePostFix = "";
    private String name = null;
    private String resourceBundle = ResourceBundles.JAHIA_INTERNAL_RESOURCES;
    private String listCSSClass = "windowStates";
    private String currentCSSClass = "current";

    public WindowStatesTag () {
    }

    /**
     * @return name of the pageContext attribute holding the {@link PortletWindowBean}
     * @jsp:attribute name="name" required="true" rtexprvalue="true"
     * description="name of the pageContext attribute holding the PortletWindowBean
     *
     * <p><attriInfo>
     * </attriInfo>"
     */
    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    /**
     * @return String to append to portlet name's resource bundle key
     * @jsp:attribute name="namePostFix" required="false" rtexprvalue="true"
     * description="String to append to portlet name's resource bundle key
     *
     * <p><attriInfo>This corresponds to the following code in HTMLToolbox.drawWindowStateList() :
     * <br>getResource(resourceBundle,\"org.jahia.taglibs.html.portlets.windowstates.\" + curWindowStateBean.getName()
     * + \".label\" + namePostFix)
     * <p>Default is \"\".
     * </attriInfo>"
     */
    public String getNamePostFix () {
        return namePostFix;
    }

    public void setNamePostFix (String namePostFix) {
        this.namePostFix = namePostFix;
    }

    /**
     * @return resource bundle to use to find portlet status labels
     * @jsp:attribute name="resourceBundle" required="false" rtexprvalue="true"
     * description="resource bundle to use to find portlet status labels.
     *
     * <p><attriInfo>Default is 'JahiaInternalResources'.
     * </attriInfo>"
     */
    public String getResourceBundle () {
        return resourceBundle;
    }

    public void setResourceBundle (String resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * @return CSS class to use to display portlet status labels
     * @jsp:attribute name="listCSSClass" required="false" rtexprvalue="true"
     * description="CSS class to use to display portlet status labels.
     *
     * <p><attriInfo>Default is 'windowStates'.
     * </attriInfo>"
     */
    public String getListCSSClass () {
        return listCSSClass;
    }

    public void setListCSSClass (String listCSSClass) {
        this.listCSSClass = listCSSClass;
    }

    /**
     * @return CSS class to use to display the currently selected portlet status label
     * @jsp:attribute name="currentCSSClass" required="false" rtexprvalue="true"
     * description="CSS class to use to display the currently selected portlet status label.
     *
     * <p><attriInfo>Default is 'current'.
     * </attriInfo>"
     */

    public String getCurrentCSSClass () {
        return currentCSSClass;
    }

    public void setCurrentCSSClass (String currentCSSClass) {
        this.currentCSSClass = currentCSSClass;
    }

    @Override
    public int doStartTag () {

        PortletWindowBean portletWindowBean = (PortletWindowBean) pageContext.
                                              findAttribute(name);
        if (portletWindowBean == null) {
            logger.error("Couldn't find any PortletWindowBean with name " +
                         name);
            return SKIP_BODY;
        }

        JspWriter out = pageContext.getOut();
        try {

            drawWindowStateList(portletWindowBean, namePostFix,
                                            resourceBundle, listCSSClass,
                                            currentCSSClass, out);

        } catch (IOException ioe) {
            logger.error(
                "IO exception while trying to display action menu for object " +
                name, ioe);
        }

        return SKIP_BODY;
    }

    public int doEndTag ()
        throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        namePostFix = "";
        name = null;
        resourceBundle = ResourceBundles.JAHIA_INTERNAL_RESOURCES;
        listCSSClass = "windowStates";
        currentCSSClass = "current";
        return EVAL_PAGE;
    }


	private void drawWindowStateList(final PortletWindowBean portletWindowBean,
	        final String namePostFix, final String resourceBundle, final String listCSSClass,
	        final String currentCSSClass, final JspWriter out) throws IOException {

		out.print("<ul class=\"");
		out.print(listCSSClass);
		out.print("\">\n");
		for (WindowStateBean curWindowStateBean : portletWindowBean.getWindowStateBeans()) {
			if (curWindowStateBean.getName().equals(
			        portletWindowBean.getCurrentWindowStateBean().getName())) {
				out.print("<li class=\"");
				out.print(currentCSSClass);
				out.print("\">\n");
			} else {
				out.print("<li>");
			}
			final StringBuilder buff = new StringBuilder();
			buff.append("<a class=\"")
			        .append(curWindowStateBean.getName())
			        .append("\" title=\"")
			        .append(curWindowStateBean.getName())
			        .append("\"")
			        .append("\" href=\"")
			        .append(curWindowStateBean.getURL())
			        .append("\">")
			        .append("<span>")
			        .append(getResource(
			                resourceBundle,
			                "org.jahia.taglibs.html.portlets.windowstates."
			                        + curWindowStateBean.getName() + ".label" + namePostFix))
			        .append("</span></a>");
			out.print(buff.toString());
			out.println("</li>");
		}
		out.println("</ul>");
	}

	private String getResource(final String resourceBundle, final String resourceName) {
		ResourceBundle res;
		String resValue = null;

		final Locale locale = getLocale();
		try {
			res = ResourceBundle.getBundle(resourceBundle, locale);
			resValue = res.getString(resourceName);
		} catch (MissingResourceException mre) {
			logger.warn("Error accessing resource " + resourceName + " in bundle " + resourceBundle
			        + " for locale " + locale + ":" + mre.getMessage());
		}
		return resValue;
	}

    private Locale getLocale() {
        Locale locale = null;
        RenderContext ctx = Utils.getRenderContext(pageContext);
        if (ctx != null) {
            locale = ctx.getMainResourceLocale();
        }

        if (locale == null) {
            locale = (Locale) pageContext.getAttribute(Constants.SESSION_LOCALE,
                    PageContext.SESSION_SCOPE);
        }

        return locale != null ? locale : SettingsBean.getInstance().getDefaultLocale();
    }
}
