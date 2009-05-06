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
package org.jahia.taglibs.template.theme;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.SiteBean;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.operations.valves.ThemeValve;
import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.engines.shared.JahiaPageEngineTempBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map;

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
                    jahiaThemeCurrent = jParams.getUser().getProperty(ThemeValve.THEME_ATTRIBUTE_NAME + "_" + theSite.getID());
                }
                if (pageContext.getSession().getAttribute(ThemeValve.THEME_ATTRIBUTE_NAME) != null) {
                    jahiaThemeCurrent = (String) pageContext.getSession().getAttribute(ThemeValve.THEME_ATTRIBUTE_NAME + "_" + theSite.getID());
                }
            } else if (scope.equals("page")) {
                Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
                if (engineMap != null) {
                    JahiaPageEngineTempBean tempBean = (JahiaPageEngineTempBean) engineMap.get("pageTempBean");
                    if (tempBean != null) {
                        jahiaThemeCurrent = tempBean.getTheme();
                    }
                }
                if (jahiaThemeCurrent == null) {
                    jahiaThemeCurrent = (String) jParams.getPage().getProperty(ThemeValve.THEME_ATTRIBUTE_NAME);
                }

            } else {
                return SKIP_BODY;
            }

            final StringBuffer buff = new StringBuffer();
            SiteBean siteBean = new SiteBean(theSite, jParams);
            PageBean pageBean = new PageBean(jParams.getPage(), jParams);
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
            boolean isInEngine = jParams.getEngineName()!=null;
            try {
                if (jParams.isInAdminMode()) {
                    pageUrl = request.getAttribute("requestURI") + "?do=themes&sub=display";
                    displayInput = "display";
                    doInput = "themes";
                } else {
                    pageUrl = pageBean.getUrl();
                }
            } catch (JahiaSessionExpirationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if(!isInEngine) {
            buff.append("<form name=\"jahiathemeSelectorForm").append(scope).append("\" method=\"get\" action=\"");
            buff.append(pageUrl);
            buff.append("\">");
            }
            buff.append("<select name=\"jahiaThemeSelector\"");
            if(!isInEngine)
            buff.append(" onchange=\"document.jahiathemeSelectorForm").append(scope).append(".submit()\"");
            buff.append(">");

            if (scope.equals("user") || scope.equals("page")) {
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
                logger.error(e);
            }
            if(!isInEngine)
            buff.append("</form>");

            out.print(buff.toString());
        } catch (IOException e) {
            logger.error("IOException rendering the menu", e);
        } catch (JahiaException e) {
            logger.error(e);
        }
        return SKIP_BODY;
    }


    @Override
    public int doEndTag() throws JspException {
        scope = "";
        return super.doEndTag();
    }
}
