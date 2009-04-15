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

package org.jahia.taglibs.template.theme;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.SiteBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.operations.valves.ThemeValve;
import org.jahia.exceptions.JahiaSessionExpirationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author David Griffon
 *         Date: 25 sept. 2008
 *         Time: 12:55:28
 *         To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ThemeSelectorTag extends AbstractJahiaTag {
    private static transient final Category logger = Logger.getLogger(ThemeSelectorTag.class);
    private String scope = "";

    public void setScope(String scope) {
        if (scope != null && !scope.trim().equals("")) {
            this.scope = scope;
        }
    }

    public String getScope() {
        return this.scope;
    }

    public int doStartTag() throws JspException {
        try {
            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final ProcessingContext jParams = jData.getProcessingContext();
            final JahiaSite theSite = jParams.getSite();

            String jahiaThemeCurrent = (String) pageContext.getSession().getAttribute(ThemeValve.THEME_ATTRIBUTE_NAME + "_" + theSite.getID());

            if (scope.equals("site") && jParams.getUser().isAdminMember(theSite.getID())) {
                jahiaThemeCurrent = theSite.getSettings().getProperty(ThemeValve.THEME_ATTRIBUTE_NAME);
            } else if (scope.equals("user")) {
                if (jData.gui().isLogged()) {
                    jahiaThemeCurrent = jParams.getUser().getProperty(ThemeValve.THEME_ATTRIBUTE_NAME +"_"+theSite.getID());
                }
                if (pageContext.getSession().getAttribute(ThemeValve.THEME_ATTRIBUTE_NAME)!= null) {
                    jahiaThemeCurrent = (String) pageContext.getSession().getAttribute(ThemeValve.THEME_ATTRIBUTE_NAME +"_"+theSite.getID());
                }
            } else {
                return SKIP_BODY;
            }

                final StringBuffer buff = new StringBuffer();
                SiteBean siteBean = new SiteBean(theSite, jParams);
                PageBean pageBean = new PageBean(jParams.getPage(),jParams);
                JahiaTemplatesPackage pkg = siteBean.getTemplatePackage();

                SortedSet<String> themes = new TreeSet<String>();
                for (Object o : pkg.getLookupPath()) {
                    String rootFolderPath = (String) o;
                    File f = new File(Jahia.getStaticServletConfig().getServletContext().getRealPath(rootFolderPath + "/theme"));
                    if (f.exists()) {
                        themes.addAll(Arrays.asList(f.list()));
                    }
                }


                JspWriter out = pageContext.getOut();
                String pageUrl = "";
                String doInput = "";
                String displayInput = "";
            try {
                if (jParams.isInAdminMode()) {
                    pageUrl = request.getAttribute("requestURI")+"?do=themes&sub=display";
                    displayInput = "display";
                    doInput = "themes";
                } else {
                    pageUrl = pageBean.getUrl();
                }
            } catch (JahiaSessionExpirationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            buff.append("<form name=\"jahiathemeSelectorForm"+scope+"\" method=\"get\" action=\"");
                buff.append(pageUrl);
                buff.append("\">");
                buff.append("<select name=\"");
                buff.append("jahiaThemeSelector");
                buff.append("\" onchange=\"document.jahiathemeSelectorForm"+scope+".submit()\">");

            if (scope.equals("user")) {
                buff.append("<option ");
                buff.append("value=\"");
                buff.append("\">");
                buff.append("------");
                buff.append("</option>");
            }

                for (Object theme1 : themes) {
                    String theme = (String) theme1;
                    buff.append("<option ");
                    buff.append("value=\"");
                    buff.append(theme);
                    buff.append("\"");
                    if (theme.equals(jahiaThemeCurrent)) {
                        buff.append(" selected=\"selected\"");
                    }
                    buff.append(">");
                    buff.append(getMessage(theme, "theme." + theme));
                    buff.append("</option>");
                }
                buff.append("</select>");
                buff.append("<input type=\"hidden\" name=\"jahiathemeSelectorScope\" value=\"");
                buff.append(this.scope);
                buff.append("\">");
            try {
                if (jParams.isInAdminMode()) {
                    buff.append("<input type=\"hidden\" name=\"do\" value=\"");
                    buff.append(doInput);
                    buff.append("\">");
                    buff.append("<input type=\"hidden\" name=\"sub\" value=\"");
                    buff.append(displayInput);
                    buff.append("\">");
                }
            } catch (JahiaSessionExpirationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            buff.append("</form>");

                out.print(buff.toString());
        } catch (
                IOException e) {
            logger.error("IOException rendering the menu", e);
        }
        return SKIP_BODY;
    }


    @Override
    public int doEndTag() throws JspException {
        scope = "";
        return super.doEndTag();
    }
}
