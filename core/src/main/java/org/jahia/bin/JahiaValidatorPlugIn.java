/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
