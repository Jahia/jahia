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
package org.jahia.params;

import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;

import javax.servlet.ServletContext;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 31, 2003
 * Time: 5:26:33 PM
 * To change this template use Options | File Templates.
 */
public class SoapParamBean extends ParamBean {

    private JahiaPage page;

    public SoapParamBean(JahiaSite site, JahiaUser user) throws JahiaException {
        super(null, null, Jahia.getStaticServletConfig().getServletContext(), System.currentTimeMillis(),
                ProcessingContext.POST_METHOD, site, user);
    }

    public SoapParamBean(ServletContext servletContext,
                         SettingsBean jSettings,
                         long startTime,
                         JahiaSite site,
                         JahiaUser user)

            throws JahiaException {

        super(null, null, servletContext, startTime, ProcessingContext.POST_METHOD, site, user);

    }

    public void setUserGuest() throws JahiaException {
        // we won't change initial user
    }

    public JahiaPage getPage() {
        return page;
    }

    public void setPage(JahiaPage page) {
        this.page = page;
    }

}
