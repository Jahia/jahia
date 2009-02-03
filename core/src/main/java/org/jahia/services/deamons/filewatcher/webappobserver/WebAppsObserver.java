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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//
//
//  WebAppsObserver
//
//  NK      23.04.2001
//
//


package org.jahia.services.deamons.filewatcher.webappobserver;

import java.io.Serializable;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.webapps_deployer.JahiaWebAppsDeployerService;

/**
 * An Observable object for web apps
 *
 * @author Khue ng
 * @version 1.0
 */
public class WebAppsObserver implements Observer, Serializable {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(WebAppsObserver.class);

    /**
     * updatedOnce true if update() has been executed once *
     */
    private boolean mUpdatedOnce = false;

    private JahiaWebAppsDeployerService ads;
    private JahiaSitesService ss;

    /**
     * Constructor
     *
     * @param path      the path of directory to watch.
     * @param checkDate check by last modif date or not
     * @param interval  the interval of check
     * @param fileOnly  check both new files and directory
     */
    public WebAppsObserver(String path,
            boolean checkDate,
            long interval,
            boolean fileOnly,
            JahiaSitesService sitesService,
            JahiaFileWatcherService fws,
            JahiaWebAppsDeployerService wads)
            throws JahiaException {

        this.ss = sitesService;
        this.ads = wads;
        if (fws != null) {

            try {
                fws.addFileWatcher(path, path, checkDate, interval, fileOnly);
                fws.registerObserver(path, this);
                fws.startFileWatcher(path);

            } catch (JahiaException e) {

                logger.error("init:: ", e);
                throw new JahiaInitializationException(
                        "WebAppsObserver::init failed ", e);
            }
        }
    }

    /**
     * Handles new files detection
     */
    public void update(Observable subject,
                       Object args) {

        synchronized (args) {

            if (ads != null
                && ss != null && Jahia.isInitiated()) {

                List files = (List) args;
                try {
                    ads.deploy(files);
                } catch (Exception t) {
                    logger.error("Web apps observer error : ", t);
                }
            }
        }
    }

}