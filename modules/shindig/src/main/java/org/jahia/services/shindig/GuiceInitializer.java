/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.shindig;

import static org.apache.shindig.common.servlet.GuiceServletContextListener.INJECTOR_ATTRIBUTE;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * Initializer for Guice injector.
 * 
 * @author Sergiy Shyrkov
 */
public class GuiceInitializer implements ServletContextAware, InitializingBean, DisposableBean {

    private List<Module> modules;

    private ServletContext servletContext;
    
    private Map<String, String> systemProperties; 

    public void afterPropertiesSet() throws Exception {
        if (systemProperties != null && !systemProperties.isEmpty()) {
            for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
        Injector injector = Guice.createInjector(Stage.PRODUCTION, modules);
        servletContext.setAttribute(INJECTOR_ATTRIBUTE, injector);
    }

    public void destroy() throws Exception {
        servletContext.removeAttribute(INJECTOR_ATTRIBUTE);
    }

    /**
     * Sets the list of configured modules.
     * 
     * @param modules the list of configured modules
     */
    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

}
