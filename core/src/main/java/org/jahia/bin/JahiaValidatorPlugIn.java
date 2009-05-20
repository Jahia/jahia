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

package org.jahia.bin;

import java.net.MalformedURLException;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.validator.ValidatorPlugIn;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;

/**
 * Extends validator resource lookup mechanism to consider template set specific
 * validation.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaValidatorPlugIn extends ValidatorPlugIn {

    private static Logger logger = Logger.getLogger(JahiaValidatorPlugIn.class);

    @Override
    public void init(ActionServlet servlet, ModuleConfig config)
            throws ServletException {

        if (SpringContextSingleton.getInstance().isInitialized()) {
            List<JahiaTemplatesPackage> templateSets = ServicesRegistry
                    .getInstance().getJahiaTemplateManagerService()
                    .getAvailableTemplatePackages();
            StringBuilder paths = new StringBuilder(64);
            for (JahiaTemplatesPackage templatePackage : templateSets) {
                String configPath = templatePackage.getRootFolderPath()
                        + "/validation.xml";
                try {
                    if (servlet.getServletContext().getResource(configPath) != null) {
                        paths.append(",").append(configPath);
                    }
                } catch (MalformedURLException e) {
                    // ignore it
                }
            }
            if (paths.length() > 0) {
                if (getPathnames() != null) {
                    paths.insert(0, getPathnames());
                } else {
                    paths.delete(0, 1);
                }
                setPathnames(paths.toString());
            }
        } else {
            logger
                    .error("Spring-based services are not initialized yet."
                            + " Unable to lookup template set specific validator resources.");
        }
        super.init(servlet, config);
    }

}
