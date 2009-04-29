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
 package org.jahia.clipbuilder.sql.struts;

import org.apache.struts.actions.LookupDispatchAction;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.context.ApplicationContext;
import org.jahia.clipbuilder.sql.database.hibernate.service.DefaultConfiguationManager;

/**
 *  Base Action for sqlBuilder
 *
 *@author    ktlili
 */
public abstract class BaseAction extends LookupDispatchAction {
    /**
     *  Gets the DefaultConfigurationManager attribute of the BaseAction object
     *
     *@return    The DefaultConfigurationManager value
     */
    public DefaultConfiguationManager getDefaultConfigurationManager() {
        ApplicationContext cxt = org.jahia.clipbuilder.util.JahiaUtils.getSpringApplicationContext();
        if (cxt == null) {
                    // clip builder is not part of jahia
            cxt = WebApplicationContextUtils.getRequiredWebApplicationContext(getServlet().getServletContext());
        }
        return (DefaultConfiguationManager) cxt.getBean("sqlClipperDefaultConfigurationManager");
    }

}
