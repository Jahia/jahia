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

package org.jahia.services.templates;

import javax.servlet.ServletContext;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.SpringContextSingleton;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.File;
import java.util.List;

/**
 * Loader and manager of the Spring's application context for Jahia modules.
 * 
 * @author Sergiy Shyrkov
 */
public class TemplatePackageApplicationContextLoader implements ServletContextAware {

    private static final Logger logger = LoggerFactory.getLogger(TemplatePackageApplicationContextLoader.class);

    private ServletContext servletContext;

    private String contextConfigLocation;

    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void start() {
        logger.info("Initializing Spring application context for Jahia modules");
        long startTime = System.currentTimeMillis();


        List<JahiaTemplatesPackage> packages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages();
        for (JahiaTemplatesPackage aPackage : packages) {
            try {
                createWebApplicationContext(aPackage);
            } catch (BeansException e) {
                logger.error("Cannot instantiate context for module "+aPackage.getRootFolder(),e);
            }
        }
        
        logger.info("Jahia modules application context initialization completed in {} ms",
                System.currentTimeMillis() - startTime);
    }

    public void stop() {
        List<JahiaTemplatesPackage> packages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages();
        for (JahiaTemplatesPackage aPackage : packages) {
            if (aPackage.getContext() != null) {
                try {
                    aPackage.getContext().close();
                } catch (Exception e) {
                    logger.error("Error shutting down Jahia modules Spring application context", e);
                }
            }
        }
    }

// -------------------------- OTHER METHODS --------------------------

    public void reload(JahiaTemplatesPackage aPackage) {
        logger.info("Reloading Spring application context for module {}", aPackage.getName());
        long startTime = System.currentTimeMillis();

        if (aPackage.getContext() != null) {
            aPackage.getContext().refresh();
        }

        aPackage.getContext().publishEvent(new ContextInitializedEvent(this, aPackage));
        
        logger.info("...done reloading context for module {} in {} ms", aPackage.getName(),
                System.currentTimeMillis() - startTime);
    }

    public void createWebApplicationContext(JahiaTemplatesPackage aPackage) throws BeansException {
        if (new File(servletContext.getRealPath("modules/" + aPackage.getRootFolderWithVersion() + "/META-INF/spring/")).exists()) {
            logger.debug("Start initializing context for module {}", aPackage.getName());
            long startTime = System.currentTimeMillis();

            String configLocation = contextConfigLocation + ",modules/" + aPackage.getRootFolderWithVersion() + "/META-INF/spring/*.xml";
            XmlWebApplicationContext ctx = new XmlWebApplicationContext();
            ctx.setParent(SpringContextSingleton.getInstance().getContext());
            ctx.setServletContext(servletContext);
            servletContext.setAttribute(WebApplicationContext.class.getName() + ".jahiaModule." + aPackage.getRootFolder(), ctx);
            ctx.setConfigLocation(configLocation);
            aPackage.setContext(ctx);
            ctx.refresh();
            
            logger.info(
                    "\"{}\" [{}] context initialized in {} ms",
                    new Object[] { aPackage.getName(), aPackage.getRootFolder(),
                            System.currentTimeMillis() - startTime });
        }
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * This event is fired when modules application context is initialized.
     * 
     * @author Sergiy Shyrkov
     */
    public static class ContextInitializedEvent extends ApplicationEvent {
        private static final long serialVersionUID = -2367558261328740803L;
        private JahiaTemplatesPackage aPackage;

        public ContextInitializedEvent(Object source, JahiaTemplatesPackage aPackage) {
            super(source);
            this.aPackage = aPackage;
        }

        public JahiaTemplatesPackage getPackage() {
            return aPackage;
        }

        public XmlWebApplicationContext getContext() {
            return aPackage.getContext();
        }
    }
}