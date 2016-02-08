/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.osgi;

import org.apache.karaf.main.Main;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

import java.io.File;

/**
 * OSGi framework service
 *
 * @author Serge Huber
 */
public class FrameworkService {
    
    private static final Logger logger = LoggerFactory.getLogger(FrameworkService.class);

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final FrameworkService INSTANCE = new FrameworkService(JahiaContextLoaderListener.getServletContext());

        private Holder() {
        }
    }

    /**
     * Returns a singleton instance of this class.
     * 
     * @return a singleton instance of this class
     */
    public static FrameworkService getInstance() {
        return Holder.INSTANCE;
    }

    private final ServletContext servletContext;
    
    private Main main;

    private boolean started;

    private FrameworkService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    private void startKaraf() {
        try {
            String karafHome = new File(servletContext.getRealPath("/WEB-INF/karaf")).getAbsolutePath();
            String varDiskPath = SettingsBean.getInstance().getJahiaVarDiskPath();
            System.setProperty("karaf.home", karafHome);
            System.setProperty("karaf.base", karafHome);
            System.setProperty("karaf.data", varDiskPath + "/karaf");
            System.setProperty("karaf.etc", varDiskPath + "/etc");
            System.setProperty("org.osgi.framework.storage", varDiskPath + "/bundles-deployed");
            System.setProperty("karaf.history", varDiskPath + "/karaf/history.txt");
            System.setProperty("karaf.instances", varDiskPath + "/karaf/instances");
            System.setProperty("karaf.startLocalConsole", "false");
            System.setProperty("karaf.startRemoteShell", "true");
            System.setProperty("karaf.lock", "false");
            System.setProperty("log4j.ignoreTCL", "true");
            System.setProperty("jahiaVarDiskPath", varDiskPath);
            main = new Main(new String[0]);
            main.launch();
        } catch (Exception e) {
            main = null;
            logger.error("Error starting Karaf container", e);
            throw new JahiaRuntimeException("Error starting Karaf container", e);
        }

    }

    public void start() throws BundleException {
        startKaraf();
        servletContext.setAttribute(BundleContext.class.getName(), main.getFramework().getBundleContext());
        started = true;
    }

    /**
     * Shuts the OSGi container down.
     * 
     * @throws BundleException in case of an error
     */
    public void stop() throws BundleException {
        if (this.main != null) {
            servletContext.removeAttribute(BundleContext.class.getName());
            try {
                main.destroy();
            } catch (Exception e) {
                logger.error("Error shutting down Karaf framework", e);
            }
        }
        logger.info("OSGi framework stopped");
    }

    /**
     * Returns <code>true</code> if the OSGi container is completely started.
     * 
     * @return <code>true</code> if the OSGi container is completely started; <code>false</code> otherwise
     */
    public boolean isStarted() {
        return started;
    }
    
    /**
     * Returns bundle context.
     * 
     * @return bundle context or <code>null</code> in case the OSGi container has not been started yet
     */
    public static BundleContext getBundleContext() {
        final FrameworkService instance = getInstance();
        if (instance != null && instance.main != null) {
            return instance.main.getFramework().getBundleContext();
        } else {
            return null;
        }
    }
    
    /**
     * Notify this service that the container has actually started.
     */
    public static void notifyStarted() {
        logger.info("Got started event");
        final FrameworkService instance = getInstance();
        synchronized (instance) {
            logger.info("Started event arrived");
            instance.started = true;
            instance.notifyAll();
            logger.info("Notified all about OSGi container started event");
        }
    }
}
